package com.hanyang;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	public JSONObject addUrl(JSONObject openAPI, String url, String action) throws JSONException {
		JSONObject urlObject = openAPI.getJSONObject("paths");
		Out.prln("---------match--action-------------");
		Out.prln(action);
		JSONObject actionObject = new JSONObject();
		actionObject.put(action, new JSONObject());

		if (urlObject.isNull(url)) {
			// if url object is null, add directly for the first time
			urlObject.put(url, actionObject);
		} else {
			// otherwise add the new action to the url
			JSONObject urlInterObject = urlObject.getJSONObject(url);
			urlInterObject.put(action, new JSONObject());
		}

		return openAPI;
	}

	public void addNoParaUrl(JSONObject openAPI, String strAll, List<JSONObject> infoJson) throws JSONException {
		// choose the most proper url/action pair
		Pair<String, String> properPair = solveConflicts(strAll, infoJson);
		// handle it badly, need to fix:
		String action = properPair.getKey();
		String url = properPair.getValue();
		addUrl(openAPI, url, action);
	}

	public Pair<String, String> solveConflicts(String strAll, List<JSONObject> infoJson) throws JSONException {
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

			if (Math.abs(acLocation - urlLocation) < miniMum) {
				miniMum = Math.abs(acLocation - urlLocation);
				properPair = Pair.of(actionFinal, urlFinal);
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
		if (urlString.contains(" ")) {
			urlString = urlString.split(" ")[0].trim();
		}
		// user/user-id/ => user/user-id
		if (urlString.endsWith("/")) {
			urlString = urlString.substring(0, urlString.lastIndexOf("/"));
		}
		// user/user-id.json => user/user-id
		// String urlEnd = urlString.substring(urlString.lastIndexOf("/"));
		if (urlString.lastIndexOf(".json") != -1) {
			urlString = urlString.substring(0, urlString.lastIndexOf(".json"));
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
	    Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}

	public boolean isUrl(String urlText, Annotation anno, String strAll, String aPI_NAME) {
		if (urlText.contains(aPI_NAME)) {
			return true;
		}
		return false;
	}

	public String findAction(String urlString) {
		if (urlString.contains("get")) {
			return "GET";
		} else if (urlString.contains("post") | urlString.contains("create") 
				| urlString.contains("add") | urlString.contains("edit")
				| urlString.contains("join")| urlString.contains("set")) {
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
}
