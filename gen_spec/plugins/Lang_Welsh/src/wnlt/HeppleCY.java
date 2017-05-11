/*
 *  HeppleCY.java
 *  This file is part of Welsh Natural Language Toolkit (WNLT)
 *  (see http://gate.ac.uk/), and is free software, licenced under 
 *  the GNU Library General Public License, Version 2, June 1991
 *  
 *  
 */
package wnlt;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import hepple.postag.InvalidRuleException;
import hepple.postag.POSTagger;

/**
 * Extends the Mark Hepple's POS tagger POSTagger class for the purposes 
 * of the Welsh Natural Language Toolkit WNLT. The class extends the original Hepple Tagger
 * with conditional statements that classify words using linguistic evidence. 
 * 
 * @author Andreas Vlachidis 20/03/2016
 *
 */
public class HeppleCY extends POSTagger {
	

	LexiconCY lexicon;
	
	static final String staart = "STAART";
    private String[] staartLex = { staart };
    private String[] deflex_NNM = { "NNM"};
    private String[] deflex_NNF = { "NNF"};
    private String[] deflex_JJ  = { "JJ"};
    private String[] deflex_VB = { "VB"};
    private String[] deflex_VBI = { "VBI"};
    private String[] deflex_NNP = { "NNP"};
    private String[] deflex_CD  = { "CD"};
    private String[] deflex_NNS = { "NNS"};
    private String[] deflex_NN  = { "NN"};
    private String[] deflex_PN  = { "PN"};
    private String[] deflex_SC  = { "SC"};
	
	
	/**
     * Construct a POS tagger using the platform's native encoding to read the
     * lexicon and rules files.
     */
    public HeppleCY(URL lexiconURL, URL rulesURL) throws InvalidRuleException,
                                                          IOException {
      super(lexiconURL, rulesURL, null);
    }

    /**
     * Construct a POS tagger using the specified encoding to read the lexicon
     * and rules files.
     */
    public HeppleCY(URL lexiconURL, URL rulesURL, String encoding) throws InvalidRuleException,
                                                          IOException{
    	super(lexiconURL, rulesURL, encoding);
    	this.lexicon = new LexiconCY(lexiconURL, encoding); 
    }
    
	
	/**
	   * Attempts to classify an unknown word.
	   * @param wd the word to be classified
	   */
	  protected String[] classifyWord(String wd){
	    String[] result;

	    if (staart.equals(wd)) return staartLex;

		List<String> categories = lexicon.get(wd.toLowerCase());
	    if(categories != null){
	      result = new String[categories.size()];
	      for(int i = 0; i < result.length; i++){
	        result[i] = categories.get(i);
	      }
	      return result;
	    }

	    //no lexical entry for the word. Try to guess
	    if ('A' <= wd.charAt(0) && wd.charAt(0) <= 'Z') return deflex_NNP;

	    for (int i=0 ; i < wd.length() ; i++)
	      if ('0' <= wd.charAt(i) && wd.charAt(i) <= '9') return deflex_CD;
	    if (wd.endsWith("d")) {
	    	if (wd.endsWith("id") || wd.endsWith("od") || wd.endsWith("awd")) {
	    		return deflex_NNM;
	    	}
	    	if (wd.endsWith("ydd")){
	    		if (wd.endsWith("feydd")){
	    			return deflex_NNS;
	    		}
	    		else {
	    			return deflex_NNM;
	    		}
	    	}
	    	if (wd.endsWith("edd")) {
	    		if (wd.endsWith("oedd")){
	    			return deflex_NNF;
	    		}
	    		else {
	    			return deflex_NNM;
	    		}
	    	}
	    	if (wd.endsWith("yd")) {
	    		if (wd.endsWith("lyd")){
	    			return deflex_JJ;
	    		}
	    		else {
	    			return deflex_NNM;
	    		}
	    	}
	    	if (!wd.endsWith("ad") || !wd.endsWith("aid") || !wd.endsWith("ed")) {
	    			return deflex_VB;
	    		}
	    } //End of ends with d
	    if (wd.endsWith("eb")) {
    		if (wd.endsWith("deb") || wd.endsWith("ineb")){
    			return deflex_NNM;
    		}
    		else {
    			return deflex_NNF;
    		}
    	}//End of ends with eb
	    if (wd.endsWith("el")) {
    		if (wd.endsWith("fel")){
    			return deflex_NNM;
    		}
    		else {
    			return deflex_VB;
    		}
    	}//End of ends with el
	    if (wd.endsWith("ur")) {
    		if (wd.endsWith("adur")){
    			return deflex_NNM;
    		}
    		else {
    			return deflex_VB;
    		}
    	}//End of ends with ur
	    if (wd.endsWith("u")) {
    		if (wd.endsWith("au")){
    			return deflex_NNS;
    		}
    		else {
    			return deflex_VBI;
    		}
    	}//End of ends with u
	    if (wd.endsWith("a")) {
    		if (wd.endsWith("dra")){
    			return deflex_NNM;
    		} 
    		else if (wd.endsWith("fa")){
    			return deflex_NNF;
    		}
    		else {
    			return deflex_VB;
    		}
    	}//End of ends with a
	    if (wd.endsWith("ig")) {
    		if (wd.endsWith("wraig")){
    			return deflex_NNF;
    		}
    		else {
    			return deflex_JJ;
    		}
    	}//End of ends with ig
	    
	    if (wd.endsWith("aint") ||
		        wd.endsWith("cyn") ||
		        wd.endsWith("der") ||
		        wd.endsWith("iant") ||
		        wd.endsWith("mon") ||
		        wd.endsWith("wr") ||
		        wd.endsWith("yr")) return deflex_NNM;
	    //End return NNM - Noun Masculine
	    
	    if (wd.endsWith("ell") ||
		        wd.endsWith("en") ||
		        wd.endsWith("es") ||
		        wd.endsWith("red")) return deflex_NNF;
	    //End return NNF - Noun Feminine 
	    
	    if (wd.endsWith("fan") ||
		        wd.endsWith("ain") ||
		        wd.endsWith("o")) return deflex_VB;
	    //End return VB - Verb
	    
	    if (wd.endsWith("adwy") ||
		        wd.endsWith("aidd") ||
		        wd.endsWith("ar") ||
		        wd.endsWith("ed") ||
		        wd.endsWith("gar") ||
		        wd.endsWith("lon") ||
		        wd.endsWith("us")) return deflex_JJ;
	    //End return JJ - Adjective
	    
	    if (wd.endsWith("os")) return deflex_NNS; 
	    //End return JJ - Noun Plural 
	    
	    if (wd.matches("[^A-Za-z0-9\\s’'\\[\\]\\(\\)\\{\\}⟨⟩:,،、‒…\\-\\!\\.?‘’“”'\\\";/⁄]")) return deflex_SC;
	    //End return JJ - Noun Special Character 
	    
	    if (wd.matches("[’'\\[\\]\\(\\)\\{\\}⟨⟩:,،、‒…\\-\\!\\.?‘’“”'\\\";/⁄]")) return deflex_PN;
	    //End return PN - Punctuation
	    
	    return deflex_NN;
	    //Return NN if non of the above conditions are true   
	    
	  }//End private String[] classifyWord(String wd)
	

}
