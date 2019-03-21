'use strict';

const postMessage = function(database, firebase) {

    return function(req, res) {
        database.postMessage(req.params.uId, req.body, firebase, err => {
            if (err) {
                console.log("Failed to add message!");
                return res.status(500).send("Message could not be added to the database!");
            } else {
                console.log("Successfully added message!");
                return res.sendStatus(201);
            }
        });
    };
};

module.exports = postMessage;
