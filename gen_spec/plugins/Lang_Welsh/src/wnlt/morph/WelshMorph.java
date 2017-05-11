/*
 *  WelshMorph.java
 *  This file is part of Welsh Natural Language Toolkit (WNLT)
 *  (see http://gate.ac.uk/), and is free software, licenced under 
 *  the GNU Library General Public License, Version 2, June 1991
 *  
 */

package wnlt.morph;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.Factory.DuplicationContext;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.CustomDuplication;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.ResourceInstantiationException;
import gate.creole.Transducer;
import gate.creole.gazetteer.DefaultGazetteer;
import gate.creole.gazetteer.FlexibleGazetteer;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.event.ProgressListener;
import gate.event.StatusListener;
import gate.util.Benchmark;
import gate.util.Benchmarkable;
import gate.util.GateRuntimeException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import wnlt.LexiconCY;



/**
 * Description: This class is a wrapper for {@link wnlt.morph.Interpret},
 * the Morphological Analyzer. The class is based on and modifies for the 
 * purposes of the Welsh Natural Language Toolkit the Morph class
 * of the GATE Morphological analyser. 
 * 
 * @author Andreas Vlachidis 20/03/2016
 * 
 */

@CreoleResource(name = "Welsh Morphological Analyser",
        comment = "Morphological Analyzer of the Welsh Natural Language Toolkit", icon="welsh_lemmatiser.png")
public class WelshMorph
    extends AbstractLanguageAnalyser
    implements ProcessingResource, CustomDuplication, Benchmarkable {

  // note that this package could probably be simplified as the only modified
  // class from the original is Interpret so we may be able to just use the
  // existing classes, but for safety we currently use an entire copy, although
  // this may make bug fixing harder in the future
  
  
  private static final long serialVersionUID = 6964689654685956128L;

  /** File which contains rules to be processed */
  protected URL rulesFile;

  /** Instance of BaseWord class - Welsh Morpher */
  protected Interpret interpret;

  /** Feature Name that should be displayed for the root word */
  protected String rootFeatureName;

  /** Feature Name that should be displayed for the affix */
  protected String affixFeatureName;

  /** The name of the annotation set used for input */
  protected String annotationSetName;

  /** Boolean value that tells if parser should behave in caseSensitive mode */
  protected Boolean caseSensitive;
  
  /** Boolean value that checks if the required Part of Speech input is available  */
  protected Boolean considerPOSTag;
  
  /**
   * If this Morph PR is a duplicate of an existing PR, this property
   * will hold a reference to the original PR's Interpret instance.
   */
  protected Interpret existingInterpret;
  
  /** Lexicon of lemmas : read from an external file */ 
  protected LexiconCY lexicon;
  
  /** Path to the lexicon containing word lemmas*/
  public static final String
  TAG_LEXICON_URL_PARAMETER_NAME = "lexiconURL";
  
  /** Encoding of lexicon and gazetteer lists*/
  public static final String
  TAG_ENCODING_PARAMETER_NAME = "encoding";
  
  /** Post process transducer and gazeteer*/
  public static final String
  SPLIT_DOCUMENT_PARAMETER_NAME = "document";
  
  /** Name of input Annotation Set*/
  public static final String
  SPLIT_INPUT_AS_PARAMETER_NAME = "inputASName";
  
  /** Name of output Annotation Set*/
  public static final String
  SPLIT_OUTPUT_AS_PARAMETER_NAME = "outputASName";
  
  /** Path to gazetteer lists file*/
  public static final String
  SPLIT_GAZ_URL_PARAMETER_NAME = "gazetteerListsURL";
  
  /** Path to Post-processing JAPE transducer for mutation behaviour */
  public static final String
  SPLIT_TRANSD_URL_PARAMETER_NAME = "transducerURL";
  
  /** Path to validation JAPE transducer of proposed mutations*/
  public static final String
  MORPH_VALIDTRANSD_URL_PARAMETER_NAME = "validationTransducerURL";
	
  @RunTime
  @Optional
  @CreoleParameter(
    comment = "Throw and exception when there are none of the required input annotations",
    defaultValue = "true")  
  public void setFailOnMissingInputAnnotations(Boolean fail) {
    failOnMissingInputAnnotations = fail;
  }
  public Boolean getFailOnMissingInputAnnotations() {
    return failOnMissingInputAnnotations;
  }
  protected Boolean failOnMissingInputAnnotations = false;
  
  protected Logger logger = Logger.getLogger(this.getClass().getName());  
  
  /** Default Constructor */
  public WelshMorph() {
  }

  /**
   * This method creates the instance of the BaseWord - Welsh Morpher and
   * returns the instance of current class with different attributes and
   * the instance of BaseWord class wrapped into it. 
   * The method also instantiates the post-processing transducer,    
   * and mutation validation gazetteer and transducer  
   * @return Resource
   * @throws ResourceInstantiationException
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    interpret = new Interpret();
    if(existingInterpret != null) {
      interpret.init(existingInterpret);
    }
    else {
      if (rulesFile == null) {
        // no rule file is there, simply run the interpret to interpret it and
        throw new ResourceInstantiationException("\n\n No Rule File Provided");
      }
  
      fireStatusChanged("Reading Rule File...");
      // compile the rules
      interpret.init(rulesFile, getLexiconURL(), getEncoding());
      fireStatusChanged("Morpher created!");
      fireProcessFinished();
    }
    
    //create Transducer and Gazetteer
    FeatureMap params;
    FeatureMap features;

    params = Factory.newFeatureMap();
    if(gazetteerListsURL != null)
      params.put(DefaultGazetteer.DEF_GAZ_LISTS_URL_PARAMETER_NAME,
              gazetteerListsURL);
    params.put(DefaultGazetteer.DEF_GAZ_ENCODING_PARAMETER_NAME, encoding);
    params.put(DefaultGazetteer.DEF_GAZ_CASE_SENSITIVE_PARAMETER_NAME, "false");
     

    if (gazetteer == null) {
      //gazetteer
      fireStatusChanged("Creating the gazetteer");
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);

      gazetteer = (DefaultGazetteer)Factory.createResource(
              "gate.creole.gazetteer.DefaultGazetteer",
              params, features);
      gazetteer.setName("Gazetteer " + System.currentTimeMillis());
    }
    else {
      gazetteer.setParameterValues(params);
      gazetteer.reInit();
    }
    
  //create a flexible gazetteer
   params = Factory.newFeatureMap();
   List<String> inputFeatures = new ArrayList<String>();
   String rootFeature = "Token." + this.getRootFeatureName();
   inputFeatures.add(rootFeature);
   //Token.altLemma is used for additional cases of mutation e.g (f) from (m or b)  
   inputFeatures.add("Token.altLemma");
   params.put("inputFeatureNames", inputFeatures);
   params.put("gazetteerInst",gazetteer);
   if (flexigazetteer == null) {
	   	fireStatusChanged("Creating the Flexible Gazetteer");
	   	features = Factory.newFeatureMap();
	   	Gate.setHiddenAttribute(features, true);
    	flexigazetteer = (FlexibleGazetteer) Factory.createResource(
                "gate.creole.gazetteer.FlexibleGazetteer", params, features);
    	flexigazetteer.setName("FlexibleGazetteer " + System.currentTimeMillis());
    }
   else {
	   flexigazetteer.setParameterValues(params);
	   flexigazetteer.reInit();
   }
    
    fireProgressChanged(10);

    params = Factory.newFeatureMap();
    if(transducerURL != null)
      params.put(Transducer.TRANSD_GRAMMAR_URL_PARAMETER_NAME, transducerURL);
    params.put(Transducer.TRANSD_ENCODING_PARAMETER_NAME, encoding);

    if (transducer == null) {
      //transducer
      fireStatusChanged("Creating the JAPE transducer");
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);

      transducer = (Transducer)Factory.createResource(
              "gate.creole.Transducer",
              params, features);
      transducer.setName("Transducer " + System.currentTimeMillis());
    }
    else {
      transducer.setParameterValues(params);
      transducer.reInit();
    }
    
    params = Factory.newFeatureMap();
    if(transducerURL != null)
      params.put(Transducer.TRANSD_GRAMMAR_URL_PARAMETER_NAME, validationTransducerURL);
      params.put(Transducer.TRANSD_ENCODING_PARAMETER_NAME, encoding);
    
    if (validator == null) {
        //transducer
        fireStatusChanged("Creating the JAPE validator transducer");
        features = Factory.newFeatureMap();
        Gate.setHiddenAttribute(features, true);

        validator = (Transducer)Factory.createResource(
                "gate.creole.Transducer",
                params, features);
        validator.setName("Transducer validator " + System.currentTimeMillis());
      }
      else {
    	  validator.setParameterValues(params);
    	  validator.reInit();
      }
    
    fireProgressChanged(100);
    fireProcessFinished();
      
    return this;
  }

  /**
   * Method is executed after the init() method has finished its execution.
   * <BR>Method does the following operations:
   * <OL type="1">
   * <LI> creates the annotationSet
   * <LI> fetches word tokens from the document, one at a time
   * <LI> runs the morpher on each individual word token
   * <LI> finds the root and the affix for that word
   * <LI> adds them as features to the current token
   * @throws ExecutionException
   */
  
  @Override
  public void cleanup() {
    Factory.deleteResource(transducer);
  }
  
  @Override
  public void execute() throws ExecutionException {
    // lets start the progress and initialize the progress counter
    fireProgressChanged(0);

    // If no document provided to process throw an exception
    if (document == null) {
      fireProcessFinished();
      throw new GateRuntimeException("No document to process!");
    }

    // get the annotationSet name provided by the user, or otherwise use the
    // default method
    AnnotationSet inputAs = (annotationSetName == null ||
        annotationSetName.length() == 0) ?
        document.getAnnotations() :
        document.getAnnotations(annotationSetName);

    // Morpher requires tokenizer to be run before running the Morpher
    // Fetch tokens from the document
    AnnotationSet tokens = inputAs.get(TOKEN_ANNOTATION_TYPE);
    if (tokens == null || tokens.isEmpty()) {
      fireProcessFinished();
      if(failOnMissingInputAnnotations) {
        throw new ExecutionException("Either "+document.getName()+" does not have any contents or \n run the POS Tagger first and then Morpher");
      } else {
        Utils.logOnce(logger,Level.INFO,"Morphological analyser: either a document does not have any contents or run the POS Tagger first - see debug log for details.");
        logger.debug("No input annotations in document "+document.getName());
        return;
      }
      //javax.swing.JOptionPane.showMessageDialog(MainFrame.getInstance(), "Either "+document.getName()+" does not have any contents or \n run the POS Tagger first and then Morpher"); ;
      //return;
    }

    // create iterator to get access to each and every individual token
    Iterator<Annotation> tokensIter = tokens.iterator();

    // variables used to keep track on progress
    int tokenSize = tokens.size();
    int tokensProcessed = 0;
    int lastReport = 0;

    //lets process each token one at a time
    while (tokensIter != null && tokensIter.hasNext()) {
      Annotation currentToken = tokensIter.next();
      String tokenValue = (String) (currentToken.getFeatures().
                                    get(TOKEN_STRING_FEATURE_NAME));
      if(considerPOSTag != null && considerPOSTag.booleanValue() && !currentToken.getFeatures().containsKey(TOKEN_CATEGORY_FEATURE_NAME)) {
        fireProcessFinished();
        if(failOnMissingInputAnnotations) {
          throw new ExecutionException("please run the POS Tagger first and then Morpher");
        } else {
          Utils.logOnce(logger,Level.INFO,"Morphological analyser: no input annotations, run the POS Tagger first - see debug log for details.");
          logger.debug("No input annotations in document "+document.getName());
          return;
        }
        //javax.swing.JOptionPane.showMessageDialog(MainFrame.getInstance(), "please run the POS Tagger first and then Morpher"); ;
        //return;
      }

      String posCategory = (String) (currentToken.getFeatures().get(TOKEN_CATEGORY_FEATURE_NAME));
      if(posCategory == null) {
        posCategory = "*";
      }

      if(considerPOSTag == null || !considerPOSTag.booleanValue()) {
        posCategory = "*";
      }

      // run the Morpher
      if(!caseSensitive.booleanValue()) {
        tokenValue = tokenValue.toLowerCase();
      }

      String baseWord = interpret.runMorpher(tokenValue, posCategory);
      String affixWord = interpret.getAffix();

      // no need to add affix feature if it is null
      if (affixWord != null) {
        currentToken.getFeatures().put(affixFeatureName, affixWord);
      }
      // add the root word as a feature
      currentToken.getFeatures().put(rootFeatureName, baseWord);

      // measure the progress and update every after 100 tokens
      tokensProcessed++;
      if(tokensProcessed - lastReport > 100){
        lastReport = tokensProcessed;
        fireProgressChanged(tokensProcessed * 100 /tokenSize);
      }
    }
    //execute Transducer and Gazetteer
    interrupted = false;
    //set the runtime parameters
    FeatureMap params;
    if(inputASName != null && inputASName.equals("")) inputASName = null;
    if(outputASName != null && outputASName.equals("")) outputASName = null;
    try{
      fireProgressChanged(0);
      params = Factory.newFeatureMap();
      params.put(DefaultGazetteer.DEF_GAZ_DOCUMENT_PARAMETER_NAME, document);
      params.put(DefaultGazetteer.DEF_GAZ_ANNOT_SET_PARAMETER_NAME, inputASName);
      gazetteer.setParameterValues(params);

      params = Factory.newFeatureMap();
      params.put(Transducer.TRANSD_DOCUMENT_PARAMETER_NAME, document);
      params.put(Transducer.TRANSD_INPUT_AS_PARAMETER_NAME, inputASName);
      params.put(Transducer.TRANSD_OUTPUT_AS_PARAMETER_NAME, inputASName);
      transducer.setParameterValues(params);
    }catch(Exception e){
      throw new ExecutionException(e);
    }
    ProgressListener pListener = null;
    StatusListener sListener = null;
    fireProgressChanged(5);

    //run the gazetteer
    
    if(isInterrupted()) throw new ExecutionInterruptedException(
        "The execution of the \"" + getName() +
        "\" morphological analyser has been abruptly interrupted!");
    pListener = new IntervalProgressListener(5, 10);
    sListener = new StatusListener(){
      @Override
      public void statusChanged(String text){
        fireStatusChanged(text);
      }
    };   
     
    //run the transducer
    if(isInterrupted()) throw new ExecutionInterruptedException(
        "The execution of the \"" + getName() +
        "\" morphological analyser has been abruptly interrupted!");
    pListener = new IntervalProgressListener(11, 90);
    transducer.addProgressListener(pListener);
    transducer.addStatusListener(sListener);
    Benchmark.executeWithBenchmarking(transducer,
            Benchmark.createBenchmarkId("MorphTransducer",
                    getBenchmarkId()), this, null);
    transducer.removeProgressListener(pListener);
    transducer.removeStatusListener(sListener);
    // end execute Transducer and Gazetteer
    if(isInterrupted()) throw new ExecutionInterruptedException(
        "The execution of the \"" + getName() +
        "\" morphological analyser has been abruptly interrupted!");
    pListener = new IntervalProgressListener(50, 100);
    transducer.addProgressListener(pListener);
    transducer.addStatusListener(sListener);
    Benchmark.executeWithBenchmarking(transducer,
            Benchmark.createBenchmarkId("transducer",
                    getBenchmarkId()), this, null);
    transducer.removeProgressListener(pListener);
    transducer.removeStatusListener(sListener);
    //End Transducer Execute
    
    // Execute Flexi Gazetteer
    flexigazetteer.addProgressListener(pListener);
    flexigazetteer.addStatusListener(sListener);
    flexigazetteer.setDocument(document);
    flexigazetteer.execute();
    flexigazetteer.removeProgressListener(pListener);
    flexigazetteer.removeStatusListener(sListener);
    
    validator.setDocument(document);
    validator.execute();
    
    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

  /**
   * This method should only be called after init()
   * @param word
   * @return the rootWord
   */
  public String findBaseWord(String word, String cat) {
    return interpret.runMorpher(word, cat);
  }

  /**
   * This method should only be called after init()
   * @param word
   * @return the afix of the rootWord
   */
  public String findAffix(String word, String cat) {
    interpret.runMorpher(word, cat);
    return interpret.getAffix();
  }

  /**
   * Sets the rule file to be processed
   * @param rulesFile - rule File name to be processed
   */
  @CreoleParameter(comment = "File which defines rules for the morphological analysis", defaultValue = "resources/morph/default.rul")
  public void setRulesFile(URL rulesFile) {
    this.rulesFile = rulesFile;
  }

  /**
   * Returns the document under process
   */
  public URL getRulesFile() {
    return this.rulesFile;
  }

  /**
   * Returns the feature name that has been currently set to display the root
   * word
   */
  public String getRootFeatureName() {
    return rootFeatureName;
  }

  /**
   * Sets the feature name that should be displayed for the root word
   * @param rootFeatureName
   */
  @RunTime
  @CreoleParameter(comment="Name of the variable which shows the root word",defaultValue="lemma")
  public void setRootFeatureName(String rootFeatureName) {
    this.rootFeatureName = rootFeatureName;
  }

  /**
   * Returns the feature name that has been currently set to display the affix
   * word
   */
  public String getAffixFeatureName() {
    return affixFeatureName;
  }

  /**
   * Sets the feature name that should be displayed for the affix
   * @param affixFeatureName
   */
  @RunTime
  @CreoleParameter(comment="Name of the affix variable", defaultValue="affix")
  public void setAffixFeatureName(String affixFeatureName) {
    this.affixFeatureName = affixFeatureName;
  }

  /**
   * Returns the name of the AnnotationSet that has been provided to create
   * the AnnotationSet
   */
  public String getAnnotationSetName() {
    return annotationSetName;
  }

  /**
   * Sets the AnnonationSet name, that is used to create the AnnotationSet
   * @param annotationSetName
   */
  @RunTime
  @Optional
  @CreoleParameter(comment="The name of the annotation set used for input")
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }

  /**
   * A method which returns if the parser is in caseSenstive mode
   * @return a {@link Boolean} value.
   */
  public Boolean getCaseSensitive() {
    return this.caseSensitive;
  }

  /**
   * Sets the caseSensitive value, that is used to tell parser if it should
   * convert document to lowercase before parsing
   */
  @CreoleParameter(comment="If parser should be converted to lowercase first", defaultValue="false")
  public void setCaseSensitive(java.lang.Boolean value) {
    this.caseSensitive = value;
  }
  
  /**
   * A method which returns if Part of Speech input is present
   * @return a {@link Boolean} value.
   */
  public Boolean getConsiderPOSTag() {
    return this.considerPOSTag;
  }
  
  /**
   * Sets the result of checking for Part of Speech input availability
   */
  @RunTime
  @CreoleParameter(comment="If parser should consider POS Tag prior to running Morph", defaultValue="true")
  public void setConsiderPOSTag(Boolean value) {
    this.considerPOSTag = value;
  }
  
  /**
   * Only for use by the duplication mechanism.
   */
  public void setExistingInterpret(Interpret existingInterpret) {
    this.existingInterpret = existingInterpret;
  }

  /**
   * Duplicate this morpher, sharing the compiled regular expression
   * patterns and finite state machine with the duplicate.
   */
  @Override
  public Resource duplicate(DuplicationContext ctx)
          throws ResourceInstantiationException {
    String className = this.getClass().getName();
    String resName = this.getName();
    FeatureMap initParams = getInitParameterValues();
    initParams.put("existingInterpret", interpret);
    Resource res = Factory.createResource(className, initParams, this.getFeatures(), resName);
    res.setParameterValues(getRuntimeParameterValues());
    return res;
  }
  /**
   * Sets the location of the lexicon responsible for providing word lemmas
   */
  @Optional
  @CreoleParameter(comment="The URL to the lexicon file", defaultValue="resources/morph/lexicon")
  public void setLexiconURL(java.net.URL lexiconURL) {
    this.lexiconURL = lexiconURL;
  }
  public java.net.URL getLexiconURL() {
    return this.lexiconURL;
  }
  protected URL lexiconURL;
  
  /**
   * Sets the encoding of the lexicon responsible for providing word lemmas
   */
  @Optional
  @CreoleParameter(comment="The encoding used for lexicon", defaultValue="UTF-8")
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  public String getEncoding() {
	    return this.encoding;
	  }
  protected String encoding;
  
  /**
   * Notifies all the PRs in this controller that they should stop their
   * execution as soon as possible.
   */
  @Override
  public synchronized void interrupt(){
    interrupted = true;
    gazetteer.interrupt();
    transducer.interrupt();
  }
  
  /**
   * Sets the location of post-processing transducer
   */
  @CreoleParameter(defaultValue="resources/morph/grammar/postprocess.jape", comment="The URL to the custom Jape grammar file", suffixes="jape")
  public void setTransducerURL(java.net.URL newTransducerURL) {
    transducerURL = newTransducerURL;
  }
  
  /**
   * Returns the location of post-processing transducer
   */
  public java.net.URL getTransducerURL() {
    return transducerURL;
  }
  
  /**
   * Sets the location of mutations validation transducer
   */
  @CreoleParameter(defaultValue="resources/morph/grammar/validation-main.jape", comment="The URL to the custom Jape grammar file", suffixes="jape")
  public void setValidationTransducerURL(java.net.URL newValidationTransducerURL) {
	 validationTransducerURL = newValidationTransducerURL;
  }
  /**
   * Returns the location of mutations validation transducer
   */
  public java.net.URL getValidationTransducerURL() {
    return validationTransducerURL;
  }
  
  DefaultGazetteer gazetteer;
  Transducer transducer;
  Transducer validator;
  FlexibleGazetteer flexigazetteer;
  private java.net.URL transducerURL;
  private java.net.URL validationTransducerURL;
  private java.net.URL gazetteerListsURL;
  
  /**
   * Sets the location of gazetteer list used for validating mutations suggested by post-processing
   */
  @Optional
  @CreoleParameter(defaultValue="resources/morph/gazetteer/lists.def", comment="The URL to the custom list lookup definition file", suffixes="def")
  public void setGazetteerListsURL(java.net.URL newGazetteerListsURL) {
    gazetteerListsURL = newGazetteerListsURL;
  }
  public java.net.URL getGazetteerListsURL() {
    return gazetteerListsURL;
  }
  //end transducer and gazetteer 
  
  private String benchmarkId;
  
  /* (non-Javadoc)
   * @see gate.util.Benchmarkable#setBenchmarkId(java.lang.String)
   */
  @Override
  public void setBenchmarkId(String benchmarkId) {
    this.benchmarkId = benchmarkId;
  }
  
  /* (non-Javadoc)
   * @see gate.util.Benchmarkable#getBenchmarkId()
   */
  @Override
  public String getBenchmarkId() {
    if(benchmarkId == null) {
      return getName();
    }
    else {
      return benchmarkId;
    }
  }
  private String inputASName;
  private String outputASName;
}