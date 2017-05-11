/*
 * WelshPOSTagger.java This file is part of Welsh Natural Language Toolkit
 * (WNLT) (see http://gate.ac.uk/), and is free software, licenced under the GNU
 * Library General Public License, Version 2, June 1991
 */
package wnlt;

import gate.Resource;
import gate.creole.POSTagger;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;

/**
 * This class is a wrapper for the WNLT class HeppleCY which extends the the
 * Hepple's POS Tagger
 * 
 * @author Andreas Vlachidis 20/03/2016
 */
@CreoleResource(name = "Welsh POS Tagger", comment = "Mark Hepple's Brill-style POS tagger, adapted for Welsh", icon="welsh_pos.png")
public class WelshPOSTagger extends POSTagger {

  private static final long serialVersionUID = 1L;

  @Override
  public Resource init() throws ResourceInstantiationException {
    if(getLexiconURL() == null) { throw new ResourceInstantiationException(
      "NoURL provided for the lexicon!"); }
    if(getRulesURL() == null) { throw new ResourceInstantiationException(
      "No URL provided for the rules!"); }
    try {
      tagger = new wnlt.HeppleCY(getLexiconURL(), getRulesURL(), getEncoding());
    } catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
    return this;
  }

  @Override
  @CreoleParameter(defaultValue = "resources/postag/lexicon")
  public void setLexiconURL(URL newLexiconURL) {
    super.setLexiconURL(newLexiconURL);
  }
  
  @Override
  @CreoleParameter(defaultValue = "resources/postag/ruleset")
  public void setRulesURL(URL newRulesURL) {
    super.setRulesURL(newRulesURL);
  }
  
  @Override
  @CreoleParameter(defaultValue="UTF-8")
  public void setEncoding(String encoding) {
    super.setEncoding(encoding);
  }

}
