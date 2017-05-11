/*
 * Copyright (c) 1995-2016, The University of Sheffield. See the file
 * COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 */

package danish;

import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.tokeniser.DefaultTokeniser;

@CreoleResource(name = "Danish Tokeniser", comment = "PAROLE tokeniser, for Danish")
public class DanishTokeniser extends DefaultTokeniser {

  private static final long serialVersionUID = 150304758902960407L;
  
  @Override
  @CreoleParameter(defaultValue="resources/tokeniser/danish.jape")
  public void setTransducerGrammarURL(java.net.URL transducerGrammarURL) {
    super.setTransducerGrammarURL(transducerGrammarURL);
  }
}