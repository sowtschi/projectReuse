'use strict';

const getUserProfile = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {
        database.getUserProfile(req.params.id, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
                var requests = {
                    href: "/user/" + req.params.id + "/requests"
                };
                var clothing = {
                    href: "/user/" + req.params.id + "/clothing"
                };
                return res.status(200).send(mappings);
            }
        })

    };
};

module.exports = getUserProfile;
