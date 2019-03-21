'use strict';

const putRequest = function(database, firebase) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        database.putRequest(req.body, req.params.uId, req.params.id, firebase, err => {
            if (err) {
                console.log("Failed to edit request!");
                return res.status(500).send("request could not be edited!");
            } else {
                console.log("Successfully edited request!");
                return res.sendStatus(201);
            }
        });

    };
};

module.exports = putRequest;
