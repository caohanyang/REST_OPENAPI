
const openAPI = require('./openAPI7.json');
var fs = require('fs');

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

	fs.writeFileSync("refineOpenAPI.json", JSON.stringify(openAPI, null, 2), 'utf8');
}

