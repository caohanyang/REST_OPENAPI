package com.hanyang;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.json.JSONException;
import org.json.JSONObject;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.Out;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ExtractInformation {

	/** The Corpus Pipeline application to contain ANNIE */
	 // corpus/www.instagram.com dev.twitter.com www.twilio.com www.youtube.com
	// www.flickr.com
	private static String API_NAME = "google";
	private static String API_FOLDER = "developers.google.com";
	
	private static String FilteredSet_PATH = "FilteredSet/" + API_FOLDER;
	private static String CompareSet_PATH = "CompareSet/" + API_NAME;
	// "https", "http", "null"
	private static List<String> MODE = new ArrayList<String>(Arrays.asList("https", "http", "null"));
	// "table", "list"
	private static List<String> TEMPLATE = new ArrayList<String>(Arrays.asList("table", "list"));
	// "single", "multiple"
	private static List<String> NUMBER = new ArrayList<String>(Arrays.asList("single", "multiple"));
	// "del", "delete"
	private static List<String> ABBREV_DELETE = new ArrayList<String>(Arrays.asList("del", "delete"));

	public static void main(String[] args) throws GateException, JSONException, IOException {
		// https://developers.google.com/youtube/v3 https://developers.google.com/youtube/v3/docs
		// https://cloud.google.com/translate  https://cloud.google.com/translate/docs/reference
		if (args.length > 0){
			API_FOLDER = args[0].split("//")[1].split("/")[0];
			API_NAME = args[0].split("//")[1].split("\\.")[1];
			// google have several APIs
			if (API_NAME.contains("google")) {
				API_NAME = args[0].split("//")[1].split("/")[1];
			}
			FilteredSet_PATH = "../FilteredSet/" + API_FOLDER;
			CompareSet_PATH = "../CompareSet/" + API_NAME;
        }
		
		// init gate
		Gate.init();

		// 1. create corpus
		File folder = new File(FilteredSet_PATH);
		File[] listFiles = folder.listFiles();
		new File(CompareSet_PATH).mkdirs();
		File compareSet = new File(CompareSet_PATH);
		
		// 2. generate swagger according to pattern
		for (Iterator<String> sIterator = MODE.iterator(); sIterator.hasNext();) {
			String mode = sIterator.next();

			for (Iterator<String> tIterator = TEMPLATE.iterator(); tIterator.hasNext();) {
				String template = tIterator.next();

				for (Iterator<String> nIterator = NUMBER.iterator(); nIterator.hasNext();) {
					String number = nIterator.next();

					for (Iterator<String> dIterator = ABBREV_DELETE.iterator(); dIterator.hasNext();) {
						String abbrev = dIterator.next();
						// generate different swagger
						generateOpenAPI(listFiles, mode, template, number, abbrev);
						System.gc();
					}
				}
			}
		}

		// 3. compare the json files and select the final one.
		selectOpenAPI(compareSet);

	}

	public static void generateOpenAPI(File[] listFiles, String mode, String template, String number,
			String abbrev) throws ResourceInstantiationException, JSONException, IOException, MalformedURLException {
		// 2. initial the specification
		GenerateMain mainObject = new GenerateMain();
		JSONObject openAPI = mainObject.generateStructure();
		ProcessBaseUrl processBa = new ProcessBaseUrl();
		String baseUrl = null;
		// 3. different mode
		// if it's null mode, find the common base url first
		if (mode == "null") {
		   baseUrl = processBa.searchBaseUrl(listFiles, API_NAME);
		   Out.prln(baseUrl);
		}
		
		// 4. check each html file
		for (int i = 0; i < listFiles.length; i++) {
			// print the file name
			Out.prln("=============File name=======================");
			Out.prln(listFiles[i].getPath());
			String type = new Tika().detect(listFiles[i].getPath());
			// only detect html
			if (type.equals("text/html")) {
				executeFile(listFiles[i].getPath(), openAPI, mode, template, number, abbrev, baseUrl);
			}
		}
		
		// 4. prune swagger
		openAPI = processBa.handleBaseUrl(openAPI, mode, baseUrl);

		// 5. write to file
		writeOpenAPI(openAPI, mode, template, number, abbrev);

	}

	public static void executeFile(String path, JSONObject swagger, String scheme, String template,
			String number, String abbrev, String baseUrl) throws ResourceInstantiationException, JSONException, IOException {
		URL u = Paths.get(path).toUri().toURL();
		FeatureMap params = Factory.newFeatureMap();
		params.put("sourceUrl", u);
		Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

		// 2. get all text
		DocumentContent textAll = doc.getContent();
		// Out.prln(textAll);
		// 3. initial swagger
		ProcessMethod processMe = new ProcessMethod();
		processMe.generateDefault(swagger);
		
		if (scheme == "null") {
			nullMode(swagger, template, number, abbrev, doc, textAll, processMe, baseUrl, API_NAME);
		} else {
			httpMode(swagger, scheme, template, number, abbrev, doc, textAll, processMe);
		}
        

	}

	private static void nullMode(JSONObject swagger, String template, String number, String abbrev, Document doc,
			DocumentContent textAll, ProcessMethod processMe, String baseUrl, String aPI_NAME) throws JSONException {
		String strAll = textAll.toString();
		String actionStr = null, urlString = null;
		List<JSONObject> infoJson = new ArrayList<JSONObject>();
		Out.prln(infoJson);
		// 1 get original markups
		doc.setMarkupAware(true);

		AnnotationSet annoOrigin = doc.getAnnotations("Original markups");
		AnnotationSet annoH1 = annoOrigin.get("h1");
		
		Iterator<Annotation> urlIter = annoH1.iterator();
		while (urlIter.hasNext()) {
			Annotation anno = (Annotation) urlIter.next();
			String urlText = gate.Utils.stringFor(doc, anno);
			JSONObject sectionJson = new JSONObject();
			
			if (processMe.isUrl(urlText, anno, strAll, aPI_NAME)) {
				urlString = urlText;
				actionStr = processMe.findAction(urlString);
				Out.prln("==========Url Action=================");
				Out.prln(urlText + "  " + actionStr);
				
				int location = anno.getStartNode().getOffset().intValue();
				//set url/action in the json
				JSONObject acJson = new JSONObject();
				acJson.put(actionStr, location);
				sectionJson.put("action", acJson);
				JSONObject urJson = new JSONObject();
				urJson.put(urlString, location);
				sectionJson.put("url", urJson);
			}
			
			if (sectionJson.length() > 0) {
				infoJson.add(sectionJson);
			}
		}
		
		Out.prln("---------INFO JSON-------");
		Out.prln(infoJson.toString());
		
		if (template == "table") {
			// 5.2 get table annotation
			AnnotationSet annoTable = annoOrigin.get("table");
			handleTemplate(swagger, number, template, doc, processMe, strAll, infoJson, annoTable);
		} else if (template == "list") {
			AnnotationSet annoList = annoOrigin.get("dl");
			// 5.3 get list annotation
			handleTemplate(swagger, number, template, doc, processMe, strAll, infoJson, annoList);
		}
		
	}

	private static void httpMode(JSONObject swagger, String scheme, String template, String number, String abbrev,
			Document doc, DocumentContent textAll, ProcessMethod processMe) throws JSONException {
		// 4.1 search for the GET https
		String strAll = textAll.toString();
		// Fix 1: suppose the len(content between get and http) < 40 + "://"
		String regexAll = "(?si)((get)|(post)|(" + abbrev + ")|(put)|(patch)){1}\\s(.*?)" + scheme;
		Pattern p = Pattern.compile(regexAll);
		Matcher matcher = p.matcher(strAll);
		String actionStr = null, urlString = null;
		List<JSONObject> infoJson = new ArrayList<JSONObject>();

		while (matcher.find()) {
			JSONObject sectionJson = new JSONObject();
			int startIndex = matcher.start();
			Out.prln("start: " + matcher.start());
			Out.prln("end:   " + matcher.end());
			// Fix 2: suppose the URL length < 100
			String matchStr = strAll.substring(matcher.start(), matcher.end() + 100);
			Out.prln("========matchSTR==============");
			Out.prln(matchStr);

			// match reversed action
			Pattern action = Pattern.compile(
					"((teg)|(tsop)|(" + new StringBuilder(abbrev).reverse().toString() + ")|(tup)|(hctap))",
					Pattern.CASE_INSENSITIVE);
			// match the reversed string, from right to left
			Matcher matcherAction = action.matcher(new StringBuilder(matchStr).reverse());
			// find the first match
			if (matcherAction.find()) {
				// find the action which is nearest to http
				Out.prln("matchStart： " + matcherAction.start());
				// Out.prln("==========real Action============");
				int acOffset = matchStr.length() - matcherAction.start() - 4;
				int acLocation = startIndex + acOffset;
				// Out.prln(strAll.substring(acLocation, matcher.end()));
				Out.prln("==========REST Action============");
				actionStr = new StringBuilder(matcherAction.group(1)).reverse().toString();
				JSONObject acJson = new JSONObject();
				acJson.put(actionStr, acLocation);
				sectionJson.put("action", acJson);

				Out.prln(actionStr);
			}
			// match endpoint
			String regexHttp = scheme;
			Pattern endpoint = Pattern.compile(regexHttp, Pattern.CASE_INSENSITIVE);
			Matcher endpointMatcher = endpoint.matcher(matchStr);
			if (endpointMatcher.find()) {
				Out.prln("urlStart： " + endpointMatcher.start());
				int uLocation = startIndex + endpointMatcher.start();
				urlString = matchStr.substring(endpointMatcher.start()).split("\n")[0].trim();
				// handle url, make it short and clean
				urlString = processMe.cleanUrl(urlString);

				// Out.prln("==========real ADDRESS============");
				// Out.prln(strAll.substring(uLocation, uLocation + 100));
				Out.prln("==========URL ADDRESS============");
				Out.prln(urlString);
				JSONObject urJson = new JSONObject();
				urJson.put(urlString, uLocation);
				sectionJson.put("url", urJson);
			}

			// Write into swagger
			// After matching table, we write url/action into swagger
			// processMe.addUrl(swagger, urlString, actionStr);
			infoJson.add(sectionJson);
		}

		// 4.2 handle info json
		// solve the conflicts: one URL with two action
		Out.prln("---------INFO JSON-------");
		Out.prln(infoJson.toString());

		// 5.1 get original markups
		doc.setMarkupAware(true);

		AnnotationSet annoOrigin = doc.getAnnotations("Original markups");
       
		// 6. if info json != null
		if (infoJson.isEmpty()) {
			Out.prln("sdfasf");
		}
		if (template == "table") {
			// 5.2 get table annotation
			AnnotationSet annoTable = annoOrigin.get("table");
			handleTemplate(swagger, number, template, doc, processMe, strAll, infoJson, annoTable);
		} else if (template == "list") {
			AnnotationSet annoList = annoOrigin.get("dl");
			// 5.3 get list annotation
			handleTemplate(swagger, number, template, doc, processMe, strAll, infoJson, annoList);
		}
	}

	private static void handleTemplate(JSONObject swagger, String templateNum, String template, Document doc,
			ProcessMethod processMe, String strAll, List<JSONObject> infoJson, AnnotationSet annoTemplate)
			throws JSONException {
		// 5.3 for each page, set findParaTable = False
		boolean findParaTemplate = false;
		// 5.3.1 Test if the page contains multiple parameter table or not
		Iterator<Annotation> testIter = annoTemplate.iterator();
		String templateNumber = templateNum;
		// int numTemplate = 0;
//		while (testIter.hasNext()) {
//			Annotation anno = (Annotation) testIter.next();
//			String templateText = gate.Utils.stringFor(doc, anno);
//			ProcessParameter processPa = new ProcessParameter();
//			if (processPa.isParaTemplate(templateText, anno, strAll)) {
//				// numTemplate++;
//			}
//		}
    
		Out.prln(findParaTemplate);
		// if (numTemplate > 1) {
		// // more than one parameter template in the page
		// multiTemplate = "multiple";
		// }

		// 5.3.2 handle the template context
		Iterator<Annotation> templateIter = annoTemplate.iterator();
		while (templateIter.hasNext()) {
			Annotation anno = (Annotation) templateIter.next();
			String templateText = gate.Utils.stringFor(doc, anno);
			ProcessParameter processPa = new ProcessParameter();
			if (processPa.isParaTemplate(templateText, anno, strAll)) {
				findParaTemplate = true;
				Out.prln("==========TABLE or LIST=================");
				Out.prln(templateText);
				processPa.generateParameter(swagger, templateText, strAll, infoJson, anno, doc, processMe,
						templateNumber, template);
			}
		}

		// 5.4 In case of the method doesn't have parameter
		// fix then.....
		if (!findParaTemplate) {
			// can't find table
			if (!infoJson.isEmpty()) {
				// In case of the method doesn't have parameter
				// add the noPara url
				processMe.addNoParaUrl(swagger, strAll, infoJson);
			}
		}
	}

	public static void writeOpenAPI(JSONObject swagger, String scheme, String template, String number, String abbrev)
			throws IOException {
		
		// Print pretty swagger
		String fileName = scheme + "_" + template + "_" + number + "_" + abbrev + ".json";
		writeFile(swagger.toString(), fileName);
	}

	public static void writeFile(String swagger, String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(CompareSet_PATH + "/" + fileName);
		String swString = swagger.toString();
		JsonParser parser = new JsonParser();
		JsonElement jelement = parser.parse(swString);

		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		String prettyJson = gson.toJson(jelement);

		fileWriter.write(prettyJson);
		fileWriter.close();
	}

	public static void selectOpenAPI(File folder) throws IOException {
		File[] listNew = folder.listFiles();
		String finalName = "OpenAPI.json";
		File finalFile = new File(CompareSet_PATH + "/" + finalName);

		for (int i = 0; i < listNew.length; i++) {
			String type = new Tika().detect(listNew[i].getPath());
			// only detect .json
			if (type.equals("application/json")) {
				// ignore final file
				if (listNew[i].getName() != finalName) {
					// select the maximum size file
					if (listNew[i].length() > finalFile.length()) {
						writeFile(new String(Files.readAllBytes(listNew[i].toPath())), finalName);
					}
				}
			}
		}
	}

}
