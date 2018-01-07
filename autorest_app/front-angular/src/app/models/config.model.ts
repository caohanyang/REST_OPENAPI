class Config {
    _id: string;
    apiName: string;
    docUrl: string;
    filterUrl: string;
    searchBase: boolean;
    urlBase: string;
    stuffing: string;
    urlMiddle: string;
    urlAfter: string;
    urlTemplate: string;
    reverse: string;
    existVerb: string;
    urlKey: string;
    verbKey: string;
    mode: string;
    abbrevDelete: string;
    reqKey: string;
    reqMiddle: string;
    reqExample: string;
    url1req2: boolean;
    reqTemplate: string;
    resKey: string;
    resMiddle: string;
    url1res2: boolean;
    resTemplate: string;
    resExample: string;
    existPara: boolean;
    paraKey: string;
    url1para2: boolean;
    paraMiddle: string;
    paraIn: string;
    template: string;
    number: string;

    constructor(
    ){
        this.apiName = "Instagram"
        this.docUrl = "https://www.instagram.com/developer"
        this.filterUrl = "https://www.instagram.com/developer/endpoints"
        this.searchBase = false
        this.urlBase = ""
        this.stuffing= "\\s"
        this.urlMiddle= " "
        this.urlAfter= "\n"
        this.urlTemplate= "no"
        this.reverse= "no"
        this.existVerb= "yes"
        this.urlKey= ""
        this.verbKey= ""
        this.mode="https://"
        this.abbrevDelete= "delete"
        this.reqKey= "no"
        this.reqMiddle = ".{0,10}"
        this.reqExample= "((\\{)|(\\[)){1}(.*?)((\\})|(\\])){1}"
        this.url1req2 = true
        this.reqTemplate= "pre"
        this.resKey= "JSON representation"
        this.resMiddle= "\\s.{0,120}"
        this.url1res2 = true
        this.resTemplate= "pre"
        this.resExample= "((\\{)|(\\[)){1}(.*?)((\\})|(\\])){1}"
        this.existPara= true
        this.paraKey= "parameters"
        this.url1para2= true
        this.paraMiddle= "13"
        this.paraIn= "query"
        this.template= "table"
        this.number= "multiple"
    }
}

export default Config;