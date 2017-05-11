/*
 * CSVStreamingOutputHandler.java
 * 
 * Copyright (c) 2015, The University of Sheffield. See the file COPYRIGHT.txt
 * in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Mark A. Greenwood, 5/08/2015
 */

package gate.cloud.io.csv;

import static gate.cloud.io.IOConstants.PARAM_ENCODING;
import static gate.cloud.io.IOConstants.PARAM_FILE_EXTENSION;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;
import gate.cloud.batch.DocumentID;
import gate.cloud.io.json.JSONStreamingOutputHandler;
import gate.util.GateException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVStreamingOutputHandler extends JSONStreamingOutputHandler {

  public static final String PARAM_SEPARATOR_CHARACTER = "separator";

  public static final String PARAM_QUOTE_CHARACTER = "quote";

  public static final String PARAM_COLUMNS = "columns";

  public static final String PARAM_ANNOTATION_SET_NAME = "annotationSetName";

  public static final String PARAM_ANNOTATION_TYPE = "annotationType";
  
  public static final String PARAM_CONTAINED_ONLY = "containedOnly";

  private static final Logger logger = Logger
      .getLogger(CSVStreamingOutputHandler.class);

  protected String encoding;

  protected char separatorChar;

  protected char quoteChar;

  protected String annotationSetName, annotationType;

  protected String[] columns;
  
  protected boolean containedOnly;

  @Override
  protected void configImpl(Map<String, String> configData) throws IOException,
      GateException {

    if(!configData.containsKey(PARAM_FILE_EXTENSION)) {
      // set the extension to csv if nothing is provided
      configData.put(PARAM_FILE_EXTENSION, ".csv");
    }

    // handle the standard streaming output config options
    super.configImpl(configData);

    // configuration params for the CSV document output
    encoding =
        configData.containsKey(PARAM_ENCODING)
            ? configData.get(PARAM_ENCODING)
            : "UTF-8";

    separatorChar =
        configData.containsKey(PARAM_SEPARATOR_CHARACTER) ? configData.get(
            PARAM_SEPARATOR_CHARACTER).charAt(0) : CSVWriter.DEFAULT_SEPARATOR;

    quoteChar =
        configData.containsKey(PARAM_QUOTE_CHARACTER)
            ? configData.get(PARAM_QUOTE_CHARACTER).charAt(0)
            : CSVWriter.DEFAULT_QUOTE_CHARACTER;

    // the details of the columns to output
    columns = configData.get(PARAM_COLUMNS).split(",\\s*");

    // the annotation set to read annotations from
    annotationSetName = configData.get(PARAM_ANNOTATION_SET_NAME);

    // the annotation type to treat as a document, can be null
    annotationType = configData.get(PARAM_ANNOTATION_TYPE);

    // if the annotationType param is empty then nullify the variable so we work
    // at the document level as if the param was missing
    if(annotationType != null && annotationType.trim().equals(""))
      annotationType = null;
    
    // should we only look at annotations contained within the annotationType or
    // do we allow overlapping ones as well?
    containedOnly =
        configData.containsKey(PARAM_CONTAINED_ONLY) ? Boolean
            .parseBoolean(configData.get(PARAM_CONTAINED_ONLY)) : true;
  }

  @Override
  protected void outputDocumentImpl(Document document, DocumentID documentId)
      throws IOException, GateException {

    // TODO move to a thread local to save recreating each time?
    // create the CSV writer ready for creating output
    CSVWriter csvOut =
        new CSVWriter(new OutputStreamWriter(getFileOutputStream(documentId),
            encoding), separatorChar, quoteChar);

    // create an array to hold the column data
    String[] data = new String[columns.length];

    if(annotationType == null) {
      // if we are producing one row per document then....

      for(int i = 0; i < columns.length; ++i) {
        // get the data for each column
        data[i] = (String)getValue(columns[i], document, null);
      }

      // write the row to the output
      csvOut.writeNext(data);
    } else {

      // we are producing one row per annotation so find all the annotations of
      // the correct type to treat as documents
      List<Annotation> sorted =
          Utils.inDocumentOrder(document.getAnnotations(annotationSetName).get(
              annotationType));

      for(Annotation annotation : sorted) {
        // for each of the annotations....

        for(int i = 0; i < columns.length; ++i) {
          // get the data for each column
          data[i] = (String)getValue(columns[i], document, annotation);
        }

        // write the row to the ouput
        csvOut.writeNext(data);
      }
    }

    // flush the writer to ensure everything is pushed into the byte array
    csvOut.flush();

    // get the bytes we will want to put into the output file
    byte[] result = baos.get().toByteArray();

    // close the CSV writer as we don't need it anymore
    csvOut.close();

    // reset the underlying byte array output stream ready for next time
    baos.get().reset();

    try {
      // store the results so that the they will eventually end up in the output
      results.put(result);
    } catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Get the value from the document given the column key
   */
  private Object getValue(String key, Document document, Annotation within) {

    // split the key on any . that appear
    String[] parts = key.split("\\.");

    if(parts.length > 2) {
      // currently we only support keys with at most two parts
      logger.log(Level.WARN, "Invalid Column Key: " + key);
      return null;
    }

    if(key.startsWith(".")) {
      // keys that start with a . are references to document features
      return document.getFeatures().get(parts[1]);
    } else {
      // if it's not a document feature then it refers to an annotation

      // get all annotations of the correct type (the bit before the .)
      AnnotationSet annots =
          document.getAnnotations(annotationSetName).get(parts[0]);

      if(within != null) {
        // if we have been provided with an annotation to limit the search then
        // get just those either....
        
        if (containedOnly) {
          // contained within the annotation
          annots = Utils.getContainedAnnotations(annots, within);
        }
        else {
          // or partially overlapping with it
          annots = Utils.getOverlappingAnnotations(annots, within);
        }
      }

      // if there are no annotations then we can quit
      if(annots.size() == 0) return null;

      // get the first annotation that matches
      Annotation annotation = Utils.inDocumentOrder(annots).get(0);

      // if we just want the annotation then return it's document content
      if(parts.length == 1) return Utils.stringFor(document, annotation);

      // the key references a feature so return that
      return annotation.getFeatures().get(parts[1]);
    }
  }
}
