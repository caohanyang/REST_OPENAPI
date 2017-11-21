var curl = require('curlrequest');
var async = require('async');
var openAPI = require('./openAPI6.json');

var baseUrl = openAPI.schemes[0]+ "://" + openAPI.host;
// var baseUrl = openAPI.schemes[0]+ "://" + openAPI.host + openAPI.basePath;
var pathObj = openAPI.paths;
// console.log(pathObj);

async.eachSeries(Object.keys(pathObj), function (key, next) {

	if (pathObj.hasOwnProperty(key)) {
		var testUrl;
		if (key.startsWith('http')) {
            testUrl = key ;
            // testUrl = key + '.json';
		} else {
			testUrl = baseUrl + key;
			// testUrl = ba seUrl + key + '.json';
		}
		// console.log("---------Next Path------------");
		console.log(testUrl);
		var options = {url: testUrl, method: 'GET', include: true};

		curl.request(options, function (err, parts) {
			// console.log(parts);
			var full = parts;

			parts = parts.split('\r\n');
		    parts.pop();

            try {
		        var statusCode = getStatusCode(parts);
			    
			    if (statusCode == 403 | statusCode == 404) {
			    	
			    	//403 404
			    	if (testUrl.match(/{.*}/g)) {
			    		// contains {} path template
			    		
			    	} else {
			    		console.log("Wrong url");
			    		// console.lo1g(parts);
			    	}
			       
			    } else {
			    	if (full.match(/<!DOCTYPE html>/g)) {
				       console.log("Wrong url");
				
			        }
			    }
            } catch (e) {
                console.log("Wrong url");
            }

		
	    
		
		    next()
		});
	}


})




function getStatusCode (parts) {
	var statusCode = parts[0].split(' ')[1];
	return statusCode;
}
