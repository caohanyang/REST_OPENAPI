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
			Annotation anno, Document doc, ProcessMethod processMe, String templateNumber, String template)
			throws JSONException {

		JSONObject sectionJson = matchURL(paraStr, fullText, infoJson, anno.getStartNode().getOffset(), templateNumber);

		// if the sectionJson is null, showing that it doesn't match
		if (sectionJson.length() != 0) {

			String url = sectionJson.getString("url");
			String action = sectionJson.getString("action");

			// 1. we add url/action into swagger now.
			// because here we have known that each table have match one url
			// some urls would not be used.
			processMe.addUrl(openAPI, url, action);

			JSONObject urlObject = openAPI.getJSONObject("paths").getJSONObject(url);
			// 1. find the action
			JSONObject paraAll = new JSONObject();
			// parser the parameters
			JSONArray paraArray = parseParameter(paraStr, anno, doc, template);
			paraAll.put("parameters", paraArray);
			urlObject.put(action, paraAll);
		}

		return openAPI;
	}

	public JSONArray parseParameter(String paraStr, Annotation anno, Document doc, String template)
			throws JSONException {

		JSONArray paraArray = new JSONArray();

		if (template == "table") {
			paraArray = parseTable(paraStr, anno, doc);
		} else if (template == "list") {
			paraArray = parseList(paraStr, anno, doc);
		}

		return paraArray;
	}

	private JSONArray parseList(String paraStr, Annotation anno, Document doc) throws JSONException {
		JSONArray paraArray = new JSONArray();
		Long startDl = anno.getStartNode().getOffset();
		Long endDl = anno.getEndNode().getOffset();
		
		AnnotationSet dtSet = doc.getAnnotations("Original markups").get("dt", startDl, endDl + 1);
		
		//get dt list and sort
		List dtList = new ArrayList(dtSet);
		Collections.sort(dtList, new OffsetComparator());
		
		
		//get dd
		for (int i = 0; i < dtList.size(); i++) {
			Annotation dtElement = (Annotation) dtList.get(i);
			Long endDt = dtElement.getEndNode().getOffset();
			
			String dtStr = gate.Utils.stringFor(doc, dtElement);
			if (!dtStr.isEmpty()) {
				// find value
				AnnotationSet ddSet = doc.getAnnotations("Original markups").get("dd", endDt, endDt + 100);
				//get dd list and sort
				List ddList = new ArrayList(ddSet);
				Collections.sort(ddList, new OffsetComparator());
				// each time get the first dd (nearest dd)
				Annotation ddElement = (Annotation) ddList.get(0);
				String ddStr = gate.Utils.stringFor(doc, ddElement);
				//construct json
				JSONObject keyObject = new JSONObject();
				keyObject.put("name", dtStr);
				keyObject.put("description", ddStr);
				keyObject.put("in", "query");
				keyObject.put("type", "integer");
				keyObject.put("required", "required");
				paraArray.put(keyObject);
			}
			
		}
		
		return paraArray;
	}

	private JSONArray parseTable(String paraStr, Annotation anno, Document doc) throws JSONException {
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
			String key = trStr.split(" ")[0];
			String value = trStr.substring(trStr.indexOf(trStr.split(" ")[1]));
			JSONObject keyObject = new JSONObject();
			keyObject.put("name", key);
			keyObject.put("description", value);
			keyObject.put("in", "query");
			keyObject.put("type", "integer");
			keyObject.put("required", "required");

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

	public JSONObject matchURL(String paraStr, String fullText, List<JSONObject> infoJson, Long paraLocation,
			String templateNumber) throws JSONException {
		JSONObject sectionObject = new JSONObject();
		int minimumDistance = Integer.MAX_VALUE;
		Out.prln("----------para location-------");
		Out.prln(paraLocation);
		for (JSONObject it : infoJson) {
			JSONObject entry = it.getJSONObject("url");
			Iterator keys = entry.keys();
			if (keys.hasNext()) {
				String key = (String) keys.next();
				// Rule 1: url that not far from "example..." ???

				// Rule 2: search the nearest url
				if (templateNumber.matches("single")) {
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
		return sectionObject;
	}

	public boolean isParaTemplate(String txt, Annotation anno, String strAll) {
		// not only check "parameter" in the table text
		// but also need to check text just before the table
		// maybe in the table maybe doesn't contain str "parameter"

		// find previous text
		int templateLocation = anno.getStartNode().getOffset().intValue();
		String appendTemplateText;
		// the "parameter" string must not far from the startNode
		if (anno.getEndNode().getOffset().intValue() - templateLocation > 100) {
			// if the table is to big, just check the 100 character
			appendTemplateText = strAll.substring(templateLocation - 20, templateLocation + 100);
		} else {
			appendTemplateText = strAll.substring(templateLocation - 20, anno.getEndNode().getOffset().intValue());
		}
		if (Pattern.compile("(parameter)|(argument)", Pattern.CASE_INSENSITIVE).matcher(appendTemplateText).find()) {
			return true;
		}
		return false;
	}

}
