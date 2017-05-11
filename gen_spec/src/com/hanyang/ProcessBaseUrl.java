package com.hanyang;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ResourceInstantiationException;
import gate.util.Out;

public class ProcessBaseUrl {
	public JSONObject handleBaseUrl(JSONObject openAPI, String mode, String baseUrl) throws JSONException, MalformedURLException {
		
		if (mode == "null") {
			openAPI = nullBaseUrl(openAPI, mode, baseUrl);
		} else {
			openAPI = httpBaseUrl(openAPI, mode);
		}
       
		return openAPI;
	}

	private JSONObject nullBaseUrl(JSONObject openAPI, String mode, String baseUrl) throws MalformedURLException, JSONException {
		
		// 1. adjust specification
		if (baseUrl!=null) {
			openAPI = adjustSpec(openAPI, mode, baseUrl);
		}
		return openAPI;
	}

	private JSONObject httpBaseUrl(JSONObject openAPI, String mode) throws JSONException, MalformedURLException {
		// 1. first remove the unrelated url
		List<String> urlList = pruneUrl(openAPI);
		if (!urlList.isEmpty()) {
			// 2. find the common url
			String commonUrl = combineUrl(urlList);
			// 3. adjust specification
			openAPI = adjustSpec(openAPI, mode, commonUrl);
		}
		return openAPI;
	}

	public String combineUrl(List<String> urlList) {
		// If it contains only url, return it
		if (urlList.size() == 1)
			return urlList.get(0);

		// First find the common part for most of the string
		// Step 1 most frequence
		Map<String, Integer> allFrequency = new HashMap<String, Integer>();

		for (int i = 0; i < urlList.size(); i++) {
			// for a string , find the most frequence match common string
			Map<String, Integer> tmpFrequency = new HashMap<String, Integer>();
			String tmpUrl = urlList.get(i);
			for (int j = 0; j < urlList.size(); j++) {
				String compareUrl = urlList.get(j);
				if (tmpUrl.equals(compareUrl))
					continue;
				String tmpCommon = greatestCommonPrefix(tmpUrl, compareUrl);
				if (tmpFrequency.containsKey(tmpCommon)) {
					tmpFrequency.put(tmpCommon, tmpFrequency.get(tmpCommon) + 1);
				} else {
					tmpFrequency.put(tmpCommon, 1);
				}
			}
			// Out.prln(tmpFrequency);
			// get the common string frequency, put the most frequency into a
			// map
			int maxNum = 0;
			String maxCommon = null;
			for (Map.Entry<String, Integer> it : tmpFrequency.entrySet()) {
				if (maxNum < it.getValue()) {
					maxNum = it.getValue();
					maxCommon = it.getKey();
				}
			}

			// put them in the whole map
			allFrequency.put(maxCommon, maxNum);
		}

		// Step 2 most length
		String basePath = "http";
		Out.prln(allFrequency);
		for (Map.Entry<String, Integer> entry : allFrequency.entrySet()) {
			if (entry.getKey().length() > basePath.length()) {
				basePath = entry.getKey().toString();
			}
		}
		basePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath.trim();

		return basePath;

	}

	public String greatestCommonPrefix(String a, String b) {
		int minLength = Math.min(a.length(), b.length());
		for (int i = 0; i < minLength; i++) {
			if (a.charAt(i) != b.charAt(i)) {
				return a.substring(0, i);
			}
		}
		return a.substring(0, minLength);
	}

	public List<String> pruneUrl(JSONObject openAPI) throws JSONException {
		List<String> urlList = new ArrayList<String>();
		JSONObject pathObject = openAPI.getJSONObject("paths");
		Iterator<?> pathIter = pathObject.keys();
		while (pathIter.hasNext()) {
			urlList.add(pathIter.next().toString());
		}

		Out.prln(urlList);
		for (int i = 0; i < urlList.size(); i++) {
			// remove ?access_tocken.....
			String str = urlList.get(i).trim();
			if (str.contains("?")) {
				str = str.substring(0, str.indexOf("?"));
				if (str.endsWith("/")) {
					str = str.substring(0, str.length() - 1);
				}
				urlList.set(i, str);
			}
			Out.prln(str);
		}

		Out.prln(urlList);

		return urlList;

	}

	public JSONObject adjustSpec(JSONObject openAPI, String scheme, String baseUrl) throws MalformedURLException, JSONException {
		URL url = new URL(baseUrl);
		// 1. add host, basePath, scheme
		String host = url.getHost();
		String basePath = url.getPath();
		String schemes = url.getProtocol();
		JSONArray scheArr = new JSONArray();
		openAPI.put("host", host);
		openAPI.put("basePath", basePath);
		scheArr.put(schemes);
		openAPI.put("schemes", scheArr);

		// 2. short internal urls
		JSONObject pathObject = openAPI.getJSONObject("paths");
		// if pathObject is null, return directly
		if (pathObject.length() == 0) 
			return openAPI;
		
		Map<String, Pair<String, JSONObject>> modiMap = new HashMap<String, Pair<String, JSONObject>>();
		// construct new object
		for (int i = 0; i < pathObject.names().length(); i++) {

			String keyUrl = (String) pathObject.names().get(i);
			if (keyUrl.contains("?")) {
				keyUrl = keyUrl.substring(0, keyUrl.indexOf("?")).trim();
				if (keyUrl.endsWith("/")) {
					keyUrl = keyUrl.substring(0, keyUrl.length() - 1);
				}
			}
           
			String originStr = null, retainStr=null;
			
			if (scheme == "null") {
				originStr = (String) pathObject.names().get(i);
				retainStr = "?method" + "=" + originStr;
				
			} else {
				// if url contains commonUrl
				if (keyUrl.contains(baseUrl)) {
					retainStr = keyUrl.substring(baseUrl.length());
					originStr = (String) pathObject.names().get(i);
				} else {
					// doestn't contain 
					continue;
				}
			}
			JSONObject originValue = (JSONObject) pathObject.get(originStr);
			
			Pair<String, JSONObject> replacedJson = Pair.of(retainStr, originValue);
			// replacedJson.put(retainStr, originValue);
			
			modiMap.put(originStr, replacedJson);
		}

		// remove and add new object
		for (Map.Entry<String, Pair<String, JSONObject>> entry : modiMap.entrySet()) {
			pathObject.remove(entry.getKey());
			pathObject.put(entry.getValue().getLeft(), entry.getValue().getRight());
		}

		Out.prln(pathObject);
		return openAPI;
	}
	
	public String searchBaseUrl(File[] listFiles, String API_NAME) throws MalformedURLException, ResourceInstantiationException, JSONException {
		// define the list of baseUrl
		List<String> baseUrlList = new ArrayList<String>();
		ProcessMethod processMe = new ProcessMethod();
		for (int i = 0; i < listFiles.length; i++) {
			// print the file name
//			Out.prln("=============File name=======================");
//			Out.prln(listFiles[i].getPath());
			String type = new Tika().detect(listFiles[i].getPath());
			// only detect html
			if (type.equals("text/html")) {
				URL u = Paths.get(listFiles[i].getPath()).toUri().toURL();
				FeatureMap params = Factory.newFeatureMap();
				params.put("sourceUrl", u);
				Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
				// 2. get all text
				DocumentContent textAll = doc.getContent();
                
				// 4.1 search for the GET https
				String strAll = textAll.toString();
				// Fix 1: suppose the len(content between get and http) < 40 + "://"
				String regexRest = "(?si)rest.+request";
				String regexHttp = "(?si)((http)|(https)){1}://";
				Pattern pRest = Pattern.compile(regexRest);
				Matcher matcherREST = pRest.matcher(strAll);
				while (matcherREST.find()) {
					// first find the page which contains REST + request
					Pattern pHttp = Pattern.compile(regexHttp);
					Matcher matcherHttp = pHttp.matcher(strAll);
					while (matcherHttp.find()) {
						// Fix 2: suppose the URL length < 40
						String matchStrNull = strAll.substring(matcherHttp.start()).split("\n")[0].trim();
						// final API endpoint must contain API_NAME
						matchStrNull = processMe.constrainUrl(matchStrNull, API_NAME);
						if (matchStrNull != null) {
							Out.prln(matchStrNull);
							baseUrlList.add(matchStrNull);
						}
					}
				}
				
			}
		}
		
		Out.prln(baseUrlList);
		
        //find the base url
		Map<Object, Long> counts =
				baseUrlList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
		
		Map<Object, Long> sortedMap = processMe.sortByValues(counts);
//		Out.prln(sortedMap);
		
		// select top 2
		if (sortedMap.size() == 0) {
			return null;
		} else if (sortedMap.size() == 1) {
			return (String) sortedMap.entrySet().iterator().next().getKey();
		} else {
			Object[] sortedArray = sortedMap.keySet().toArray();
			// select top 2, who contains api
			for (int i = 0; i< 2; i++) {
				if (sortedArray[i].toString().contains("api")) {
					return sortedArray[i].toString();
				}
			}
			
			// if they don't contain api, just return the first one
			return sortedArray[0].toString();
		}
	}

}
