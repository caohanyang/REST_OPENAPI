package com.hanyang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings {

	/*
	 * VERB AND URL 
	 */
	// in the case we don't have example http request, we need to globally search for the base url.
	public static boolean SEARCHBASE = true;
		
	// The things between Http verbs and Url:
	// "\\s", "(.*?)"
	public static String STUFFING = "\\s";	
	// for some url contains URL parameters
	// It will present URL in different tags, which causes whitespace
	public static String URLMIDDLE = "";
	public static String URLAFTER = " ";
	// "no", "yes"
	public static String REVERSE = "no";
	// "https", "http", "/"
	public static String MODE ="/";
	// first URL then parameters
	public static boolean U1P2 = true;
	// "del", "delete"
	public static String ABBREV_DELETE = "delete";
	
	
	/*
	 * RQUEST
	 */
	// The key word before the request
	// EXAMPLE REQUEST
	public static String REQKEY = "request";
	// \\s \\s(.*?)
	public static String REQMIDDLE = "\\s.{0,60}";
	// The request exists or not 
	// http \\{(.*?)\\} no
	public static String REQEXAMPLE = "\\{(.*?)\\}";
	
	
	/*
	 * RESPONSE 
	 */
	// The response exists or not 
	public static boolean RESEXAMPLE = true;
	// (example)|(response)
	public static String RESKEY = "response";
	
	
	/*
	 * PARAMETER 
	 */
	// The parameters exist or not 
	public static boolean EXISTPARA = true;
	// (parameter)|(argument)|(field)
	public static String PARAKEY = "(parameter)|(argument)|(field)";
		
	// parameter types
	// Required. The location of the parameter. 
	// Possible values are "query", "header", "path", "formData" or "body".     
	// for one api, the parameters can be "mix"
	public static String PARAIN = "query";
	// "table", "list"
	public static String TEMPLATE = "list";
	// "single", "multiple"
	public static String NUMBER = "multiple";

	
	// "no", "yes"
//	public static List<String> REVERSE = new ArrayList<String>(Arrays.asList("no"));
//	// "https", "http", "/"
//	public static List<String> MODE = new ArrayList<String>(Arrays.asList("/"));
//	// "table", "list"
//	public static List<String> TEMPLATE = new ArrayList<String>(Arrays.asList("list"));
//	// "single", "multiple"
//	public static List<String> NUMBER = new ArrayList<String>(Arrays.asList("multiple"));
//	// "del", "delete"
//	public static List<String> ABBREV_DELETE = new ArrayList<String>(Arrays.asList("delete"));
}
