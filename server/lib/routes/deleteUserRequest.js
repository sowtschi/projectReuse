'use strict';

const deleteUserRequest = function(database) {

    return function(req, res) {
        database.deleteUserRequest(req.params.uId, req.params.id, err => {
            if (err) {
                return res.status(500).send("Could not delete request!");
            } else {
                console.log("Successfully deletet request!");
                res.sendStatus(201);
            }
        });
    };
};

module.exports = deleteUserRequest;
