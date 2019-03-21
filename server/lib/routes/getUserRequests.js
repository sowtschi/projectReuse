'use strict';

const getUserRequests = function(database, firebase) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        database.getUserRequests(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");

            if (err) {
                return res.status(500).end();
            } else {
                return res.status(200).send(mappings);
            }


        })

    };
};

module.exports = getUserRequests;
