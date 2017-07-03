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

import org.apache.commons.lang.StringUtils;
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
	public JSONObject handleBaseUrl(JSONObject openAPI, String mode, String baseUrl)
			throws JSONException, MalformedURLException {

		if (mode == "null" | mode == "/") {
			openAPI = nullBaseUrl(openAPI, mode, baseUrl);
		} else {
			openAPI = httpBaseUrl(openAPI, mode);
		}

		return openAPI;
	}

	private JSONObject nullBaseUrl(JSONObject openAPI, String mode, String baseUrl)
			throws MalformedURLException, JSONException {

		// 1. adjust specification
		if (baseUrl != null) {
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
			// https://api.createsend.com/api/v3.1/ => https://api.createsend.com/api/v3.1
			if (commonUrl.endsWith("/")) {
				commonUrl = commonUrl.substring(0, commonUrl.length() -1 );
			}
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
		String basePath = "https://";

		for (int i = 0; i < urlList.size(); i++) {
			// for a string , find the most frequence match common string
			Map<String, Integer> tmpFrequency = new HashMap<String, Integer>();
			String tmpUrl = urlList.get(i);
			
			for (int j = 0; j < urlList.size(); j++) {
				String compareUrl = urlList.get(j);
				if (tmpUrl.equals(compareUrl))
					continue;
				String tmpCommon = greatestCommonPrefix(tmpUrl, compareUrl);
				if (tmpCommon.length() <= basePath.length()) {
					continue;
				}

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

			// make sure the common url is valid
			if (isValidCommon(maxCommon)) {
				allFrequency.put(maxCommon, maxNum);
			}
		}

		
		// Step 2 most length

		int num = 0;
		Out.prln(allFrequency);

		ProcessMethod processMe = new ProcessMethod();
		Map<String, Integer> sortedMap = processMe.sortByValues(allFrequency);
		// select top 2
		if (sortedMap.size() == 0) {
			return null;
		} else if (sortedMap.size() == 1) {
			return (String) sortedMap.entrySet().iterator().next().getKey();
		} else {
			Object[] sortedArray = sortedMap.keySet().toArray();
			// select top 2, who contains api
			String rank1 = sortedArray[0].toString();
			String rank2 = sortedArray[1].toString();

			// if they don't contain api, just return the first one
			basePath = rank1;

			// case 1: rank 1 contain api, rank 2 don't contain
			if (rank1.contains("api") && !rank2.contains("api")) {
				basePath = rank1;
			} else if (!rank1.contains("api") && (rank2.contains("api") | rank2.contains("rest"))) {
				// case 2: rank 1 don't contain api, rank2 contain api
				basePath = rank2;
			} else if (rank1.contains("api") && rank2.contains("api")) {
				// case 3 : rank 1 2 contain api
				// check rank2 last segment contain "v" or not
				String[] segment = rank2.split("/");
				// https://api.yelp.com/v3/businesses/
				// check the segment, if it contians v?
				for (int i = 0; i < segment.length; i++) {
					if (segment[i].matches("(?i)v\\d")) {
						basePath = rank2.substring(0, rank2.indexOf(segment[i]) + segment[i].length());
						break;
					}
				}
				// check last segment
				if (segment[segment.length - 1].matches("(?i)v\\d")) {
					basePath = rank2;
				} else if (segment[segment.length - 1].matches("(?i)rest")) {
					// check rank2 last segment contain "/rest" or not
					basePath = rank2;
				}
			}
		}

		basePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath.trim();

		Out.prln(basePath);
		return basePath;

	}

	private boolean isValidCommon(String maxCommon) {
		// make sure the common url is valid
		if (maxCommon != null && maxCommon.length() > "https://www.a".length() && !maxCommon.endsWith(".")) {
			return true;
		}
		return false;
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
			String str = pathIter.next().toString();
			Out.prln(str);
			if (str != null && !str.isEmpty() && !str.equalsIgnoreCase("http") && !str.equalsIgnoreCase("https")) {
				urlList.add(str);
			}
		}

		Out.prln(urlList);
		for (int i = 0; i < urlList.size(); i++) {
			// remove ?access_tocken.....
			String str = urlList.get(i).trim();
			if (str.contains("?")) {
				str = str.substring(0, str.indexOf("?"));
				if (str.endsWith("~")) {
					str = str.substring(0, str.length() - 1);
				}
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

	public JSONObject adjustSpec(JSONObject openAPI, String scheme, String baseUrl)
			throws MalformedURLException, JSONException {
		try {
			URL url = new URL(baseUrl);
			// 1. add host, basePath, scheme
			String host = url.getHost();
			String basePath = url.getPath();
			String schemes = url.getProtocol();
			JSONArray scheArr = new JSONArray();
			openAPI.put("host", host);
			if (StringUtils.isNotEmpty(basePath)) {
				openAPI.put("basePath", basePath);
			}
			scheArr.put(schemes);
			openAPI.put("schemes", scheArr);
		} catch (MalformedURLException e) {
			// the URL is not in a valid form
			return openAPI;
		}

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

			String originStr = null, retainStr = null;

			
			// if url contains commonUrl
			if (keyUrl.equals(baseUrl)) {
				retainStr = "/";
				originStr = (String) pathObject.names().get(i);
			} else if (keyUrl.contains(baseUrl)) {
				retainStr = keyUrl.substring(baseUrl.length());
				originStr = (String) pathObject.names().get(i);
			} else {
				// doestn't contain
				continue;
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

	public String searchBaseUrl(File[] listFiles, String API_NAME)
			throws MalformedURLException, ResourceInstantiationException, JSONException {
		// define the list of baseUrl
		List<String> baseUrlList = new ArrayList<String>();
		ProcessMethod processMe = new ProcessMethod();
		
		//direct model
		baseUrlList = searchUrlMode1(listFiles, API_NAME, baseUrlList, processMe, "(?si)API root URL.{1,18}((http)|(https)){1}://");
		if (baseUrlList.size() != 0) {
			// if matched, return directly
			return baseUrlList.get(0);
		}
		baseUrlList = searchUrlMode1(listFiles, API_NAME, baseUrlList, processMe, "(?si)REST API along with.{1,100}((http)|(https)){1}://");
		if (baseUrlList.size() != 0) {
			// if matched, return directly
			return baseUrlList.get(0);
		}
        
		baseUrlList = searchUrlMode2(listFiles, API_NAME, baseUrlList, processMe);

		if (baseUrlList.isEmpty()) {
			// in this case, find the http
			// don't need contain rest at first
			for (int i = 0; i < listFiles.length; i++) {
				// print the file name
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
					// Fix 1: suppose the len(content between get and http) < 40 +
					// "://"
					String regexHttp = "(?si)((http)|(https)){1}://";
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
		
		// find the base url
		Map<Object, Long> counts = baseUrlList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

		Map<Object, Long> sortedMap = processMe.sortByValues(counts);
		// Out.prln(sortedMap);

		// select top 2
		if (sortedMap.size() == 0) {
			return null;
		} else if (sortedMap.size() == 1) {
			return (String) sortedMap.entrySet().iterator().next().getKey();
		} else {
			Object[] sortedArray = sortedMap.keySet().toArray();
			List<String> apiArray = new ArrayList<String>();
			// first, select all the URL who contains APIs
			for (int i = 0; i< sortedArray.length; i++) {
				if (sortedArray[i].toString().contains("api")) {
					apiArray.add(sortedArray[i].toString());
				}
			}
			
			if (apiArray.size()!=0) {
				// select from api array
				for (int j = 0; j < apiArray.size(); j++ ){
					String[] apiUrl = apiArray.get(j).toString().split("/");
					// end with version
					if (apiUrl[apiUrl.length-1].matches("(?si)v\\d+")) {
						return apiArray.get(j).toString();
					}
				}
			} else {
				// select from big array
				// select top 2, who contains api
				for (int i = 0; i < 2; i++) {
					if (sortedArray[i].toString().contains("api")) {
						return sortedArray[i].toString();
					}
				}
			}
			

			// if they don't contain api, just return the first one
			return sortedArray[0].toString();
		}
	}

	private List<String> searchUrlMode1(File[] listFiles, String API_NAME, List<String> baseUrlList,
			ProcessMethod processMe, String regexRoot) throws MalformedURLException, ResourceInstantiationException {
		
		
		for (int i = 0; i < listFiles.length; i++) {
			// print the file name
			// Out.prln("=============File name=======================");
			// Out.prln(listFiles[i].getPath());
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
				// Fix 1: suppose the len(content between get and http) < 40 +
				// "://"

				Pattern rRest = Pattern.compile(regexRoot);
				Matcher matcherRoot = rRest.matcher(strAll);
				while (matcherRoot.find()) {
					
					// Fix 2: suppose the URL length < 40
					String matchStrNull = strAll.substring(matcherRoot.start()).split("\n")[0].trim();
					// final API endpoint must contain API_NAME
					matchStrNull = matchStrNull.substring(matchStrNull.indexOf("http"));
					if (matchStrNull != null) {
						Out.prln(matchStrNull);
						baseUrlList.add(matchStrNull);
					}
				}

			}
		}
		
		return baseUrlList;
	}

	private List<String> searchUrlMode2(File[] listFiles, String API_NAME, List<String> baseUrlList, ProcessMethod processMe)
			throws MalformedURLException, ResourceInstantiationException {
		
		for (int i = 0; i < listFiles.length; i++) {
			// print the file name
			// Out.prln("=============File name=======================");
			// Out.prln(listFiles[i].getPath());
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
				// Fix 1: suppose the len(content between get and http) < 40 +
				// "://"
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
		
		return baseUrlList;
	}

	public String cleanBaseUrl(String baseUrl) {
		// in the case the baseUrl contains path templating 
		// https://graph.facebook.com/<API_VERSION>/<PAGE_ID>/feed
		if (baseUrl == null) return baseUrl;
		
		String[] urlPart = baseUrl.split("/");
		if (urlPart.length != 0) {
			for (int i = 0; i < urlPart.length; i++) {
				String part = urlPart[i];
				// part contain < | {
				if (part.startsWith("<") | part.startsWith("{")) {
					// return the 
					return baseUrl.substring(0, baseUrl.indexOf(part)-1);
				}
			}
		}
		
		return baseUrl;
	}

}
