/*
 *  CochraneTextDocumentFormat.java
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
 *  $Id: CochraneTextDocumentFormat.java 19047 2015-12-23 14:08:05Z ian_roberts $
 */
package gate.corpora;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.util.DocumentFormatException;

import java.util.List;

/**
 * A document format analyser for Cochrane text documents. Use mime type value 
 * "text/x-cochrane", or file extension ".cochrane.txt" to access this document
 * format. 
 */
@CreoleResource(name="GATE .cochrane.txt document format",
  comment="<html>Load this to allow the opening of Cochrane text documents, " +
      "and choose the mime type <strong>\"text/x-cochrane\"</strong>, or use " +
      "the correct file extension.", 
  autoinstances = {@AutoInstance(hidden=true)},
  isPrivate = true)
public class CochraneTextDocumentFormat extends PubmedTextDocumentFormat {

  private static final long serialVersionUID = 8362288605943414676L;

  private static final String COCHRANE_TITLE = "TI";
  
  private static final String COCHRANE_ABSTRACT = "AB";
  
  private static final String COCHRANE_AUTHORS = "AU";
  
  private static final String COCHRANE_ID = "ID";
  
  /* (non-Javadoc)
   * @see gate.corpora.TextualDocumentFormat#init()
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    MimeType mime = new MimeType("text","x-cochrane");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("cochrane.txt", mime);
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
    suffixes2mimeTypeMap.remove("cochrane.txt");
  }

  @CreoleParameter(defaultValue = "(?<CODE>[A-Z]+): (?<VALUE>.*)")
  public void setFieldPattern(String fieldPattern) {
    super.setFieldPattern(fieldPattern);
  }
  

  @CreoleParameter(defaultValue = COCHRANE_TITLE + "=title;" + COCHRANE_ID +
      "=id;" + COCHRANE_AUTHORS + "=authors;" + COCHRANE_ABSTRACT +
      "=abstract")
  public void setFieldsForText(List<String> fieldsForText) {
    super.setFieldsForText(fieldsForText);
  }

  @CreoleParameter(defaultValue = COCHRANE_TITLE + ";" + COCHRANE_ABSTRACT)
  public void setExcludeFromFeatures(List<String> excludeFromFeatures) {
    super.setExcludeFromFeatures(excludeFromFeatures);
  }

}
