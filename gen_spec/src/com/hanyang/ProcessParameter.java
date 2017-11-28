package com.hanyang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.SimpleAnnotation;
import gate.util.OffsetComparator;
import gate.util.Out;

public class ProcessParameter {
	public JSONObject generateParameter(JSONObject openAPI, String paraStr, String fullText, List<JSONObject> infoJson,
			Annotation anno, Document doc, ProcessMethod processMe)
			throws JSONException {

		JSONObject sectionJson = matchURL(paraStr, fullText, infoJson, anno.getStartNode().getOffset(),
				doc, "parameter");

		// if the sectionJson is null, showing that it doesn't match
		if (sectionJson.length() != 0) {

			String url = sectionJson.getString("url");
			String action = sectionJson.getString("action");

			if (processMe.isRealUrl(url)) {
				// 1. we add url/action into openAPI now.
				// because here we have known that each table have match one url
				// some urls would not be used.
//				processMe.addUrl(openAPI, url, action);

				try {
					JSONObject urlObject = openAPI.getJSONObject("paths").getJSONObject(url);
					// 1. find the action
					JSONObject actionObject = urlObject.getJSONObject(action);
					JSONObject paraAll = new JSONObject();
					// parser the parameters
					JSONArray paraArray = parseParameter(url, actionObject, paraStr, anno, doc);
					
					actionObject.put("parameters", paraArray);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		return openAPI;
	}

	public JSONArray parseParameter(String url, JSONObject actionObject, String paraStr, Annotation anno, Document doc)
			throws JSONException {

		JSONArray paraArray = new JSONArray();

		if (Settings.TEMPLATE.equals("table")) {
			paraArray = parseTable(url, actionObject, paraStr, anno, doc);
		} else if (Settings.TEMPLATE.equals("list")) {
			paraArray = parseList(url, actionObject, paraStr, anno, doc);
		}

		return paraArray;
	}

	private JSONArray parseList(String url, JSONObject actionObject, String paraStr, Annotation anno, Document doc) throws JSONException {
		JSONArray paraArray = new JSONArray();
		Long startL = anno.getStartNode().getOffset();
		Long endL = anno.getEndNode().getOffset();

		AnnotationSet listSet = doc.getAnnotations("Original markups").get("dt", startL, endL + 1);

		if (!listSet.isEmpty()) {

			// dl dt dd
			Out.prln("dl dt dd list");

			// get dt list and sort
			List dtList = new ArrayList(listSet);
			Collections.sort(dtList, new OffsetComparator());

			// get dd
			for (int i = 0; i < dtList.size(); i++) {
				Annotation dtElement = (Annotation) dtList.get(i);
				Long endDt = dtElement.getEndNode().getOffset();

				String dtStr = gate.Utils.stringFor(doc, dtElement);
				if (!dtStr.isEmpty()) {
					// find value
					AnnotationSet ddSet = doc.getAnnotations("Original markups").get("dd", endDt, endDt + 100);
					// get dd list and sort
					List ddList = new ArrayList(ddSet);
					Collections.sort(ddList, new OffsetComparator());
					// each time get the first dd (nearest dd)
					Annotation ddElement = (Annotation) ddList.get(0);
					String ddStr = gate.Utils.stringFor(doc, ddElement);
					// construct json
					JSONObject keyObject = new JSONObject();
					keyObject.put("name", dtStr);
					keyObject.put("description", ddStr);
					keyObject.put("in", findParaType(url, actionObject));
					keyObject.put("type", "integer");
					keyObject.put("required", "required");
					paraArray.put(keyObject);
				}

			}
		} else {
			
			// we use the annotation directly
			
					Long startUl = anno.getStartNode().getOffset();
					Long endUl = anno.getEndNode().getOffset();

					String ulStr = gate.Utils.stringFor(doc, anno);
					if (!ulStr.isEmpty()) {
						// find value
						AnnotationSet aSet = doc.getAnnotations("Original markups").getContained(startUl, endUl + 1).get("li");
						// get dd list and sort
						List aList = new ArrayList(aSet);
						if (!aList.isEmpty()) {
							
							Collections.sort(aList, new OffsetComparator());
							// each time get the first dd (nearest dd)
							for (int i = 0 ; i < aList.size(); i++) {
								
								Annotation liElement = (Annotation) aList.get(i);
								String liStr = gate.Utils.stringFor(doc, liElement);
								// construct json
								String[] pArray = liStr.split("\n");
								JSONObject keyObject = new JSONObject();
								keyObject.put("name", pArray[0]);
								keyObject.put("description", pArray[1]);
								keyObject.put("in", findParaType(url, actionObject));
								keyObject.put("type", "integer");
								keyObject.put("required", true);
								paraArray.put(keyObject);
							}
							
						}
						
					}

			
			
		}

		return paraArray;
	}

	private String findParaType(String url, JSONObject actionObject) throws JSONException {
		// find parameter type
		// Possible values are "query", "header", "path", "formData" or "body".
		if (actionObject.has("request")) {
			String request = actionObject.getString("request");
			if (request.startsWith("{") | request.startsWith("[")) {
				return "body";
			}
		}
		return Settings.PARAIN;
	}

	private JSONArray parseTable(String url, JSONObject actionObject, String paraStr, Annotation anno, Document doc) throws JSONException {
		// Here we need to get tbody, not the whole table annotation
		List trList = getTbody(paraStr, anno, doc);

		JSONArray paraArray = new JSONArray();
		// Iterate the row element
		// Iterator<Annotation> trIter = trSet.iterator();
		boolean titleRow = true; // first time check the first row
		// Set defaut property key name
		int NAME_INDEX = -1;
		int DESCRIPTION_INDEX = -1;
		int TYPE_INDEX = -1;
		int REQUIRED_INDEX = -1;

		for (int i = 0; i < trList.size(); i++) {
			Annotation trElement = (Annotation) trList.get(i);
			String trStr = gate.Utils.stringFor(doc, trElement);
			Long startTd = trElement.getStartNode().getOffset();
			Long endTd = trElement.getEndNode().getOffset();
			// add offset 1 to avoid extract less td annotation
			AnnotationSet tdSet = doc.getAnnotations("Original markups").get("td", startTd, endTd + 1);
			List tdList = new ArrayList(tdSet);
			Collections.sort(tdList, new OffsetComparator());

			// If it's first time, check the rowname
			if (titleRow) {
				// close checking the title row.
				titleRow = false;
				// check the first row
				if (StringUtils.containsIgnoreCase(trStr, "name")
						|| StringUtils.containsIgnoreCase(trStr, "description")) {
					// check the property name
					for (int j = 0; j < tdList.size(); j++) {
						Annotation tdElement = (Annotation) tdList.get(j);
						String tdStr = gate.Utils.stringFor(doc, tdElement);
						if (!tdStr.isEmpty()) {
							if (StringUtils.containsIgnoreCase(tdStr, "name")) {
								NAME_INDEX = j;
							} else if (StringUtils.containsIgnoreCase(tdStr, "description")) {
								DESCRIPTION_INDEX = j;
							} else if (StringUtils.containsIgnoreCase(tdStr, "example")) {
								TYPE_INDEX = j;
							} else if (StringUtils.containsIgnoreCase(tdStr, "require")) {
								REQUIRED_INDEX = j;
							}
						}
					}
					// match the title row, continue
					continue;
				}

			}

			// set defaut value
			JSONObject keyObject = new JSONObject();
			try {
				String key = trStr.split(" ")[0];
				String value = trStr.substring(trStr.indexOf(trStr.split(" ")[1]));
				keyObject.put("name", key);
				keyObject.put("description", value);
				keyObject.put("in", findParaType(url, actionObject));
				keyObject.put("type", "integer");
				keyObject.put("required", "required");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// if we have already identified the key-value
			// change the value to the new one
			if (NAME_INDEX != -1) {
				keyObject.put("name", gate.Utils.stringFor(doc, (SimpleAnnotation) tdList.get(NAME_INDEX)));
			}
			if (DESCRIPTION_INDEX != -1) {
				keyObject.put("description",
						gate.Utils.stringFor(doc, (SimpleAnnotation) tdList.get(DESCRIPTION_INDEX)));
			}
			if (TYPE_INDEX != -1) {
				String type = gate.Utils.stringFor(doc, (SimpleAnnotation) tdList.get(TYPE_INDEX));
				keyObject.put("type", extactType(type));
			}
			if (REQUIRED_INDEX != -1) {
				keyObject.put("required", gate.Utils.stringFor(doc, (SimpleAnnotation) tdList.get(REQUIRED_INDEX)));
			}

			paraArray.put(keyObject);

		}

		return paraArray;
	}

	public List getTbody(String paraStr, Annotation anno, Document doc) {
		// We need to get the precise tbody tag, in the parameter table
		Long startTbody = anno.getStartNode().getOffset();
		Long endTbody = anno.getEndNode().getOffset();
		AnnotationSet tbodySet = doc.getAnnotations("Original markups").get("tbody", startTbody, endTbody);
		// if contain only one tbody
		if (tbodySet.size() == 1) {
			Annotation tbodyAnno = tbodySet.iterator().next();
			Long startTr = tbodyAnno.getStartNode().getOffset();
			Long endTr = tbodyAnno.getEndNode().getOffset();
			AnnotationSet trSet = doc.getAnnotations("Original markups").get("tr", startTr, endTr);

			List trList = new ArrayList(trSet);

			// sort the list by offset
			Collections.sort(trList, new OffsetComparator());

			return trList;
		}

		return null;

	}

	public String extactType(String type) {
		if (StringUtils.isNumeric(type)) {
			return "integer";
		} else if ("true".equalsIgnoreCase(type) || "false".equalsIgnoreCase(type)) {
			return "boolean";
		}
		return "integer";
	}

	public Map<String, Integer> genUrlLocation(String fullText, List<String> urlList) {
		Map<String, Integer> urlLocation = new HashMap<String, Integer>();
		for (String element : urlList) {
			int location = fullText.indexOf(element);
			urlLocation.put(element, location);
		}
		return urlLocation;

	}

	public void handleParaTemplate(JSONObject openAPI, Document doc,
			ProcessMethod processMe, String strAll, List<JSONObject> infoJson,
			AnnotationSet annoOrigin) throws JSONException {
		if (Settings.TEMPLATE.equals("table")) {
			// 1 get table annotation
			AnnotationSet annoTable = annoOrigin.get("table");
			searchParameter(openAPI, doc, processMe, strAll, infoJson, annoTable);
		} else if (Settings.TEMPLATE.equals("list")) {
			AnnotationSet annoList = annoOrigin.get("dl");
			if (annoList.isEmpty()) {
				// this is unordered list
				annoList = annoOrigin.get("ul");
			}
			// 2 get list annotation
			searchParameter(openAPI, doc, processMe, strAll, infoJson, annoList);
		}
	}
	
	public void searchParameter(JSONObject openAPI, Document doc,
			ProcessMethod processMe, String strAll, List<JSONObject> infoJson, AnnotationSet annoTemplate) throws JSONException {
		// 5.3 for each page, set findParaTable = False
		boolean findParaTemplate = false;
		// 5.3.1 Test if the page contains multiple parameter table or not
		Iterator<Annotation> testIter = annoTemplate.iterator();
		String templateNumber = Settings.NUMBER;
		// int numTemplate = 0;
		// while (testIter.hasNext()) {
		// Annotation anno = (Annotation) testIter.next();
		// String templateText = gate.Utils.stringFor(doc, anno);
		// ProcessParameter processPa = new ProcessParameter();
		// if (processPa.isParaTemplate(templateText, anno, strAll)) {
		// // numTemplate++;
		// }
		// }
		// if (numTemplate > 1) {
		// // more than one parameter template in the page
		// multiTemplate = "multiple";
		// }

		// 5.3.2 handle the template context
		Iterator<Annotation> templateIter = annoTemplate.iterator();
		ProcessParameter processPa = new ProcessParameter();
		
//		// put all the infoJson into openAPI first
//		if (!infoJson.isEmpty()) {
//			// In case of the method doesn't have parameter
//			// add the noPara url
//			// processMe.addNoParaUrl(openAPI, strAll, infoJson, reverse);
//			// add all the url/action pair
//			processMe.addAllParaURL(openAPI, strAll, infoJson);
//		}
		
		// add parameters to those urls
		while (templateIter.hasNext()) {
			Annotation anno = (Annotation) templateIter.next();
			String templateText = gate.Utils.stringFor(doc, anno);
			if (processPa.isParaTemplate(templateText, anno, strAll)) {
				findParaTemplate = true;
				Out.prln("==========TABLE or LIST=================");
				Out.prln(templateText);
				processPa.generateParameter(openAPI, templateText, strAll, infoJson, anno, doc, processMe);
			}
		}

		
		
	}
	
	public JSONObject matchURL(String paraStr, String fullText, List<JSONObject> infoJson, Long paraLocation, Document doc, String mode) throws JSONException {
		JSONObject sectionObject = new JSONObject();
		int minimumDistance = Integer.MAX_VALUE;
		Out.prln("---------para location-------");
		Out.prln(paraLocation);
		for (JSONObject it : infoJson) {

//			// Rule 1: action that not far from "example..."
//			JSONObject entryAction = it.getJSONObject("action");
//			Iterator keysAction = entryAction.keys();
//			if (keysAction.hasNext()) {
//				Long standard = null;
//				if (mode == "parameter") {
//					// parameter
//					standard = entryAction.getLong(keysAction.next().toString());
//				} else {
//					// example ???
//					standard = entryAction.getLong(keysAction.next().toString());
//
//				}
//
//				Long head = standard;
//				String headStr = gate.Utils.stringFor(doc, Math.max(head - 50, 0), head).toLowerCase();
//				Long bottom = standard + paraStr.length();
//
//				if (bottom < fullText.length()) {
//					// it should be less than the fullText length
//					String bottomStr = gate.Utils.stringFor(doc, bottom, Math.min(bottom + 50, fullText.length()))
//							.toLowerCase();
//					if (headStr.contains("example") | bottomStr.contains("example")) {
//						sectionObject.put("action", it.getJSONObject("action").keys().next());
//						sectionObject.put("url", it.getJSONObject("url").keys().next());
//						break;
//					}
//				}
//			}

			if (Settings.U1P2) {
				// Rule 2: search the nearest url
				// first url then parameter
				JSONObject entry = it.getJSONObject("url");
				Iterator keys = entry.keys();
				if (keys.hasNext()) {
					String key = (String) keys.next();

					if (Settings.NUMBER.matches("single")) {
						// if it's a single page, search from all the text
						if (Math.abs(paraLocation - entry.getInt(key)) < minimumDistance) {
							minimumDistance = (int) Math.abs((paraLocation - entry.getInt(key)));
							sectionObject.put("action", it.getJSONObject("action").keys().next());
							sectionObject.put("url", key);
						}
					} else {
						// multiple table mode: search only in the previous context
						if ((paraLocation - entry.getInt(key) < minimumDistance)
								&& (paraLocation - entry.getInt(key) > 0)) {
							minimumDistance = (int) (paraLocation - entry.getInt(key));
							sectionObject.put("action", it.getJSONObject("action").keys().next());
							sectionObject.put("url", key);
						}
					}

				}
			}
			
		}
		return sectionObject;
	}

	public boolean isParaTemplate(String txt, Annotation anno, String strAll) {
		// not only check "parameter" in the table text
		// but also need to check text just before the table
		// maybe in the table maybe doesn't contain str "parameter"

		// find previous text
		int templateLocation = anno.getStartNode().getOffset().intValue();
		String appendTemplateText;
		//1. the "parameter" string must not far from the startNode
//		if (anno.getEndNode().getOffset().intValue() - templateLocation > 100) {
//			// if the table is to big, just check the 100 character
//			// check the pre-text, if it contains parameter | argument
//			if (templateLocation <= 50) {
//				appendTemplateText = strAll.substring(templateLocation, templateLocation + 100);
//			} else {
//				appendTemplateText = strAll.substring(templateLocation - 50, templateLocation + 100);
//			}
//		} else {
//			if (templateLocation <= 50) {
//				appendTemplateText = strAll.substring(templateLocation, anno.getEndNode().getOffset().intValue());
//			} else {
//				appendTemplateText = strAll.substring(templateLocation - 50, anno.getEndNode().getOffset().intValue());
//			}
//		}
		
		// change new method
		appendTemplateText = strAll.substring(templateLocation - 13, templateLocation + 13);
		
		if (Pattern.compile(Settings.PARAKEY, Pattern.CASE_INSENSITIVE).matcher(appendTemplateText)
				.find()) {
			return true;
		}
		return false;
	}

}
