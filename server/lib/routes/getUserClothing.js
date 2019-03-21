'use strict';

const getUserClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        database.getUserClothing(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
                // Füge Hypermedialinks hinzu
                var profile = {
                    href: "/user/" + req.params.uId
                };
                mappings.push = {
                    _links: profile
                };
                return res.status(200).send(mappings);
            }
        })

    };
};

module.exports = getUserClothing;
