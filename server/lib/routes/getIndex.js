'use strict';

const getIndex = function() {

    return function(req, res) {
        res.status(200).send("Welcome!");
    };
};

module.exports = getIndex;
