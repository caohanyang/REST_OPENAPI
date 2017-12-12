package com.hanyang;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import gate.util.Out;

public class Settings {
	
	/*
	 * VERB AND URL 
	 */
	// in the case we don't have example http request, we need to globally search for the base url.
	public static boolean SEARCHBASE = true;
	// in case it may contain two different prefix, two different api.
    //	api.twitter.com/1.1
	public static String URLBASE = "";
		
	// The things between Http verbs and Url:
	// "\\s" \\s.{0,60} "(.*?)"
	public static String STUFFING = "\\s";	
	// for some url contains URL parameters
	// It will present URL in different tags, which causes whitespace
	// " " ""
	public static String URLMIDDLE = "";
	public static String URLAFTER = " ";
	// path template in the url
	// :id  <id> {id} no
	public static String URLTEMPLATE = "no";
	
	// "no", "yes"
	public static String REVERSE = "no";
	
	// exist the verb or not
	// no yes
	public static String EXISTVERB = "no";
	public static String URLKEY= "";
	public static String VERBKEY= "";
	
	// "https://", "http://", "/", "null"
	public static String MODE ="/";
	
	// "del", "delete"
	public static String ABBREV_DELETE = "delete";
	
	
	/*
	 * RQUEST
	 */
	// The key word before the request
	// "EXAMPLE REQUEST"  "" "no"
	public static String REQKEY = "Example";
	// \\s \\s.{0,60} "" (.*?)
	public static String REQMIDDLE = "\\s.{0,60}";
	// The request exists or not 
	// http \\{(.*?)\\} no curl  ((\\{)|(\\[)){1}(.*?)((\\})|(\\])){1}
	public static String REQEXAMPLE = "curl";
	
	// default true
	public static Boolean URL1REQ2 = true;
	
	// pre code b a p
	public static String REQTEMPLATE = "pre";
	
	/*
	 * RESPONSE 
	 */
	
	// (example)|(response)  ""  "no"
	public static String RESKEY = "Example Response";
	// \\s \\s.{0,10} \\s.{0,100} ""
	// 1.
	public static String RESMIDDLE = "\\s.{0,30}";
	// default true
	public static Boolean URL1RES2 = true;
	//  pre code span
	public static String RESTEMPLATE = "pre";
	
	// The response exists or not 
	// ((\\{)|(\\[)){1}(.*?)((\\})|(\\])){1}
	public static String RESEXAMPLE = "(\\<)(.*?)(\\>)";
	
	
	/*
	 * PARAMETER 
	 */
	// The parameters exist or not 
	public static boolean EXISTPARA = true;
	// (parameter)|(argument)|(field)
	// sometimes not common "Query Parameters" "url Parameters"
	// choose the last common one
	// (parameter)|(argument)|(field)  or choose the first element Name
	public static String PARAKEY = "(parameter)|(argument)|(field)|(parameters)|(arguments)|(fields)";
	// first URL then parameters
	public static boolean URL1PARA2 = true;	
	
	public static String PARAMIDDLE= "13";
	// parameter types
	// Required. The location of the parameter. 
	// Possible values are "query", "header", "path", "formData" or "body".     
	// for one api, the parameters can be "mix"
	public static String PARAIN = "query";
	// "table", "list"
	public static String TEMPLATE = "list";
	// "single", "multiple"
	public static String NUMBER = "multiple";

	
	//write property file
    public static void writeProperties(String api){
        Properties properties=new Properties();
        try {
            OutputStream outputStream=new FileOutputStream( api + ".config");
            properties.setProperty("SEARCHBASE", Boolean.toString(SEARCHBASE));
            properties.setProperty("STUFFING", STUFFING);    
            properties.setProperty("URLBASE",URLBASE );
            properties.setProperty("URLMIDDLE",URLMIDDLE );
            properties.setProperty("URLAFTER", URLAFTER);
            properties.setProperty("URLTEMPLATE", URLTEMPLATE);
            properties.setProperty("REVERSE",REVERSE);
            properties.setProperty("MODE", MODE);
            properties.setProperty("URL1PARA2", Boolean.toString(URL1PARA2));
            properties.setProperty("URL1REQ2", Boolean.toString(URL1REQ2));
            properties.setProperty("URL1RES2", Boolean.toString(URL1RES2));
            properties.setProperty("ABBREV_DELETE", ABBREV_DELETE);
            
            properties.setProperty("EXISTVERB", EXISTVERB);
            properties.setProperty("URLKEY", URLKEY);
            properties.setProperty("VERBKEY", VERBKEY);
            
            
            properties.setProperty("REQKEY", REQKEY);
            properties.setProperty("REQMIDDLE", REQMIDDLE);
            properties.setProperty("REQEXAMPLE", REQEXAMPLE);
            properties.setProperty("REQTEMPLATE", REQTEMPLATE);
            
            properties.setProperty("RESEXAMPLE", RESEXAMPLE);
            properties.setProperty("RESKEY", RESKEY);
            properties.setProperty("RESTEMPLATE", RESTEMPLATE);
            properties.setProperty("RESMIDDLE", RESMIDDLE);
            
            properties.setProperty("EXISTPARA", Boolean.toString(EXISTPARA));
            properties.setProperty("PARAKEY", PARAKEY);
            properties.setProperty("PARAMIDDLE", PARAMIDDLE);
            properties.setProperty("PARAIN", PARAIN);
            properties.setProperty("TEMPLATE", TEMPLATE);
            properties.setProperty("NUMBER", NUMBER);
            properties.store(outputStream, api);
            outputStream.close();

            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    // get property file 
    public static void getPropertiesReader(String api){
 
    	Properties properties = new Properties();  
  
        try {
        	FileInputStream fis =  new FileInputStream(api + ".config"); 
            properties.load(fis);
            setSEARCHBASE(Boolean.valueOf(properties.getProperty("SEARCHBASE")));
            setSTUFFING(properties.getProperty("STUFFING"));
            setURLBASE(properties.getProperty("URLBASE"));
            setURLMIDDLE(properties.getProperty("URLMIDDLE"));
            setURLAFTER(properties.getProperty("URLAFTER"));
            setURLTEMPLATE(properties.getProperty("URLTEMPLATE"));
            setREVERSE(properties.getProperty("REVERSE"));
            setMODE(properties.getProperty("MODE"));
            setURL1PARA2(Boolean.valueOf(properties.getProperty("URL1PARA2")));
            setURL1REQ2(Boolean.valueOf(properties.getProperty("URL1REQ2")));
            setURL1RES2(Boolean.valueOf(properties.getProperty("URL1RES2")));
            setABBREV_DELETE(properties.getProperty("ABBREV_DELETE"));
            
            
            setEXISTVERB(properties.getProperty("EXISTVERB"));
            setURLKEY(properties.getProperty("URLKEY"));
            setVERBKEY(properties.getProperty("VERBKEY"));
            
            
            setREQKEY(properties.getProperty("REQKEY"));
            setREQMIDDLE(properties.getProperty("REQMIDDLE"));
            setREQEXAMPLE(properties.getProperty("REQEXAMPLE"));
            setREQTEMPLATE(properties.getProperty("REQTEMPLATE"));
            
            setRESEXAMPLE(properties.getProperty("RESEXAMPLE"));
            setRESKEY(properties.getProperty("RESKEY"));
            setRESTEMPLATE(properties.getProperty("RESTEMPLATE"));
            setRESMIDDLE(properties.getProperty("RESMIDDLE"));
            
            setEXISTPARA(Boolean.valueOf(properties.getProperty("EXISTPARA")));
            setPARAMIDDLE(properties.getProperty("PARAMIDDLE"));
            setPARAIN(properties.getProperty("PARAIN"));
            setTEMPLATE(properties.getProperty("TEMPLATE"));
            setNUMBER(properties.getProperty("NUMBER"));
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isSEARCHBASE() {
		return SEARCHBASE;
	}

	public static void setSEARCHBASE(boolean sEARCHBASE) {
		SEARCHBASE = sEARCHBASE;
	}

	public static String getSTUFFING() {
		return STUFFING;
	}

	public static void setSTUFFING(String sTUFFING) {
		STUFFING = sTUFFING;
	}

	public static String getURLMIDDLE() {
		return URLMIDDLE;
	}

	public static void setURLMIDDLE(String uRLMIDDLE) {
		URLMIDDLE = uRLMIDDLE;
	}

	public static String getURLAFTER() {
		return URLAFTER;
	}

	public static void setURLAFTER(String uRLAFTER) {
		URLAFTER = uRLAFTER;
	}

	public static String getREVERSE() {
		return REVERSE;
	}

	public static void setREVERSE(String rEVERSE) {
		REVERSE = rEVERSE;
	}

	public static String getMODE() {
		return MODE;
	}

	public static void setMODE(String mODE) {
		MODE = mODE;
	}

	public static String getABBREV_DELETE() {
		return ABBREV_DELETE;
	}

	public static void setABBREV_DELETE(String aBBREV_DELETE) {
		ABBREV_DELETE = aBBREV_DELETE;
	}

	public static String getREQKEY() {
		return REQKEY;
	}

	public static void setREQKEY(String rEQKEY) {
		REQKEY = rEQKEY;
	}

	public static String getREQMIDDLE() {
		return REQMIDDLE;
	}

	public static void setREQMIDDLE(String rEQMIDDLE) {
		REQMIDDLE = rEQMIDDLE;
	}

	public static String getREQEXAMPLE() {
		return REQEXAMPLE;
	}

	public static void setREQEXAMPLE(String rEQEXAMPLE) {
		REQEXAMPLE = rEQEXAMPLE;
	}

	public static String getRESKEY() {
		return RESKEY;
	}

	public static void setRESKEY(String rESKEY) {
		RESKEY = rESKEY;
	}

	public static boolean isEXISTPARA() {
		return EXISTPARA;
	}

	public static void setEXISTPARA(boolean eXISTPARA) {
		EXISTPARA = eXISTPARA;
	}

	public static String getPARAKEY() {
		return PARAKEY;
	}

	public static void setPARAKEY(String pARAKEY) {
		PARAKEY = pARAKEY;
	}

	public static String getPARAIN() {
		return PARAIN;
	}

	public static void setPARAIN(String pARAIN) {
		PARAIN = pARAIN;
	}

	public static String getTEMPLATE() {
		return TEMPLATE;
	}

	public static void setTEMPLATE(String tEMPLATE) {
		TEMPLATE = tEMPLATE;
	}

	public static String getNUMBER() {
		return NUMBER;
	}

	public static void setNUMBER(String nUMBER) {
		NUMBER = nUMBER;
	}
	
	public static String getURLTEMPLATE() {
		return URLTEMPLATE;
	}

	public static void setURLTEMPLATE(String uRLTEMPLATE) {
		URLTEMPLATE = uRLTEMPLATE;
	}
	
	public static Boolean getURL1REQ2() {
		return URL1REQ2;
	}

	public static void setURL1REQ2(Boolean uRL1REQ2) {
		URL1REQ2 = uRL1REQ2;
	}

	public static String getREQTEMPLATE() {
		return REQTEMPLATE;
	}

	public static void setREQTEMPLATE(String rEQTEMPLATE) {
		REQTEMPLATE = rEQTEMPLATE;
	}

	public static Boolean getURL1RES2() {
		return URL1RES2;
	}

	public static void setURL1RES2(Boolean uRL1RES2) {
		URL1RES2 = uRL1RES2;
	}

	public static String getRESTEMPLATE() {
		return RESTEMPLATE;
	}
	
	public static String getRESEXAMPLE() {
		return RESEXAMPLE;
	}

	public static void setRESEXAMPLE(String rESEXAMPLE) {
		RESEXAMPLE = rESEXAMPLE;
	}

	public static String getRESMIDDLE() {
		return RESMIDDLE;
	}

	public static void setRESMIDDLE(String rESMIDDLE) {
		RESMIDDLE = rESMIDDLE;
	}

	public static void setRESTEMPLATE(String rESTEMPLATE) {
		RESTEMPLATE = rESTEMPLATE;
	}

	public static boolean isURL1PARA2() {
		return URL1PARA2;
	}

	public static void setURL1PARA2(boolean uRL1PARA2) {
		URL1PARA2 = uRL1PARA2;
	}
	
	public static String getURLBASE() {
		return URLBASE;
	}

	public static void setURLBASE(String uRLBASE) {
		URLBASE = uRLBASE;
	}

	public static String getEXISTVERB() {
		return EXISTVERB;
	}

	public static void setEXISTVERB(String eXISTVERB) {
		EXISTVERB = eXISTVERB;
	}

	public static String getURLKEY() {
		return URLKEY;
	}

	public static void setURLKEY(String uRLKEY) {
		URLKEY = uRLKEY;
	}

	public static String getVERBKEY() {
		return VERBKEY;
	}

	public static void setVERBKEY(String vERBKEY) {
		VERBKEY = vERBKEY;
	}

	public static String getPARAMIDDLE() {
		return PARAMIDDLE;
	}

	public static void setPARAMIDDLE(String pARAMIDDLE) {
		PARAMIDDLE = pARAMIDDLE;
	}
	
}
