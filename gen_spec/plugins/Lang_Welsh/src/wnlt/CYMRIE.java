/*
 *  CYMRIE.java
 *  This file is part of Welsh Natural Language Toolkit (WNLT)
 *  (see http://gate.ac.uk/), and is free software, licenced under 
 *  the GNU Library General Public License, Version 2, June 1991
 *  
 */
package wnlt;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;
import gate.creole.metadata.CreoleResource;

@CreoleResource(name = "CYMRIE", icon = "welsh.png", autoinstances = @AutoInstance(parameters = {
	@AutoInstanceParam(name="pipelineURL", value="resources/CYMRIE.xgapp"),
	@AutoInstanceParam(name="menu", value="Welsh")}),
    comment = "Welsh Information Extraction Application")
public class CYMRIE extends PackagedController {

  private static final long serialVersionUID = -4772002239682266707L;

}
