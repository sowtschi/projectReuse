'use strict';

const deleteConversation = function(database) {

    return function(req, res) {
        // call database
        database.deleteConversation(req.params.uId, req.params.ouId, err => {
            if (err) {
                console.log("Failed to delete conversation!");
                return res.status(500).send("conversation could not be deleted from database!");
            } else {
                console.log("Successfully deleted conversation!");
                return res.sendStatus(201);
            }
        });

    };
};

module.exports = deleteConversation;
