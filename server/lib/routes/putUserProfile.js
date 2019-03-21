'use strict';

const putUserProfile = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.putUserProfile(req.params.uId, req.body, err => {
            if (err) {
                console.log("Failed to edit Userprofile!");
                return res.status(500).send("Userprofile could not be edited!");
            } else {
                console.log("Successfully edited Userprofile!");
                return res.sendStatus(201);
            }
        });

    };
};

module.exports = putUserProfile;
