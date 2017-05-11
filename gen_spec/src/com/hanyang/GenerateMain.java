package com.hanyang;

import org.json.JSONException;
import org.json.JSONObject;

public class GenerateMain {
	public JSONObject generateStructure() {
		JSONObject swagger = new JSONObject();
		try {
			swagger.put("swagger", "2.0");
			swagger.put("info", "This is the information of the API");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return swagger;
	}
}
