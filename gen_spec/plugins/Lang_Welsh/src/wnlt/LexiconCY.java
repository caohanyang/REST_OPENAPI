/*
 *  LexiconCY.java
 *  This file is part of Welsh Natural Language Toolkit (WNLT)
 *  (see http://gate.ac.uk/), and is free software, licenced under 
 *  the GNU Library General Public License, Version 2, June 1991
 *  
 *  
 */
package wnlt;

import gate.util.BomStrippingInputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;

/**
 *  LexiconCY is an exact copy of the Hepple's POS Tagger Lexicon.java
 *  that permits HeppleCY class to use a Lexicon. 
 *  
 *  A {@link java.util.HashMap} that maps from lexical entry
 *  ({@link java.lang.String}) to possible POS categories
 *  ({@link java.util.List})
 *  
 *  @author Andreas Vlachidis 20/03/2016
 *  
 */
public class LexiconCY extends HashMap<String,List<String>> {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Constructor.
   * @param lexiconURL an URL for the file containing the lexicon.
   */
  public LexiconCY(URL lexiconURL) throws IOException{
    this(lexiconURL, null);
  }

  /**
   * Constructor.
   * @param lexiconURL an URL for the file containing the lexicon.
   * @param encoding the character encoding to use for reading the lexicon.
   */
  public LexiconCY(URL lexiconURL, String encoding) throws IOException{
    String line;
    BufferedReader lexiconReader = null;
    InputStream lexiconStream = null;
    
    try {
      lexiconStream = lexiconURL.openStream();
      
      if(encoding == null) {
        lexiconReader = new BomStrippingInputStreamReader(lexiconStream);
      } else {
        lexiconReader = new BomStrippingInputStreamReader(lexiconStream,encoding);
      }
  
      line = lexiconReader.readLine().toLowerCase();
      String entry;
      List<String> categories;
      while(line != null){
        StringTokenizer tokens = new StringTokenizer(line);
        entry = tokens.nextToken();
        categories = new ArrayList<String>();
        while(tokens.hasMoreTokens()) categories.add(tokens.nextToken());
        put(entry, categories);
  
        line = lexiconReader.readLine();
      }//while(line != null)
    }
    finally {
      IOUtils.closeQuietly(lexiconReader);
      IOUtils.closeQuietly(lexiconStream);
    }
  }//public Lexicon(URL lexiconURL) throws IOException

}//class Lexicon