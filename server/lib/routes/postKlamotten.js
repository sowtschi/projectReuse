'use strict';

const postKlamotten = function(database, firebase) {

    return function(req, res) {
        database.addClothing(JSON.stringify(req.body), firebase, err => {
            if (err) {
                console.log("Failed to add clothing!");
                return res.status(500).send("Clothing could not be added to the database!");
            } else {
                console.log("Successfully added clothing!");
                return res.sendStatus(201);
            }
        });

    };
};

module.exports = postKlamotten;
