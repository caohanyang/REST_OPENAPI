var Config = require("../models/config.model");


// Saving the context of this module inside the _the variable
_this = this;

exports.getConfigs = async function(query, page, limit) {

    var option = {
        page,
        limit
    }

    try {
        var configs = await Config.paginate(query, option);

        return configs;
    } catch (e) {
        throw Error("Error when paginating the configs");
    }
}

exports.createConfig = async function (config) {
    
    var newConfig = new Config({
        apiName: config.apiName,
        docUrl: config.docUrl,
        filterUrl: config.filterUrl,
        searchBase: config.searchBase,
        urlBase: config.urlBase,
        stuffing: config.stuffing,
        urlMiddle: config.urlMiddle,
        urlAfter: config.urlAfter,
        urlTemplate: config.urlTemplate,
        reverse: config.reverse,
        existVerb: config.existVerb,
        urlKey: config.urlKey,
        verbKey: config.verbKey,
        mode: config.mode,
        abbrevDelete: config.abbrevDelete,
        reqKey: config.reqKey,
        reqMiddle: config.reqMiddle,
        reqExample: config.reqExample,
        url1req2: config.url1req2,
        reqTemplate: config.reqTemplate,
        resKey: config.resKey,
        resMiddle: config.resMiddle,
        url1res2: config.url1res2,
        resTemplate: config.resTemplate,
        resExample: config.resExample,
        existPara: config.existPara,
        paraKey: config.paraKey,
        url1para2: config.url1para2,
        paraMiddle: config.paraMiddle,
        paraIn: config.paraIn,
        template: config.template,
        number: config.number
    })

    try {
        var savedConfig = await newConfig.save()

        return savedConfig;
    } catch (e) {
        throw Error("Error while Creating config");
    }
}

exports.updateConfig = async function(config) {
    var id = config.id;

    try {
        var oldConfig = await Config.findById(id);

    } catch (e) {
        throw Error("Error while finding the config")
    }

    if (!oldConfig) {
        return false;
    }


    oldConfig.apiName = config.apiName
    oldConfig.docUrl = config.docUrl
    oldConfig.filterUrl= config.filterUrl
    oldConfig.searchBase= config.searchBase
    oldConfig.urlBase= config.urlBase
    oldConfig.stuffing= config.stuffing
    oldConfig.urlMiddle= config.urlMiddle
    oldConfig.urlAfter= config.urlAfter
    oldConfig.urlTemplate= config.urlTemplate
    oldConfig.reverse= config.reverse
    oldConfig.existVerb= config.existVerb
    oldConfig.urlKey= config.urlKey
    oldConfig.verbKey= config.verbKey
    oldConfig.mode= config.mode
    oldConfig.abbrevDelete= config.abbrevDelete
    oldConfig.reqKey= config.reqKey
    oldConfig.reqMiddle= config.reqMiddle
    oldConfig.reqExample= config.reqExample
    oldConfig.url1req2= config.url1req2
    oldConfig.reqTemplate= config.reqTemplate
    oldConfig.resKey= config.resKey
    oldConfig.resMiddle= config.resMiddle
    oldConfig.url1res2= config.url1res2
    oldConfig.resTemplate= config.resTemplate
    oldConfig.resExample= config.resExample
    oldConfig.existPara= config.existPara
    oldConfig.paraKey= config.paraKey
    oldConfig.url1para2= config.url1para2
    oldConfig.araMiddle= config.paraMiddle
    oldConfig.paraIn= config.paraIn
    oldConfig.template= config.template
    oldConfig.number= config.number

    // console.log(oldConfig);

    try {
        var savedConfig = await oldConfig.save();

        return savedConfig
    } catch (e) {
        console.log(e)
        throw Error("Error when updating the Config");
    }

}

exports.deleteConfig = async function (id) {

    console.log(id);
    try {
        var deleted = await Config.remove({_id: id})

        if (deleted.result.n === 0) {
            throw Error("Config could not be deleted")
        }

        return deleted;
    } catch (e) {
        throw Error("Error while deleting the config")
    }
}
