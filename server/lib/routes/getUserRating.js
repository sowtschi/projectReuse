'use strict';

const getUserRating = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.getUserRating(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {

                return res.status(200).send(mappings["rating"]);
            }
        })
    };
};

module.exports = getUserRating;
