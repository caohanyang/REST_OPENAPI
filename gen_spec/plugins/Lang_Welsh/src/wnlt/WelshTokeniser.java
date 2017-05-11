/*
 *  WelshTokeniser.java
 *  This file is part of Welsh Natural Language Toolkit (WNLT)
 *  (see http://gate.ac.uk/), and is free software, licenced under 
 *  the GNU Library General Public License, Version 2, June 1991
 *  
 */
package wnlt;

import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.tokeniser.DefaultTokeniser;

import java.net.URL;

/**
 * This class extends the DefaultTokeniser class of ANNIE.
 * @author Andreas Vlachidis 20/03/2016
 * 
 */
@CreoleResource(name="Welsh Tokeniser", icon="welsh_tokenizer.png")
public class WelshTokeniser extends DefaultTokeniser {

	private static final long serialVersionUID = 1L;

  @Override
  @CreoleParameter(defaultValue = "resources/Tokeniser/WelshTokeniser.rules")
  public void setTokeniserRulesURL(URL tokeniserRulesURL) {
    super.setTokeniserRulesURL(tokeniserRulesURL);
  }

  @Override
  @CreoleParameter(defaultValue = "resources/Tokeniser/postprocess.jape")
  public void setTransducerGrammarURL(URL transducerGrammarURL) {
    super.setTransducerGrammarURL(transducerGrammarURL);
  }
}