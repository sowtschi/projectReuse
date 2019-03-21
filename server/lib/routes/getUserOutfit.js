'use strict';

const getUserOutfit = function(database, choise) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        database.getUserOutfit(req.params.uId, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
              var mappings_new = [];
              console.log(mappings);
              for (var single_map in mappings) {
                  if (mappings_new.indexOf(mappings[single_map].oId) == -1 ) {
                    mappings_new.push(mappings[single_map].oId);
                  }
               }
              return res.status(200).send(mappings_new);
            }
        })


    };
};

module.exports = getUserOutfit;
