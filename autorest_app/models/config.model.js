const mongoose = require("mongoose");
const mongoosePaginate = require("mongoose-paginate");

var ConfigSchema = new mongoose.Schema({
    apiName: String,
    docUrl: String,
    filterUrl: String,
    searchBase: Boolean,
    urlBase: String,
    stuffing: String,
    urlMiddle: String,
    urlAfter: String,
    urlTemplate: String,
    reverse: String,
    existVerb: String,
    urlKey: String,
    verbKey: String,
    mode: String,
    abbrevDelete: String,
    reqKey: String,
    reqMiddle: String,
    reqExample: String,
    url1req2: Boolean,
    reqTemplate: String,
    resKey: String,
    resMiddle: String,
    url1res2: Boolean,
    resTemplate: String,
    resExample: String,
    existPara: Boolean,
    paraKey: String,
    url1para2: Boolean,
    paraMiddle: String,
    paraIn: String,
    template: String,
    number: String
})

ConfigSchema.plugin(mongoosePaginate)

const Config = mongoose.model("Config", ConfigSchema);

module.exports = Config;