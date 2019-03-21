'use strict';

const postUserSearch = function(database) {

    return function(req, res) {
        database.postUserSearch(req.params.uId, req.body, err => {
            if (err) {
                console.log("Failed to add subscription!");
                return res.status(500).send("subscription could not be added to the database!");
            } else {
                console.log("Successfully added subscription!");
                return res.sendStatus(201);
            }

        });

    };
};

module.exports = postUserSearch;
