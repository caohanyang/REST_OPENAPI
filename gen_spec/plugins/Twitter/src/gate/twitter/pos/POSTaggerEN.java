/*
 * POSTaggerEN.java
 *
 * Copyright (c) 2016, The University of Sheffield. See the file COPYRIGHT.txt
 * in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 *
 * Ian Roberts, 24/05/2016
 */

package gate.twitter.pos;

import java.net.URL;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Factory.DuplicationContext;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.CustomDuplication;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.event.ProgressListener;
import gate.event.StatusListener;

/**
 * Transitional compatibility PR to allow saved applications created
 * with the GATE 8.0 version of TwitIE to continue operating in this
 * version. New applications should not use this class, they should
 * instantiate a Stanford Tagger PR directly with the appropriate model.
 */
@CreoleResource(name = "Twitter POS Tagger (deprecated)", comment = "Transitional compatibility PR for Twitter POS tagger - new applications should use the Stanford tagger directly", isPrivate = true)
public class POSTaggerEN extends AbstractLanguageAnalyser implements
                                                         CustomDuplication {

  private FeatureMap initParams;

  private FeatureMap runtimeParams;

  private AbstractLanguageAnalyser delegate;

  public Resource init() throws ResourceInstantiationException {
    if(delegate == null) {
      FeatureMap features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      delegate =
              (AbstractLanguageAnalyser)Factory.createResource(
                      "gate.stanford.Tagger", getInitParams(), features,
                      getName());
    }
    return this;
  }

  public void reInit() throws ResourceInstantiationException {
    delegate.setParameterValues(getInitParams());
    delegate.reInit();
  }

  public void execute() throws ExecutionException {
    try {
      delegate.setParameterValues(getRuntimeParams());
    } catch(ResourceInstantiationException e) {
      // TODO Auto-generated catch block
      throw new ExecutionException(e);
    }
    delegate.execute();
  }

  /**
   * Only for use by duplication mechanism.
   */
  public void setDelegate(AbstractLanguageAnalyser delegate) {
    this.delegate = delegate;
  }

  public AbstractLanguageAnalyser getDelegate() {
    return delegate;
  }

  /**
   * Only for use by duplication mechanism.
   */
  public void setInitParams(FeatureMap initParams) {
    this.initParams = initParams;
  }

  public FeatureMap getInitParams() {
    if(initParams == null) {
      initParams = Factory.newFeatureMap();
    }
    return initParams;
  }

  /**
   * Only for use by duplication mechanism.
   */
  public void setRuntimeParams(FeatureMap runtimeParams) {
    this.runtimeParams = runtimeParams;
  }

  public FeatureMap getRuntimeParams() {
    if(runtimeParams == null) {
      runtimeParams = Factory.newFeatureMap();
    }
    return runtimeParams;
  }

  // init parameters

  @CreoleParameter(comment = "Path to the tagger's model file", defaultValue = "resources/pos/gate-EN-twitter.model")
  public void setModelFile(URL modelFile) {
    getInitParams().put("modelFile", modelFile);
  }

  public URL getModelFile() {
    return (URL)getInitParams().get("modelFile");
  }

  // runtime parameters

  @Optional
  @RunTime
  @CreoleParameter
  public void setInputASName(String inputASName) {
    getRuntimeParams().put("inputASName", inputASName);
  }

  public String getInputASName() {
    return (String)getRuntimeParams().get("inputASName");
  }

  @Optional
  @RunTime
  @CreoleParameter
  public void setOutputASName(String outputASName) {
    getRuntimeParams().put("outputASName", outputASName);
  }

  public String getOutputASName() {
    return (String)getRuntimeParams().get("outputASName");
  }

  @RunTime
  @CreoleParameter(defaultValue = "true")
  public void setFailOnMissingInputAnnotations(Boolean fail) {
    getRuntimeParams().put("failOnMissingInputAnnotations", fail);
  }

  public Boolean getFailOnMissingInputAnnotations() {
    return (Boolean)getRuntimeParams().get("failOnMissingInputAnnotations");
  }

  @RunTime
  @CreoleParameter(defaultValue = "true")
  public void setPosTagAllTokens(Boolean tag) {
    getRuntimeParams().put("posTagAllTokens", tag);
  }

  public Boolean getPosTagAllTokens() {
    return (Boolean)getRuntimeParams().get("posTagAllTokens");
  }

  @RunTime
  @CreoleParameter(defaultValue = "Token")
  public void setBaseTokenAnnotationType(String type) {
    getRuntimeParams().put("baseTokenAnnotationType", type);
  }

  public String getBaseTokenAnnotationType() {
    return (String)getRuntimeParams().get("baseTokenAnnotationType");
  }

  @RunTime
  @CreoleParameter(defaultValue = "Sentence")
  public void setBaseSentenceAnnotationType(String type) {
    getRuntimeParams().put("baseSentenceAnnotationType", type);
  }

  public String getBaseSentenceAnnotationType() {
    return (String)getRuntimeParams().get("baseSentenceAnnotationType");
  }

  @RunTime
  @CreoleParameter(defaultValue = "Token")
  public void setOutputAnnotationType(String type) {
    getRuntimeParams().put("outputAnnotationType", type);
  }

  public String getOutputAnnotationType() {
    return (String)getRuntimeParams().get("outputAnnotationType");
  }

  @RunTime
  @CreoleParameter(defaultValue = "true")
  public void setUseExistingTags(Boolean tag) {
    getRuntimeParams().put("useExistingTags", tag);
  }

  public Boolean getUseExistingTags() {
    return (Boolean)getRuntimeParams().get("useExistingTags");
  }

  public void setCorpus(Corpus c) {
    delegate.setCorpus(c);
  }

  public Corpus getCorpus() {
    return delegate.getCorpus();
  }

  public void setDocument(Document doc) {
    delegate.setDocument(doc);
  }

  public Document getDocument() {
    return delegate.getDocument();
  }

  // listener registration

  @Override
  public synchronized void removeStatusListener(StatusListener l) {
    if(delegate != null) {
      delegate.removeStatusListener(l);
    }
  }

  @Override
  public synchronized void addStatusListener(StatusListener l) {
    if(delegate != null) {
      delegate.addStatusListener(l);
    }
  }

  @Override
  public synchronized void addProgressListener(ProgressListener l) {
    if(delegate != null) {
      delegate.addProgressListener(l);
    }
  }

  @Override
  public synchronized void removeProgressListener(ProgressListener l) {
    if(delegate != null) {
      delegate.removeProgressListener(l);
    }
  }

  @Override
  public Resource duplicate(DuplicationContext ctx)
          throws ResourceInstantiationException {
    FeatureMap newParams = Factory.newFeatureMap();
    newParams.put("initParams", Factory.duplicate(getInitParams(), ctx));
    newParams.put("runtimeParams", Factory.duplicate(getRuntimeParams(), ctx));
    newParams.put("delegate", Factory.duplicate(delegate, ctx));
    FeatureMap newFeatures = Factory.duplicate(getFeatures(), ctx);
    return Factory.createResource(POSTaggerEN.class.getName(), newParams,
            newFeatures, getName());
  }

}
