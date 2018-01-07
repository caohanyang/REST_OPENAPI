var express = require("express")

var router = express.Router()

var configs = require("./api/config.route")


router.use("/configs", configs);


module.exports = router;