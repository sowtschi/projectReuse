'use strict';

const postUserPrefer = function(database) {

    return function(req, res) {
        database.postUserPrefer(req.params.id, req.body, ((err, mappings) => {
            if (err) {
                console.log("Failed to add prefer!");
                return res.status(500).send("prefer could not be added to the database!");
            } else {
                console.log("Successfully added prefer!");
                return res.sendStatus(201);
            }
        }));


    };
};

module.exports = postUserPrefer;
