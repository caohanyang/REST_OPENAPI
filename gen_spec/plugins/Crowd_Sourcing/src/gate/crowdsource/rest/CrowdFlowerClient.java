/*
 *  CrowdFlowerClient.java
 *
 *  Copyright (c) 1995-2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 3, June 2007 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  $Id: CrowdFlowerClient.java 19726 2016-11-08 13:09:38Z domrout $
 */
package gate.crowdsource.rest;

import static gate.crowdsource.CrowdFlowerConstants.*;

import gate.*;
import gate.util.GateRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class CrowdFlowerClient {

  private static final Logger log = Logger.getLogger(CrowdFlowerClient.class);

  public static final String CF_ENDPOINT = "https://api.crowdflower.com/v1";

  private String apiKey;

  public CrowdFlowerClient(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Create a new entity classification job on CrowdFlower.
   * 
   * @param title the job title
   * @param instructions the instructions
   * @param caption a caption for the answer form, which can include an
   *          {{entity}} placeholder which will be replaced by the text
   *          of the entity being classified (e.g.
   *          "choose the most appropriate description for {{entity}}")
   * @param commonOptions common options that should be presented for
   *          all tasks, in addition to the specific options for that
   *          task extracted from the entity annotation. This is
   *          expressed as a List of [value, description] two-element
   *          Lists, one for each option.
   * @param commentCaption caption for the free-text "comment" field, or null
   *          if no comment field should be shown.
   * @return the newly created job ID.
   * @throws IOException
   */
  public long createClassificationJob(String title, String instructions,
          String caption, List<List<String>> commonOptions,
          String commentCaption) throws IOException {
    log.debug("Creating classification job");
    log.debug("title: " + title);
    log.debug("instructions: " + instructions);
    log.debug("caption: " + caption);
    log.debug("options: " + commonOptions);
    // load the custom CSS that makes entity highlighting work
    InputStream cssStream =
            CrowdFlowerClient.class.getResourceAsStream("gate-crowdflower.css");
    String css = null;
    try {
      css = IOUtils.toString(cssStream, "UTF-8");
    } finally {
      cssStream.close();
    }
    StringWriter cml = new StringWriter();

    // load the javascript that selects a default option if one is provided.
    InputStream jsStream =
            CrowdFlowerClient.class.getResourceAsStream("gate-crowdflower-classification.js");
    StringWriter js = new StringWriter();
    try {
      js.append(IOUtils.toString(jsStream, "UTF-8"));
    } finally {
      jsStream.close();
    }

    // construct the CML with the specified caption and common radio
    // options
    cml.append("<h2 id=\"unit_text\">{{text}}</h2>\n\n"
            + "{% unless detail == \"No data available\" %}\n"
            + "  <div class=\"well\">{{detail}}</div>\n"
            + "{% endunless %}\n"
            + "<cml:radios validates=\"required\" data-default=\"{{default}}\" label=\"");
    StringEscapeUtils.escapeXml(cml, caption);
    cml.append("\" name=\"answer\">\n"
            + "  {% unless options == \"No data available\" %}\n"
            + "    {% for opt in options %}\n"
            + "      {% if opt.description %}\n"
            + "        {% assign desc = opt.description %}\n"
            + "      {% else %}\n" + "      {% assign desc = opt.value %}\n"
            + "      {% endif %}\n"
            + "      <cml:radio value=\"{{opt.value}}\" label=\"{{desc}}\" />\n"
            + "    {% endfor %}\n"
            + "  {% endunless %}\n");
    if(commonOptions != null) {
      for(List<String> opt : commonOptions) {
        cml.append("  <cml:radio value=\"");
        StringEscapeUtils.escapeXml(cml, opt.get(0));
        cml.append("\" label=\"");
        StringEscapeUtils.escapeXml(cml, opt.get(1));
        cml.append("\" />\n");
      }
    }
    cml.append("</cml:radios>\n");
    if(commentCaption != null) {
      cml.append("<cml:text name=\"comment\" label=\"");
      StringEscapeUtils.escapeXml(cml, commentCaption);
      cml.append("\"/>");
    }

    log.debug("cml: " + cml.toString());

    log.debug("POSTing to CrowdFlower");
    JsonElement json =
            post("/jobs", "job[title]", title, "job[instructions]",
                    instructions, "job[cml]", cml.toString(), "job[css]", css,
                    "job[js]", js.toString());
    log.debug("CrowdFlower returned " + json);
    try {
      return json.getAsJsonObject().get("id").getAsLong();
    } catch(Exception e) {
      throw new GateRuntimeException("Failed to create CF job");
    }
  }

  /**
   * <p>
   *     Generates the text required to display the context for a classification,
   *     either based on the document content, or on features present in the annotation.
   *
   *     If any of leftContext, rightContext, or entity are set as features on the annotation,
   *     the text will be generated using those.
   *
   *     If none of these are present, the contents of the context and target feature will be
   *     combined to give the document text used.
   * </p>
   * @param doc The document containing text that will be used if needed
   * @param context The context annotation in which the entity will be couched
   * @param target The target entity to display.
   * @return
   */
  private String createClassificationUnitText(Document doc, Annotation context,
                                              Annotation target) {
    FeatureMap targetFeatures = target.getFeatures();

    try {
      // Fill in each part of the text individually, starting with the entity itself.
      String entity; // No initialiser needed as we're guaranteed to overwrite it.

      if (targetFeatures.containsKey("entity")) {
        entity = (String) targetFeatures.get("entity");
      } else {
        entity = Utils.stringFor(doc, target);
      }

      // Look for the left context.
      String leftContext = "";

      if (targetFeatures.containsKey("leftContext")) {
        leftContext = (String) targetFeatures.get("leftContext");
      } else if (context != null) {
        leftContext = Utils.stringFor(doc, Utils.start(context), Utils.start(target));
      }

      // Look for the right context.
      String rightContext = "";

      if (targetFeatures.containsKey("rightContext")) {
        rightContext = (String) targetFeatures.get("rightContext");
      } else if (context != null) {
        rightContext = Utils.stringFor(doc, Utils.end(target), Utils.end(context));
      }

      return leftContext + "<span class=\"gate-entity\">" + entity + "</span>" + rightContext;
    } catch (ClassCastException e) {
      throw new ClassCastException("Features entity, leftContext and rightContext must be String, if provided.");
    }

  }
  /**
   * <p>
   * Create a single unit in a classification job for the given
   * annotation. If the unit is created successfully, its ID will be
   * stored as a {@link Long} valued feature named <code>cf_unit</code>
   * on the target annotation.
   * </p>
   * 
   * <p>
   * The target annotation must have a feature named "options" whose
   * value is either a <code>Collection</code> of valid choices or a
   * <code>Map</code> where each key represents one option value and the
   * corresponding value is the human-readable description that will be
   * displayed to the annotator (e.g. the key could be an ontology
   * instance URI and the value its rdfs:label). The options will be
   * added to the unit in the order they are delivered by the iterator,
   * so if order is important you should use a collection or map with
   * predictable iteration order, e.g. {@link List} or
   * {@link LinkedHashMap}.
   * </p>
   * 
   * <p>
   * If the target annotation has a feature named "correct" then it will
   * be treated as a gold-standard unit. The "correct" feature must
   * match one of the "options" (i.e. one of the <i>keys</i> if options
   * is a Map) or one of the common options defined when the job was
   * created - typically things like "none" (none of the available
   * options is correct) or "nae" (the target is not an entity).
   * </p>
   *
   * <p>
   * If the target annotation has a feature named "leftContext", "rightContext"
   * or "entity", these will be used to generate the entity text for display.
   * Otherwise, the text will be taken from the document, as covered by the
   * context and target annotations.
   * </p>
   * 
   * @param jobId the CrowdFlower job ID
   * @param doc the document containing the annotation
   * @param asName the annotation set containing the target annotation
   * @param context the "context" annotation, i.e. and annotation
   *          covering the complete snippet of text (e.g. the sentence)
   *          within which the target entity will be highlighted in the
   *          unit. This need not live in the same annotation set as the
   *          <code>target</code> but must cover the target's span.
   * @param target the annotation representing the entity to be
   *          classified.
   * @return the ID of the generated unit.
   */
  public long createClassificationUnit(long jobId, Document doc, String asName,
          Annotation context, Annotation target) {

    String entity; // No initialiser needed as we're guaranteed to overwrite it.

    if (target.getFeatures().containsKey("entity")) {
      entity = (String) target.getFeatures().get("entity");
      if (entity == null) {
        throw new NullPointerException("Given entity feature was null");
      }
    } else {
      entity = Utils.stringFor(doc, target);
    }


    String defaultSelection = (String) target.getFeatures().get("default");

    String text = createClassificationUnitText(doc, context, target);

    String documentId = String.valueOf(doc.getLRPersistenceId());
    int formDataSize = 12; // text + entity + docId + asName + default + annId


    Object options = target.getFeatures().get("options");
    if(options != null) {
      if(options instanceof Map) {
        formDataSize += (4 * ((Map<?, ?>)options).size());
      } else if(options instanceof Collection) {
        formDataSize += (2 * ((Collection<?>)options).size());
      }
    }

    if(target.getFeatures().get("detail") != null) {
      formDataSize += 2;
    }

    String correctAnswer = (String)target.getFeatures().get("correct");
    String reason = (String)target.getFeatures().get("reason");
    if(correctAnswer != null) {
      formDataSize += 4; // "golden" + answer
      if(reason != null) {
        formDataSize += 2; // answer_gold_reason
      }
    }

    String[] formData = new String[formDataSize];
    int i = 0;
    formData[i++] = "unit[data][text]";
    formData[i++] = text;
    formData[i++] = "unit[data][entity]";
    formData[i++] = entity;
    formData[i++] = "unit[data][documentId]";
    formData[i++] = documentId;
    formData[i++] = "unit[data][asName]";
    formData[i++] = (asName == null ? "" : asName);
    formData[i++] = "unit[data][default]";
    formData[i++] = (defaultSelection == null ? "" : defaultSelection);
    formData[i++] = "unit[data][annId]";
    formData[i++] = String.valueOf(target.getId());
    if(options != null) {
      if(options instanceof Map) {
        for(Map.Entry<?, ?> opt : ((Map<?, ?>)options).entrySet()) {
          formData[i++] = "unit[data][options][][value]";
          formData[i++] = String.valueOf(opt.getKey());
          formData[i++] = "unit[data][options][][description]";
          formData[i++] = String.valueOf(opt.getValue());
        }
      } else if(options instanceof Collection) {
        for(Object opt : (Collection<?>)options) {
          formData[i++] = "unit[data][options][][value]";
          formData[i++] = String.valueOf(opt);
        }
      }
    }

    if(target.getFeatures().get("detail") != null) {
      formData[i++] = "unit[data][detail]";
      formData[i++] = target.getFeatures().get("detail").toString();
    }


    if(correctAnswer != null) {
      formData[i++] = "unit[golden]";
      formData[i++] = "true";
      formData[i++] = "unit[data][answer_gold]";
      formData[i++] = correctAnswer;
      if(reason != null) {
        formData[i++] = "unit[data][answer_gold_reason]";
        formData[i++] = reason;
      }
    }

    try {
      JsonElement json = post("/jobs/" + jobId + "/units", formData);
      long unitId = json.getAsJsonObject().get("id").getAsLong();
      // store the unit ID on the annotation
      target.getFeatures().put(UNIT_ID_FEATURE_NAME, Long.valueOf(unitId));
      return unitId;
    } catch(IOException e) {
      throw new GateRuntimeException("Could not add unit for annotation "
              + target, e);
    }
  }

  /**
   * Get the list of judgments for the given unit. If there are no
   * judgments, null is returned.
   * 
   * @param jobId the CrowdFlower job identifier
   * @param unitId the unit identifier
   * @return the "judgments" array for this unit, or null if none found.
   */
  public JsonArray getJudgments(long jobId, long unitId) {
    try {
      String uri = "/jobs/" + jobId + "/units/" + unitId;
      JsonElement unitResponse = get(uri);
      if(!unitResponse.isJsonObject()) {
        throw new GateRuntimeException("Response from " + uri
                + " was not a JSON object");
      }
      JsonElement results = unitResponse.getAsJsonObject().get("results");
      if(!results.isJsonObject()) {
        log.info("No results found for job " + jobId + " unit " + unitId);
        return null;
      }
      JsonElement judgments = results.getAsJsonObject().get("judgments");
      if(judgments.isJsonArray()) {
        return judgments.getAsJsonArray();
      } else {
        return null;
      }
    } catch(IOException e) {
      throw new GateRuntimeException("Could not retrieve unit details", e);
    }
  }

  /**
   * Create a named entity annotation job on CrowdFlower.
   * 
   * @param title the job title
   * @param instructions the instructions
   * @param caption a caption for the answer form, which should include
   *          the entity type to be annotated.
   * @param noEntitiesCaption a caption for the "there are no entities"
   *          checkbox.
   * @param noEntitiesError the error message to show to the user if they have
   *          not marked any annotations but have also not ticked the checkbox.
   * @param commentCaption caption for the free-text "comment" field, or null
   *          if no comment field should be shown.
   * @return the newly created job ID.
   * @throws IOException
   */
  public long createAnnotationJob(String title, String instructions,
          String caption, String noEntitiesCaption, String noEntitiesError,
          String commentCaption)
            throws IOException {
    log.debug("Creating annotation job");
    log.debug("title: " + title);
    log.debug("instructions: " + instructions);
    log.debug("caption: " + caption);

    // load the CSS that makes highlighting work
    InputStream cssStream =
            CrowdFlowerClient.class.getResourceAsStream("gate-crowdflower.css");
    String css = null;
    try {
      css = IOUtils.toString(cssStream, "UTF-8");
    } finally {
      cssStream.close();
    }

    // load the JavaScript that toggles the colour of tokens when
    // clicked
    InputStream jsStream =
            CrowdFlowerClient.class.getResourceAsStream("gate-crowdflower.js");
    StringWriter js = new StringWriter();
    try {
      js.append(IOUtils.toString(jsStream, "UTF-8"));
    } finally {
      jsStream.close();
    }

    // inject the custom error message
    // success.crowdflower.com/customer/portal/questions/5445414
    js.append("\n"
            + "var validator_options = CMLFormValidator.getValidator('required').options;\n"
            + "validator_options.errorMessage = function() {\n"
            + "  return ('");
    StringEscapeUtils.escapeJavaScript(js, noEntitiesError);
    js.append("');\n"
            + "};\n"
            + "CMLFormValidator.addAllThese([ ['required', validator_options] ]);\n");
    log.debug("js: " + js.toString());

    // construct the CML
    StringWriter cml = new StringWriter();
    cml.append("<div class=\"gate-snippet\">\n"
            + "  <cml:checkboxes validates=\"required\" label=\"");
    StringEscapeUtils.escapeXml(cml, caption);
    cml.append("\" name=\"answer\" data-default=\"{{ answer_default | join: ',' }}\">\n"
            + "    {% for tok in tokens %}\n"
            + "      <cml:checkbox label=\"{{ tok }}\" value=\"{{ forloop.index0 }}\" />\n"
            + "    {% endfor %}\n" + "  </cml:checkboxes>\n" + "</div>\n"
            + "{% unless detail == \"No data available\" %}\n"
            + "  <div class=\"well\">{{detail}}</div>\n"
            + "{% endunless %}\n"
            + "<div class=\"gate-no-entities\">\n"
            + "  <cml:checkbox name=\"noentities\" label=\"");
    StringEscapeUtils.escapeXml(cml, noEntitiesCaption);
    cml.append("\" value=\"1\"\n"
            + "      only-if=\"!answer\" validates=\"required\"/>\n"
            + "</div>\n");
    if(commentCaption != null) {
      cml.append("<cml:text name=\"comment\" label=\"");
      StringEscapeUtils.escapeXml(cml, commentCaption);
      cml.append("\"/>");
    }
    log.debug("cml: " + cml.toString());

    log.debug("POSTing to CrowdFlower");
    JsonElement json =
            post("/jobs", "job[title]", title, "job[instructions]",
                    instructions, "job[cml]", cml.toString(), "job[css]", css,
                    "job[js]", js.toString());
    log.debug("CrowdFlower returned " + json);
    try {
      return json.getAsJsonObject().get("id").getAsLong();
    } catch(Exception e) {
      throw new GateRuntimeException("Failed to create CF job");
    }
  }

  /**
   * Create a single unit for an entity annotation job.
   * 
   * @param jobId the CrowdFlower job ID
   * @param doc the document containing the annotation
   * @param asName the annotation set containing the snippet annotation
   * @param snippet an annotation covering the snippet of text that will
   *          be presented for annotation, typically a Sentence or Tweet
   * @param detail additional details to be shown to the annotator below
   *          the snippet, e.g. a list of URL links that they might want
   *          to follow for more information.  May be null, in which case
   *          no detail section will be added.
   * @param tokens annotations representing the individual substrings of
   *          the snippet that will be the atomic units of annotation.
   *          Typically these will be Token annotations. The supplied
   *          "tokens" should completely cover the non-whitespace
   *          content of the snippet, but need not cover all the
   *          intervening spaces.
   * @param correctAnnotations annotations representing the "correct"
   *          answer - if this parameter is not <code>null</code> then
   *          the unit will be considered as gold-standard data. This
   *          includes the case where an empty annotation set is
   *          provided, as this represents a gold snippet where the
   *          correct answer is that this snippet contains no entities.
   * @param goldReason for a gold-standard unit, the <em>reason</em> why
   *          the annotations should be considered correct. This will be
   *          shown to users as feedback if they get the gold unit
   *          wrong. Ignored for non-gold units.
   * @return the ID of the newly-created unit.
   */
  public long createAnnotationUnit(long jobId, Document doc, String asName,
          Annotation snippet, String detail, AnnotationSet tokens,
          AnnotationSet correctAnnotations, AnnotationSet defaultAnnotations,
                                   String goldReason) {
    String documentId = String.valueOf(doc.getLRPersistenceId());
    int formDataSize = 6; // docId + asName + annId
    List<Annotation> tokensList = Utils.inDocumentOrder(tokens);
    formDataSize += 2 * tokensList.size();
    if(detail != null) {
      formDataSize += 2;
    }
    Set<Integer> answerGold = null;
    HashSet<Integer> answerDefault = null;

    if(correctAnnotations != null) {
      // gold unit
      answerGold = new HashSet<Integer>();
      for(Annotation a : correctAnnotations) {
        for(int i = 0; i < tokensList.size(); i++) {
          Annotation tokenI = tokensList.get(i);
          if(Utils.start(tokenI) >= Utils.start(a)
                  && Utils.end(tokenI) <= Utils.end(a)) {
            answerGold.add(i);
          }
        }
      }
      formDataSize += 2; // "golden"
      if(answerGold.size() == 0) {
        formDataSize += 2; // noentities=1
      } else {
        formDataSize += 2 * answerGold.size(); // answer=N for each
                                               // token
      }
      if(goldReason != null) {
        formDataSize += 2; // answer_gold_reason or
                           // noentities_gold_reason
      }
    }

    if(defaultAnnotations != null) {
      // Add default annotations

      answerDefault = new HashSet<Integer>();
      for(Annotation a : defaultAnnotations) {
        for(int i = 0; i < tokensList.size(); i++) {
          Annotation tokenI = tokensList.get(i);
          if  (Utils.start(tokenI) >= Utils.start(a)
                  && Utils.end(tokenI) <= Utils.end(a)) {
            answerDefault.add(i);
          }
        }
      }
      formDataSize += 2 * answerDefault.size(); // answer=N for each

    }

    String[] formData = new String[formDataSize];
    int i = 0;
    formData[i++] = "unit[data][documentId]";
    formData[i++] = documentId;
    formData[i++] = "unit[data][asName]";
    formData[i++] = (asName == null ? "" : asName);
    formData[i++] = "unit[data][annId]";
    formData[i++] = String.valueOf(snippet.getId());
    for(Annotation tok : tokensList) {
      formData[i++] = "unit[data][tokens][]";
      formData[i++] = Utils.stringFor(doc, tok);
    }
    if(detail != null) {
      formData[i++] = "unit[data][detail]";
      formData[i++] = detail;
    }
    if(answerGold != null) {
      formData[i++] = "unit[golden]";
      formData[i++] = "true";
      if(answerGold.size() == 0) {
        formData[i++] = "unit[data][noentities_gold]";
        formData[i++] = "1";
        if(goldReason != null) {
          formData[i++] = "unit[data][noentities_gold_reason]";
          formData[i++] = goldReason;
        }
      } else {
        Integer[] goldArray =
                answerGold.toArray(new Integer[answerGold.size()]);
        Arrays.sort(goldArray);
        for(Integer tokIndex : goldArray) {
          formData[i++] = "unit[data][answer_gold][]";
          formData[i++] = String.valueOf(tokIndex);
        }
        if(goldReason != null) {
          formData[i++] = "unit[data][answer_gold_reason]";
          formData[i++] = goldReason;
        }
      }
    }
    if (answerDefault != null) {
      for(int answer : answerDefault) {
        formData[i++] = "unit[data][answer_default][]";
        formData[i++] = String.valueOf(answer);
      }
    }

    try {
      JsonElement json = post("/jobs/" + jobId + "/units", formData);
      long unitId = json.getAsJsonObject().get("id").getAsLong();
      return unitId;
    } catch(IOException e) {
      throw new GateRuntimeException("Could not add unit for annotation "
              + snippet, e);
    }
  }

  protected JsonElement post(String uri, String... formData) throws IOException {
    return request("POST", uri, formData);
  }

  protected JsonElement get(String uri) throws IOException {
    return request("GET", uri);
  }

  protected JsonElement request(String method, String uri, String... formData)
          throws IOException {
    if(log.isDebugEnabled()) {
      log.debug("URI: " + uri + ", formData: " + Arrays.toString(formData));
    }
    URL cfUrl = new URL(CF_ENDPOINT + uri + "?key=" + apiKey);
    HttpURLConnection connection = (HttpURLConnection)cfUrl.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Accept", "application/json");
    if(formData != null && formData.length > 0) {
      // send the form data, URL encoded
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type",
              "application/x-www-form-urlencoded");

      // annoyingly CrowdFlower doesn't support chunked streaming of
      // POSTs so we have to accumulate the content in a buffer, work
      // out its size and then POST with a Content-Length header
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
      PrintWriter writer =
              new PrintWriter(new OutputStreamWriter(buffer, "UTF-8"));
      try {
        for(int i = 0; i < formData.length; i++) {
          String fieldName = formData[i];
          String fieldValue = formData[++i];
          if(i > 0) {
            writer.write("&");
          }
          writer.write(URLEncoder.encode(fieldName, "UTF-8"));
          writer.write("=");
          writer.write(URLEncoder.encode(fieldValue, "UTF-8"));
        }
      } finally {
        writer.close();
      }

      connection.setFixedLengthStreamingMode(buffer.size());
      OutputStream connectionStream = connection.getOutputStream();
      buffer.writeTo(connectionStream);
      connectionStream.close();
    }

    // parse the response as JSON
    JsonParser parser = new JsonParser();
    Reader responseReader =
            new InputStreamReader(connection.getInputStream(), "UTF-8");
    try {
      return parser.parse(responseReader);
    } finally {
      responseReader.close();
    }
  }
}
