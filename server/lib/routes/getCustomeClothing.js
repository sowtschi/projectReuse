'use strict';

const getCustomeClothing = function(database) {
    if (!database) {
        throw new Error('Database is missing.');
    }

    return function(req, res) {

        var color = req.params.color;
        var style = req.params.style;
        var size = req.params.size;
        var brand = req.params.brand;
        var art = req.params.art;

        var search = {
            color: color,
            style: style,
            size: size,
            brand: brand,
            art: art,
            type: "clothing"
        };

        // Enteferne Attribute die nicht gesetzt wurden
        Object.keys(search).forEach(function(key) {
            if (search[key] == 0) {
                delete search[key];
            }
        });

        database.getCustomeClothing(search, req.params.latitude, req.params.longitude, req.params.vicinity, (err, mappings) => {
            res.setHeader("Content-Type", "application/json");
            if (err) {
                return res.status(500).end();
            } else {
                return res.status(200).send(mappings);
            }
        })

    };
};

module.exports = getCustomeClothing;
