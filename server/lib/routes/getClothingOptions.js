'use strict';
var data = require('../clothingOptionsData');


const getClothingOptions = function() {

    if (data == null) {
        throw new Error('Database is missing.');
        console.log("error");
    }

    return function(req, res) {


        res.set("Content-Type", 'application/json').status(200);
        res.send(data.data);
    };
};

module.exports = getClothingOptions;
