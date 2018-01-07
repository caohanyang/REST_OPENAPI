var ConfigService = require("../services/config.service");
const exec = require('child_process').exec;

_this = this


exports.getConfigs = async function (req, res, next) {
    var page = req.query.page ? req.query.page : 1;
    var limit = req.query.limit ? req.query.limit : 10;

    try {

        var configs = await ConfigService.getConfigs({}, page, limit);

        return res.status(200).json({ status: 200, data: configs, message: "Succesfully Configs received" })

    } catch (e) {
        return res.status(400).json({ status: 400, message: e.message })
    }

}

exports.createConfig = async function (req, res, next) {

    var config = {
        apiName: req.body.apiName,
        docUrl: req.body.docUrl,
        filterUrl: req.body.filterUrl,
        searchBase: req.body.searchBase,
        urlBase: req.body.urlBase,
        stuffing: req.body.stuffing,
        urlMiddle: req.body.urlMiddle,
        urlAfter: req.body.urlAfter,
        urlTemplate: req.body.urlTemplate,
        reverse: req.body.reverse,
        existVerb: req.body.existVerb,
        urlKey: req.body.urlKey,
        verbKey: req.body.verbKey,
        mode: req.body.mode,
        abbrevDelete: req.body.abbrevDelete,
        reqKey: req.body.reqKey,
        reqMiddle: req.body.reqMiddle,
        reqExample: req.body.reqExample,
        url1req2: req.body.url1req2,
        reqTemplate: req.body.reqTemplate,
        resKey: req.body.resKey,
        resMiddle: req.body.resMiddle,
        url1res2: req.body.url1res2,
        resTemplate: req.body.resTemplate,
        resExample: req.body.resExample,
        existPara: req.body.existPara,
        paraKey: req.body.paraKey,
        url1para2: req.body.url1para2,
        paraMiddle: req.body.paraMiddle,
        paraIn: req.body.paraIn,
        template: req.body.template,
        number: req.body.number,
    }


    try {
        var createdConfig = await ConfigService.createConfig(config)

        res.status(200).json({ status: 200, data: JSON.stringify(createdConfig), message: "successfully create config" })

    } catch (e) {
        console.log(e.message)
        res.status(400).json({ status: 400, message: e.message })
    }

    console.log("start to execute 1");
    
    var runscript = exec('sh ../openapi.sh ' + config.docUrl + ' ' + config.filterUrl ,
        (error, stdout, stderr) => {

            console.log("start to execute 2");
            // callback
            console.log(`${stdout}`);
            console.log(`${stderr}`);
            if (error !== null) {
                console.log(`exec error: ${error}`);
            }

            console.log("finish execute 2");

        })
    console.log("finish execute 1");
}

exports.updateConfig = async function (req, res, next) {

    if (!req.body._id) {
        res.status(400).json({ status: 400, message: "id must be present" })
    }

    var id = req.body._id;

    console.log(req.body);

    var newConfig = {
        id,
        apiName: req.body.apiName ? req.body.apiName : null,
        docUrl: req.body.docUrl ? req.body.docUrl : null,
        filterUrl: req.body.filterUrl ? req.body.filterUrl : null,
        searchBase: req.body.searchBase ? req.body.searchBase : null,
        urlBase: req.body.urlBase ? req.body.urlBase : null,
        stuffing: req.body.stuffing ? req.body.stuffing : null,
        urlMiddle: req.body.urlMiddle ? req.body.urlMiddle : null,
        urlAfter: req.body.urlAfter ? req.body.urlAfter : null,
        urlTemplate: req.body.urlTemplate ? req.body.urlTemplate : null,
        reverse: req.body.reverse ? req.body.reverse : null,
        existVerb: req.body.existVerb ? req.body.existVerb : null,
        urlKey: req.body.urlKey ? req.body.urlKey : null,
        verbKey: req.body.verbKey ? req.body.verbKey : null,
        mode: req.body.mode ? req.body.mode : null,
        abbrevDelete: req.body.abbrevDelete ? req.body.abbrevDelete : null,
        reqKey: req.body.reqKey ? req.body.reqKey : null,
        reqMiddle: req.body.reqMiddle ? req.body.reqMiddle : null,
        reqExample: req.body.reqExample ? req.body.reqExample : null,
        url1req2: req.body.url1req2 ? req.body.url1req2 : null,
        reqTemplate: req.body.reqTemplate ? req.body.reqTemplate : null,
        resKey: req.body.resKey ? req.body.resKey : null,
        resMiddle: req.body.resMiddle ? req.body.resMiddle : null,
        url1res2: req.body.url1res2 ? req.body.url1res2 : null,
        resTemplate: req.body.resTemplate ? req.body.resTemplate : null,
        resExample: req.body.resExample ? req.body.resExample : null,
        existPara: req.body.existPara ? req.body.existPara : null,
        paraKey: req.body.paraKey ? req.body.paraKey : null,
        url1para2: req.body.url1para2 ? req.body.url1para2 : null,
        paraMiddle: req.body.paraMiddle ? req.body.paraMiddle : null,
        paraIn: req.body.paraIn ? req.body.paraIn : null,
        template: req.body.template ? req.body.template : null,
        number: req.body.number ? req.body.number : null,
    }

    try {
        var updateConfig = await ConfigService.updateConfig(newConfig)

        return res.status(200).json({ status: 200, data: updateConfig, message: "successfully update config" })

    } catch (e) {
        return res.status(400).json({ status: 400, message: e.message })
    }
}

exports.removeConfig = async function (req, res, next) {
    var id = req.params.id

    console.log(id);
    try {
        var deleted = await ConfigService.deleteConfig(id);
        return res.status(204).json({ status: 204, message: "succeddfully delete config" })

    } catch (e) {
        return res.status(400).json({ status: 400, message: e.message })
    }
}