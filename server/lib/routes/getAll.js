'use strict';

const getAll = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        database.getAll((err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
                console.log(mappings);
                return res.status(200).send(mappings);
            }
        })
    };
};

module.exports = getAll;
