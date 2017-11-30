package com.hanyang;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tools.ant.taskdefs.Javadoc.Html;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.util.Out;

public class ProcessResponse {

	public boolean isResponseTemplate(String codeText, Annotation anno, String strAll) {
		// not only check "parameter" in the table text
		// but also need to check text just before the table
		// maybe in the table maybe doesn't contain str "parameter"

		// find previous text
		int templateLocation = anno.getStartNode().getOffset().intValue();
		String appendTemplateText;
		// the "parameter" string must not far from the startNode
		if (anno.getEndNode().getOffset().intValue() - templateLocation > 100) {
			// if the table is to big, just check the 100 character
			// check the pre-text, if it contains parameter | argument
			if (templateLocation <= 50) {
				appendTemplateText = strAll.substring(templateLocation, templateLocation + 100);
			} else {
				appendTemplateText = strAll.substring(templateLocation - 50, templateLocation + 100);
			}
		} else {
			if (templateLocation <= 50) {
				appendTemplateText = strAll.substring(templateLocation, anno.getEndNode().getOffset().intValue());
			} else {
				appendTemplateText = strAll.substring(templateLocation - 50, anno.getEndNode().getOffset().intValue());
			}
		}
		
		if (codeText.startsWith("{") | codeText.startsWith("[")) {
			
			if (Pattern.compile(Settings.RESKEY, Pattern.CASE_INSENSITIVE).matcher(appendTemplateText)
					.find()) {
				return true;
			}
		}
		return false;
	}

	public void handleResponseTemplate(JSONObject openAPI, Document doc, ProcessMethod processMe, String strAll,
			List<JSONObject> infoJson, AnnotationSet annoOrigin) throws JSONException {
		// find code example in the code
		AnnotationSet annoPre = annoOrigin.get(Settings.RESTEMPLATE);
		searchCode(openAPI, doc, processMe, strAll, infoJson, annoPre);
	}

	public void searchCode(JSONObject openAPI, Document doc, ProcessMethod processMe, String strAll,
			List<JSONObject> infoJson, AnnotationSet annoCode) throws JSONException {
		// 5.3 for each page, set findCodeTemplate = False
		boolean findCodeTemplate = false;
		// 5.3.1 Test if the page contains multiple parameter table or not
		Iterator<Annotation> codeIter = annoCode.iterator();
		
		while (codeIter.hasNext()) {
			Annotation anno = (Annotation) codeIter.next();
			String codeText = gate.Utils.stringFor(doc, anno);			
			if (isResponseTemplate(codeText, anno, strAll)) {
				findCodeTemplate = true;
				generateResponse(openAPI, codeText, strAll, infoJson, anno, doc, processMe,annoCode);
			}
		}

	}
	
	public JSONObject generateResponse(JSONObject openAPI, String codeText, String strAll, List<JSONObject> infoJson,
			Annotation anno, Document doc, ProcessMethod processMe, AnnotationSet annoCode) throws JSONException {
		ProcessParameter processPa = new ProcessParameter();
		JSONObject sectionJson = processPa.matchURL(codeText, strAll, infoJson, anno.getStartNode().getOffset(), doc, "response");
		
		// if the sectionJson is null, showing that it doesn't match
		if (sectionJson.length() != 0) {

			String url = sectionJson.getString("url");
			String action = sectionJson.getString("action");
       
			try {
                //need to fix
				//remove all the whitespace 
				codeText = codeText.replaceAll(" ", "");
				Out.prln(codeText);
				
				Object codeObj = null;
				if (codeText.startsWith("{")) {
					codeObj = new JSONObject(codeText);
				} else if (codeText.startsWith("[")) {
					codeObj = new JSONArray(codeText);
				}
				
					
				if (openAPI.getJSONObject("paths").has(url)) {
					JSONObject urlObject = openAPI.getJSONObject("paths").getJSONObject(url);
					JSONObject actionObject;
					
					if (urlObject.has(action)) {
						actionObject = urlObject.getJSONObject(action);
						JSONObject correctObject = new JSONObject();
						correctObject.put("description", "correct response");
						correctObject.put("example", codeObj);
						
						JSONObject resObject = new JSONObject();
						resObject.put("200", correctObject);
						
						actionObject.put("responses", resObject);
					}
					
				}
								
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return openAPI;
	}
	
}
