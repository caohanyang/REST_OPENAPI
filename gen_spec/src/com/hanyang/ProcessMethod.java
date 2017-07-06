package com.hanyang;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import gate.Annotation;
import gate.util.Out;

public class ProcessMethod {
	public JSONObject generateDefault(JSONObject openAPI) throws JSONException {

		// Add path in the top level
		if (openAPI.isNull("paths") == true) {
			openAPI.put("paths", new JSONObject());
		}
		return openAPI;
	}

	public JSONObject addUrl(JSONObject openAPI, String url, String action, String scheme) throws JSONException {
		JSONObject urlObject = openAPI.getJSONObject("paths");
		Out.prln("---------match--action-------------");
		Out.prln(action);
		JSONObject actionObject = new JSONObject();
		actionObject.put(action.toUpperCase(), new JSONObject());

		if (isRealUrl(url) | scheme == "null") {
			if (urlObject.isNull(url)) {
				// if url object is null, add directly for the first time
				urlObject.put(url, actionObject);
			} else {
				// otherwise add the new action to the url
				JSONObject urlInterObject = urlObject.getJSONObject(url);
				urlInterObject.put(action, new JSONObject());
			}
		}
		return openAPI;
	}

	public boolean isRealUrl(String url) {

		// clean two times
		url = cleanUrl(url);
		url = cleanUrl(url);

		if (url.contains(".jpg") | url.contains(".gif") | url.contains(".png") | url.contains(".txt")
				| url.contains(".pdf")) {
			return false;
		}

		if (url.startsWith("http") | url.startsWith("/")) {

			int spaces = url == null ? 0 : url.length() - url.replace(" ", "").length();
			if (spaces > 2) {
				// too much space in the url, should not be a valid url
				return false;
			}

			if (url.contains(";") | url.contains("+") | url.contains("</") | url.contains(">")) {
				return false;
			}

			// url minimum length
			if (url.length() > "http://".length()) {
				return true;
			}
		}

		return false;
	}

	public void addNoParaUrl(JSONObject openAPI, String strAll, List<JSONObject> infoJson, String reverse)
			throws JSONException {
		// choose the most proper url/action pair
		Pair<String, String> properPair = solveConflicts(strAll, infoJson, reverse);
		// handle it badly, need to fix:
		String action = properPair.getKey();
		String url = properPair.getValue();
		addUrl(openAPI, url, action, null);
	}

	public Pair<String, String> solveConflicts(String strAll, List<JSONObject> infoJson, String reverse)
			throws JSONException {
		// if the are no parameter table in the page
		// if one URL have two actions, solve the conflicts
		Pair<String, String> properPair = null;
		int miniMum = Integer.MAX_VALUE;
		for (int i = 0; i < infoJson.size(); i++) {
			JSONObject acObject = infoJson.get(i).getJSONObject("action");
			JSONObject urObject = infoJson.get(i).getJSONObject("url");
			String actionFinal = acObject.keys().next().toString();
			String urlFinal = urObject.keys().next().toString();

			int acLocation = acObject.getInt(actionFinal);
			int urlLocation = urObject.getInt(urlFinal);

			if (reverse == "no") {
				if (Math.abs(acLocation - urlLocation) < miniMum) {
					miniMum = Math.abs(acLocation - urlLocation);
					properPair = Pair.of(actionFinal, urlFinal);
				}
			} else {
				if (Math.abs(urlLocation - acLocation) < miniMum) {
					miniMum = Math.abs(urlLocation - acLocation);
					properPair = Pair.of(actionFinal, urlFinal);
				}
			}

		}

		return properPair;
	}

	public String cleanUrl(String urlString) {
		// clean the url
		// user/user-id?asdfsadf => user/user-id
		if (urlString.contains("?")) {
			urlString = urlString.split("\\?")[0].trim();
		}
		// user/user-id authentication => user/user-id
		// if (urlString.contains(" ")) {
		// urlString = urlString.split(" ")[0].trim();
		// }
		// user/user-id \n => user/user-id
		if (urlString.contains("\n")) {
			urlString = urlString.split("\n")[0].trim();
		}
		// user/user-id/ => user/user-id
		if (urlString.endsWith("/")) {
			urlString = urlString.substring(0, urlString.lastIndexOf("/"));
		}

		// www.docusign.com/restapi" => www.docusign.com/restapi
		if (urlString.endsWith("\"")) {
			urlString = urlString.substring(0, urlString.lastIndexOf("\""));
		}

		// mmed.jpg\ => mmed.jpg
		if (urlString.endsWith("\\")) {
			urlString = urlString.substring(0, urlString.lastIndexOf("\\"));
		}

		// user/user-id.json => user/user-id
		// String urlEnd = urlString.substring(urlString.lastIndexOf("/"));
		if (urlString.lastIndexOf(".json") != -1) {
			urlString = urlString.substring(0, urlString.lastIndexOf(".json"));
		}
		
		if (urlString.lastIndexOf(".format") != -1) {
			urlString = urlString.substring(0, urlString.lastIndexOf(".format"));
		}

		return urlString;
	}

	public String constrainUrl(String url, String apiName) {
		// clean it first
		url = cleanUrl(url);
		// it should not contain "auth"
		if (url.contains("auth")) {
			return null;
		} else if (url.contains(apiName)) {
			// it should contain api name
			return url;
		}

		return null;
	}

	public <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				if (compare == 0)
					return 1;
				else
					return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	public boolean isUrlPath(String urlText, Annotation anno, String strAll, String aPI_NAME, String abbrev) {
		// case 1: flickr.activity.userComments
		if (anno.getType().equals("h1")) {
			if (urlText.contains(aPI_NAME)) {
				return true;
			} else if (urlText.contains("add") | urlText.contains("remove") | urlText.contains("update")
					| urlText.contains("get") | urlText.contains("search") | urlText.contains("post")
					| urlText.contains("put") | urlText.contains("patch")) {
				return true;
			}
			return false;
		} else if (anno.getType().equals("code")) {
			// case 2: flickr.activity.userComments \s(\/.*\/)
			String regexUrlPath = "(?i)((get)|(post)|(" + abbrev + ")|(put)|(patch)){1}\\s(\\/.*\\/)";
			Pattern pUrl = Pattern.compile(regexUrlPath);
			Matcher matcherUrl = pUrl.matcher(urlText);

			if (matcherUrl.lookingAt()) {
				return true;
			}

		}
		return false;
	}

	public String findAction(String urlString) {
		if (urlString.contains("get")) {
			return "GET";
		} else if (urlString.contains("post") | urlString.contains("create") | urlString.contains("add")
				| urlString.contains("edit") | urlString.contains("join") | urlString.contains("set")) {
			return "POST";
		} else if (urlString.contains("put")) {
			return "PUT";
		} else if (urlString.contains("patch")) {
			return "PATCH";
		} else if (urlString.contains("del") | urlString.contains("leave")) {
			return "DELETE";
		} else {
			return "GET";
		}
	}

	public void addAllParaURL(JSONObject openAPI, String strAll, List<JSONObject> infoJson, String reverse,
			String scheme) throws JSONException {
		// choose the most proper url/action pair
		for (int i = 0; i < infoJson.size(); i++) {
			JSONObject acObject = infoJson.get(i).getJSONObject("action");
			JSONObject urObject = infoJson.get(i).getJSONObject("url");
			String actionFinal = acObject.keys().next().toString();
			String urlFinal = urObject.keys().next().toString();

			addUrl(openAPI, urlFinal, actionFinal, scheme);
		}

	}

	public String compressUrl(String urlString) {
		// Compress URL

		int index = urlString.lastIndexOf("/");
		String frontPart, lastPart = null;
		if (index > 0) {
			// 1. compress the front part
			// https://api.createsend.com/api/v3.1 /externalsession.{xml|json}
			// => https://api.createsend.com/api/v3.1/externalsession.{xml|json}
			frontPart = urlString.substring(0, index);
			frontPart = frontPart.replaceAll("\\s+", "").trim();

			// 2. compress the last part
			// only remove the front space:
			// 3.1. /2.0/retention_policies/ policy_id =>
			// /2.0/retention_policies/ policy_id
			lastPart = urlString.substring(index + 1);
			if (lastPart.startsWith(" ")) {
				lastPart = lastPart.trim();
			}

			// 3.2. /2.0/events \\ -H don't change
			if (lastPart.contains(" ")) {
				lastPart = lastPart.substring(0, lastPart.indexOf(" "));
			}
			return frontPart + '/' + lastPart;
		}

		return urlString;
	}
}
