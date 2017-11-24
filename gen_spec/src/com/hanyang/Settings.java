package com.hanyang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings {

	// The things between Http verbs and Url:
	// "\\s", "(.*?)"
	public static String STUFFING = "\\s";	
	// The request exists or not 
	public static boolean REQEXAMPLE = true;
	// The key word before the request
	// EXAMPLE REQUEST
	public static String REQKEY = "EXAMPLE REQUEST";
	
	// The response exists or not 
	public static boolean RESEXAMPLE = true;
	// (example)|(response)
	public static String RESKEY = "response";
	
	
	// The parameters exist or not 
	public static boolean EXISTPARA = true;
	// (parameter)|(argument)|(field)
	public static String PARAKEY = "(parameter)|(argument)|(field)";
		
	// first URL then parameters
	public static boolean U1P2 = true;
	
	// parameter types
	// Required. The location of the parameter. 
	// Possible values are "query", "header", "path", "formData" or "body".     
	// for one api, the parameters can be "mix"
	public static String PARAIN = "query";
	
	// "https", "http", "null", "/"
	public static List<String> MODE = new ArrayList<String>(Arrays.asList("/"));
	// "no", "yes"
	public static List<String> REVERSE = new ArrayList<String>(Arrays.asList("no"));
	// "table", "list"
	public static List<String> TEMPLATE = new ArrayList<String>(Arrays.asList("list"));
	// "single", "multiple"
	public static List<String> NUMBER = new ArrayList<String>(Arrays.asList("multiple"));
	// "del", "delete"
	public static List<String> ABBREV_DELETE = new ArrayList<String>(Arrays.asList("del"));
}
