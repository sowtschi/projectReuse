'use strict';

const putClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.putClothing(req.params.cId, req.body, err => {
            if (err) {
                console.log("Failed to edit request!");
                return res.status(500).send("request could not be edited!");
            }
            console.log("Successfully edited clothing!");
        });
        return res.sendStatus(201);
    };
};

module.exports = putClothing;
