'use strict';

const getAllClothingLocation = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        const latitude = req.params.latitude;
        const longitude = req.params.longitude;
        const vicinity = req.params.vicinity;

        database.getAllClothingLocation(latitude, longitude, vicinity, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
                return res.status(200).send(mappings);
            }
        })
    };
};

module.exports = getAllClothingLocation;
