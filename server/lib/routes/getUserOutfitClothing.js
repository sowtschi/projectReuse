'use strict';

const getUserOutfitClothing = function(database, choise) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.getUserOutfitClothing(req.params.uId, req.params.oId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
                return res.status(200).send(mappings);
            }
        })
    };
};

module.exports = getUserOutfitClothing;
