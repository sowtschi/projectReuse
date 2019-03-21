'use strict';

const putUserRating = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        database.putUserRating(req.params.uId, req.params.id, req.body, err => {
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

module.exports = putUserRating;
