#! /bin/bash

# crawl html page
# https://www.instagram.com/developer https://www.instagram.com/developer/endpoints
# https://dev.twitter.com/rest  https://dev.twitter.com/rest/reference
# https://www.flickr.com/services/api
# https://developers.google.com/youtube/v3 https://developers.google.com/youtube/v3/docs
# https://www.twilio.com/docs/api/rest
# https://developer.linkedin.com/docs
# http://api.eventful.com/docs
# https://cloud.google.com/translate  https://cloud.google.com/translate/docs/reference
# https://www.yelp.com/developers/documentation/v3
# https://docs.docusign.com/esign      https://docs.docusign.com/esign/restapi

# https://developers.facebook.com/docs/graph-api https://developers.facebook.com/docs/graph-api/reference
# http://docs.aws.amazon.com/AWSECommerceService/latest/DG/Welcome.html   http://docs.aws.amazon.com/AWSECommerceService/latest/DG
# https://en.wikipedia.org/api/rest_v1/
# https://developers.google.com/maps/documentation/
# http://www.last.fm/api
# https://developer.foursquare.com     https://developer.foursquare.com/docs
# https://www.mediawiki.org/wiki/REST_API https://en.wikipedia.org/api/rest_v1/
# http://www.geonames.org/export/ws-overview.html   http://www.geonames.org/export
# https://docs.microsoft.com/en-us/rest/api/cognitiveservices/bing-web-api-v5-reference
# https://go.developer.ebay.com/api-documentation   http://developer.ebay.com/devzone/rest
# https://msdn.microsoft.com/en-us/library/ff701713.aspx  https://msdn.microsoft.com/en-us/library/ff

# python ./predict_page/predictExternalTest.py $1


# https://developer.vimeo.com/api/endpoints
# https://developers.google.com/books/docs/v1/reference/
# https://sites.google.com/site/picplzapi/
# http://developer.active.com/docs/read/Home  http://developer.active.com/docs/read
# https://www.layar.com/documentation/browser/api/
# https://www.campaignmonitor.com/api/
# https://api.mobbr.com/
# https://dev.groupme.com/


# http://developer.mailchimp.com/documentation/mailchimp/reference/overview/
# https://www.ibm.com/watson/developercloud/conversation/api/v1/



python ./rest_crawler/crawlAllPages.py $1 $2

# predict html page
python ./predict_page/predictExternal.py $1


# run java to build the OPENAPI documentation
# /Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/bin/java -Xms1024m -Xmx10240m -Dgate.plugins.home=/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec\\plugins -Dgate.site.config=/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_specgate.xml -Dfile.encoding=UTF-8 -classpath /Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/target/classes:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/gate-asm-5.0.3.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/gate-compiler-jdt-4.3.2-P20140317-1600.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/gate.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/log4j-1.2.17.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/tika-core-1.7.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/tika-parsers-1.7.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/Information_Retrieval.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/ant-1.9.3.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/ant-launcher-1.9.3.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/aopalliance-1.0.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/apache-mime4j-core-0.7.2.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/apache-mime4j-dom-0.7.2.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/bcmail-jdk15-1.45.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/bcprov-jdk15-1.45.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/commons-codec-1.9.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/commons-compress-1.8.1.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/commons-io-2.4.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/commons-lang-2.6.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/commons-logging-1.1.3.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/flying-saucer-core-9.0.4.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/fontbox-1.8.8.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/hamcrest-core-1.3.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/ivy-2.3.0.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/jackson-annotations-2.3.0.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/jackson-core-2.3.2.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/jackson-databind-2.3.2.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/java-getopt-1.0.13.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/jaxen-1.1.6.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/jdom-1.1.3.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/jempbox-1.8.8.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/joda-time-2.9.2.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/junit-4.11.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/nekohtml-1.9.14.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/pdfbox-1.8.8.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/poi-3.11.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/poi-ooxml-3.11.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/poi-ooxml-schemas-3.11.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/poi-scratchpad-3.11.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/spring-aop-2.5.6.SEC01.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/spring-beans-2.5.6.SEC01.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/spring-core-2.5.6.SEC01.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/stax2-api-3.1.1.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/woodstox-core-lgpl-4.2.0.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/xercesImpl-2.9.1.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/xmlbeans-2.6.0.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/xmlunit-1.5.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/xpp3-1.1.4c.jar:/Users/hanyangcao/Documents/paper/REST/REST_OPENAPI/gen_spec/lib/xstream-1.4.7.jar:/Users/hanyangcao/.m2/repository/com/google/code/gson/gson/2.8.0/gson-2.8.0.jar:/Users/hanyangcao/.m2/repository/com/googlecode/concurrent-trees/concurrent-trees/2.6.0/concurrent-trees-2.6.0.jar:/Users/hanyangcao/.m2/repository/org/json/json/20090211/json-20090211.jar:/Users/hanyangcao/.m2/repository/org/apache/tika/tika-core/1.4/tika-core-1.4.jar:/Users/hanyangcao/.m2/repository/org/apache/commons/commons-lang3/3.0/commons-lang3-3.0.jar:/Users/hanyangcao/.m2/repository/uk/ac/gate/gate-core/6.0/gate-core-6.0.jar:/Users/hanyangcao/.m2/repository/commons-io/commons-io/1.4/commons-io-1.4.jar:/Users/hanyangcao/.m2/repository/log4j/log4j/1.2.16/log4j-1.2.16.jar:/Users/hanyangcao/.m2/repository/commons-lang/commons-lang/2.5/commons-lang-2.5.jar:/Users/hanyangcao/.m2/repository/jdom/jdom/1.0/jdom-1.0.jar:/Users/hanyangcao/.m2/repository/xerces/xercesImpl/2.9.1/xercesImpl-2.9.1.jar:/Users/hanyangcao/.m2/repository/xml-apis/xml-apis/1.3.04/xml-apis-1.3.04.jar:/Users/hanyangcao/.m2/repository/uk/ac/gate/gate-asm/3.1/gate-asm-3.1.jar com.hanyang.ExtractInformation $1
