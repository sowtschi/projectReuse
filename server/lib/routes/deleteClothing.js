'use strict';

const deleteClothing = function(database) {

    return function(req, res) {
        // call database
        database.deleteClothing(req.params.uId, err => {
            if (err) {
                console.log("Failed to delete clothing!");
                return res.status(500).send("clothing could not be deleted from database!");
            } else {
                console.log("Successfully deleted clothing!");
                return res.sendStatus(201);
            }
        });

    };
};

module.exports = deleteClothing;
