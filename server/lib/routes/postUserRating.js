'use strict';

const postUserRating = function(database) {

    return function(req, res) {
        database.postUserRating(req.params.uId, req.body, err => {
            if (err) {
                console.log("Failed to add rating!");
                return res.status(500).send("Rating could not be added to the database!");
            } else {
                console.log("Successfully added rating!");
                return res.sendStatus(201);
            }
        });
    };
};

module.exports = postUserRating;
