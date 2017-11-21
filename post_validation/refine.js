
const fs = require('fs');
var path = require('path');
const args = process.argv;

// get open api file
var API_NAME, CompareSet_PATH;

if (args.length > 0) {
	API_NAME = args[2].split("//")[1].split("\.")[1];
	console.log(API_NAME)
	// google have several APIs
	if (API_NAME.includes("google")) {
		API_NAME = args[2].split("//")[1].split("/")[1];
	}
	CompareSet_PATH = "../CompareSet/" + API_NAME;
}

const openAPI = require(CompareSet_PATH + "/OpenAPI.json");
var FinalSet_PATH = path.join(__dirname, "..", "/FinalSet/");
var API_PATH = path.join(FinalSet_PATH, API_NAME);

// 1. make sure it contains host && schema

if (openAPI.host && openAPI.schemes && openAPI.paths) {
	console.log("=========Validate host and schemes=============");
	// 2. delete path with begins with https

	var keys = Object.keys(openAPI.paths);
	for (let i = 0; i < keys.length; i ++) {
		if (!keys[i].startsWith("/")) {
			// if don't start with '/' delete
            delete openAPI.paths[keys[i]];
		}
	}

	console.log(FinalSet_PATH);
	if (!fs.existsSync(FinalSet_PATH)) {
		fs.mkdirSync(FinalSet_PATH);
		if(!fs.existsSync(API_PATH)) {
			fs.mkdirSync(API_PATH);
		}
	}

	fs.writeFileSync(API_PATH + "/refineOpenAPI.json", JSON.stringify(openAPI, null, 2), 'utf8');
}

