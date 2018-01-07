var express = require("express")

var router = express.Router()

var ConfigController = require("../../controllers/config.controller")


//map each api to the controller functions
router.get("/", ConfigController.getConfigs)

router.post("/", ConfigController.createConfig)

router.put("/", ConfigController.updateConfig)

router.delete("/:id", ConfigController.removeConfig)

module.exports = router;