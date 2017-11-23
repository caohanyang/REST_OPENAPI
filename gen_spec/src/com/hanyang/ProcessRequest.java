package com.hanyang;

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

public class ProcessRequest {

	public boolean isRequestTemplate(String codeText, Annotation anno, String strAll) {
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
			
			if (Pattern.compile("(example)|(request)", Pattern.CASE_INSENSITIVE).matcher(appendTemplateText)
					.find()) {
				return true;
			}
		}
		return false;
	}

	public JSONObject generateRequest(JSONObject openAPI, String requestText, String strAll, List<JSONObject> infoJson,
			 Document doc, ProcessMethod processMe) throws JSONException {
		ProcessParameter processPa = new ProcessParameter();
		JSONObject sectionJson = processPa.matchURL(requestText, strAll, infoJson, Long.valueOf(strAll.indexOf(requestText)), "multiple", doc, "example");
		
		// if the sectionJson is null, showing that it doesn't match
		if (sectionJson.length() != 0) {

			String url = sectionJson.getString("url");
			String action = sectionJson.getString("action");
       
			try {
                //need to fix
				//remove all the whitespace 
				requestText = requestText.replaceAll(" ", "");
				Out.prln(requestText);
				
//				JSONObject resObject = new JSONObject(requestText);
					
				if (openAPI.getJSONObject("paths").has(url)) {
					JSONObject urlObject = openAPI.getJSONObject("paths").getJSONObject(url);
					JSONObject actionObject;
					
					if (urlObject.has(action)) {
						actionObject = urlObject.getJSONObject(action);
						
						actionObject.put("request", requestText);
					}
					
				}
								
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return openAPI;
	}
	
}
