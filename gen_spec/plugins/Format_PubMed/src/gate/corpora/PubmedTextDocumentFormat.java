/*
 *  PubmedTextDocumentFormat.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 17 May 2012
 *
 *  $Id: PubmedTextDocumentFormat.java 19047 2015-12-23 14:08:05Z ian_roberts $
 */
package gate.corpora;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.GateConstants;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.util.DocumentFormatException;
import gate.util.InvalidOffsetException;
import gate.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * A document format analyser for PubMed text documents. Use mime type value 
 * "text/x-pubmed", or file extension ".pubmed.txt" to access this document 
 * format. 
 */
@CreoleResource(name="GATE .pubMed.txt document format",
  comment="<html>Load this to allow the opening of PubMed text documents, " +
  		"and choose the mime type <strong>\"text/x-pubmed\"</strong>or use " +
      "the correct file extension.", 
  autoinstances = {@AutoInstance(hidden=true)},
  isPrivate = true)
public class PubmedTextDocumentFormat extends TextualDocumentFormat {
  
  private static final long serialVersionUID = -2188662654167010328L;

  public static final String PUBMED_TITLE = "TI";
  
  public static final String PUBMED_ABSTRACT = "AB";
  
  public static final String PUBMED_AUTHORS = "AU";
  
  public static final String PUBMED_ID = "PMID";
  
  protected static final Logger logger = Logger.getLogger(
      PubmedTextDocumentFormat.class);
  
  protected String fieldPattern;

  protected String ignorePattern;

  protected List<String> fieldsForText;

  protected List<String> excludeFromFeatures;

  /* (non-Javadoc)
   * @see gate.DocumentFormat#supportsRepositioning()
   */
  @Override
  public Boolean supportsRepositioning() {
    return false;
  }

  @CreoleParameter(comment = "Regular expression that matches the (whole of the) "
      + "first line of a new field. The expression should include two named "
      + "capturing groups, <CODE> for the field code and <VALUE> for the "
      + "field value.", defaultValue = "(?<CODE>....)- (?<VALUE>.*)")
  public void setFieldPattern(String fieldPattern) {
    this.fieldPattern = fieldPattern;
  }

  public String getFieldPattern() {
    return fieldPattern;
  }

  @Optional
  @CreoleParameter(comment = "Regular expression that matches (the whole of) "
      + "any lines that should be silently ignored.  If unspecified, all "
      + "lines are considered.")
  public void setIgnorePattern(String ignorePattern) {
    this.ignorePattern = ignorePattern;
  }

  public String getIgnorePattern() {
    return ignorePattern;
  }

  @CreoleParameter(comment = "Fields which should be mapped into the document "
      + "text.  Each entry in this list should be a string of the form "
      + "fieldcode=annotationtype, the corresponding fields will be "
      + "concatenated together, separated by blank lines, to form the content "
      + "of the unpacked document, and each will be covered by an annotation of "
      + "the appropriate type in the Original markups set.",
      defaultValue = PUBMED_TITLE + "=title;" + PUBMED_ID + "=id;" +
                PUBMED_AUTHORS + "=authors;" + PUBMED_ABSTRACT + "=abstract")
  public void setFieldsForText(List<String> fieldsForText) {
    this.fieldsForText = fieldsForText;
  }

  public List<String> getFieldsForText() {
    return fieldsForText;
  }

  @CreoleParameter(comment = "Fields which should not be mapped to document "
      + "features. All fields found in the text which are not mentioned here "
      + "will be stored as features on the document.",
      defaultValue = PUBMED_TITLE + ";" + PUBMED_ABSTRACT)
  public void setExcludeFromFeatures(List<String> excludeFromFeatures) {
    this.excludeFromFeatures = excludeFromFeatures;
  }

  public List<String> getExcludeFromFeatures() {
    return excludeFromFeatures;
  }
  
  /* (non-Javadoc)
   * @see gate.corpora.TextualDocumentFormat#init()
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    MimeType mime = new MimeType("text","x-pubmed");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("pubmed.txt", mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }
  
  /* (non-Javadoc)
   * @see gate.corpora.TextualDocumentFormat#cleanup()
   */
  @Override
  public void cleanup() {
    super.cleanup();
    
    MimeType mime = getMimeType();
    
    mimeString2ClassHandlerMap.remove(mime.getType()+ "/" + mime.getSubtype());
    mimeString2mimeTypeMap.remove(mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeTypeMap.remove("pubmed.txt");
  }


  /* (non-Javadoc)
   * @see gate.corpora.TextualDocumentFormat#unpackMarkup(gate.Document)
   */
  @Override
  public void unpackMarkup(Document doc) throws DocumentFormatException {
    try {
      BufferedReader content = new BufferedReader(new StringReader(
          doc.getContent().toString()));
      Map<String, Serializable> fields = new HashMap<String, Serializable>();
      String line = content.readLine();
      String key = null;
      StringBuilder value = new StringBuilder();
      Pattern ignorePatt = null;
      if(ignorePattern != null) {
        ignorePatt = Pattern.compile(ignorePattern);
      }
      Pattern linePatt =  Pattern.compile(fieldPattern);
      while(line!= null) {
        // skip ignorable lines
        if(ignorePatt == null || !ignorePatt.matcher(line).matches()) {
          Matcher matcher = linePatt.matcher(line);
          if(matcher.matches()) {
            // new field
            if(key != null) {
              // save old value
              PubmedUtils.addFieldValue(key, value.toString(), fields);
            }
            key = matcher.group("CODE").trim();
            value.delete(0, value.length());
            value.append(matcher.group("VALUE"));
          } else {
            // a non-assignment line -> append to previous value
            if(value.length() == 0) {
              logger.warn("Ignoring invalid input line:\""  +
                  line +	"\"");
            } else {
              value.append(Strings.getNl()).append(line.trim());
            }
          }
        }
        line = content.readLine();
      }
      if(key != null) {
        // save old value
        PubmedUtils.addFieldValue(key, value.toString(), fields);
      }
      StringBuilder docText = new StringBuilder();
      // build document text
      int[] starts = new int[fieldsForText.size()];
      int[] ends = new int[fieldsForText.size()];
      for(int i = 0; i < fieldsForText.size(); i++) {
        String[] field = fieldsForText.get(i).split("=", 2);
        starts[i] = docText.length();
        ends[i] = starts[i];
        Serializable aField = fields.get(field[0]);
        if(aField != null) {
          docText.append(PubmedUtils.getFieldValueString(aField));
          ends[i] = docText.length();
          docText.append(Strings.getNl()).append(Strings.getNl());
        } else {
          String docName = doc.getName();  
          logger.warn("Could not find " + field[1] + " in document " + 
              (docName != null ? docName : ""));
        }
      }

      doc.setContent(new DocumentContentImpl(docText.toString()));
      
      AnnotationSet origMkups = doc.getAnnotations(
          GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
      for(int i = 0; i < fieldsForText.size(); i++) {
        String[] field = fieldsForText.get(i).split("=", 2);
        if(ends[i] > starts[i]) {
          origMkups.add((long)starts[i], (long)ends[i], field[1], 
              Factory.newFeatureMap());
        }
      }

      // everything else becomes document features
      for(String keyToExclude : excludeFromFeatures) {
        fields.remove(keyToExclude);
      }
      doc.getFeatures().putAll(fields);
    } catch(IOException e) {
      throw new DocumentFormatException("Error while unpacking markup",e); 
    } catch(InvalidOffsetException e) {
      throw new DocumentFormatException("Error while unpacking markup",e);
    }
    
    // now let the text unpacker also do its job
    super.unpackMarkup(doc);
  }
  
}
