'use strict';

const mongo = require('mongodb');
const calcDistance = require('./calcDistance');
const calcOutfit = require('./calcOutfit');
const sendPushNotification = require('./sendPushNotification');
const MongoClient = mongo.MongoClient;
const uuidv4 = require('uuid/v4');
const async = require("async");


// Datenbank initialisieren
const database = {
    initialize(connectionString, callback) {
        MongoClient.connect(connectionString, {
            autoReconnect: true
        }, (err, database) => {
            if (err) {
                return callback(err);
            }
            if (!connectionString) {
                throw new Error('connectionString is missing.');
            }
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            // Kollektionsnamen festlegen
            const mappings = database.collection('mappings');
            this.mappings = mappings;
            callback(null);
        });
    },

    // Suche einzelnes Kleidungsstück anhand der Kleidungs ID (cID)
    getClothing(cId, callback) {
        if (!cId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        this.mappings.findOne({
            type: "clothing",
            id: cId
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            callback(null, mappings);
        })
    },
    // Suche nach Kleidungsstücken mit Attributfilter+Distanzfilter
    getCustomeClothing(filter, latitude, longitude, vicinity, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!latitude) {
            throw new Error('latitude is missing.');
            callback(err);
        }
        if (!longitude) {
            throw new Error('longitude is missing.');
            callback(err);
        }
        if (!vicinity) {
            throw new Error('vicinity is missing.');
            callback(err);
        }
        if (!filter) {
            throw new Error('filter is missing.');
            callback(err);
        }
        var type = {
            type: "clothing"
        };
        this.mappings.find(
            filter
        ).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            // Filter Kleidungsstücke nach Distanz
            calcClothingDistance(mappings, latitude, longitude, vicinity, function(mappings_new) {
                callback(null, mappings_new);
            });

        })
    },
    // Suche nach Kleidungsstücken eines bestimmten Benutzers(uId), die zu einem bestimmten Outfit(oId) gehören
    getUserOutfitClothing(uId, oId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!oId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        this.mappings.find({
            type: "clothing",
            uId: uId,
            active: "false",
            oId: oId
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            callback(null, mappings);
        })
    },
    // Suche nach Outfits eines bestimmten Users(uId)
    getUserOutfit(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({
            type: "clothing",
            uId: uId,
            active: "false"
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            callback(null, mappings);
        })
    },
    // Suche nach Profil eines bestimmten Benutzers(uId)
    getUserProfile(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        this.mappings.findOne({
            type: "userprofile",
            uId: uId
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            callback(null, mappings);
        })
    },
    // Requests eines bestimmten Benutzers(uId) finden
    getUserRequests(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements


        async.waterfall([
            // Suche eigene Requests
            async.apply(findOwnRequests, this.mappings, uId),
            // Suche fremde Requests
            async.apply(findOtherRequests, this.mappings),
            // Suche nach zugehörigen Kleidungsattributen
            async.apply(searchClothing, this.mappings)
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                return callback(null, result);
            }
        });

        function findOwnRequests(mappings, uId, callback) {
            if (!callback) {
                throw new Error('callback is missing.');
            }
            if (!mappings) {
                return callback("1", null);
            }
            if (!uId) {
                return callback("1", null);
            }
            mappings.find({
                type: "userprofile",
                "requests.ouId": uId
            }).toArray((err, mapping) => {
                if (err) {
                    callback("1", null);
                } else {
                    callback(null, mapping);
                }
            })
        }

        function findOtherRequests(mappings, mapping, callback) {
            if (!callback) {
                throw new Error('callback is missing.');
            }
            if (!mapping) {
                return callback("1", null);
            }
            if (!mappings) {
                return callback("1", null);
            }
            var requests = [];

            /** IDs des Requests wird vertauscht, damit eine homogene Datenstruktur für die Requests erhalten bleibt.
             * Das neue Element "from" enthält anschließend die Information, ob es sich um einen Fremden oder einen
             * eigenen Request handelt */

            for (var single_map in mapping) {
                var obj = mapping[single_map].requests;
                for (var single_request in obj) {
                    if (obj[single_request].ouId == uId) {
                        obj[single_request].from = "foreign";
                        requests.push(obj[single_request]);
                    }
                }
            }
            mappings.findOne({
                type: "userprofile",
                uId: uId
            }, (err, mappings) => {
                if (err) {
                    callback("1", null);
                }
                // Löscht Bilder aus den Datensätzen, da diese nicht gebraucht werden
                for (var single_profile in mappings) {
                    delete mappings.image;
                }
                /** Das neue Element "from" enthält anschließend die Information, ob es sich um einen Fremden oder einen
                 * eigenen Request handelt */
                if (mappings !== null) {
                    for (var single_ownReq in mappings.requests) {
                        mappings.requests[single_ownReq].from = "own";
                        requests.push(mappings.requests[single_ownReq]);
                    }
                }
                //send results back to handler
                callback(null, requests);
            })
        }

        function searchClothing(mappings, requests, callback) {
            if (!callback) {
                return callback("1", null);
            }
            if (!mappings) {
                return callback("1", null);
            }
            if (!requests) {
                return callback("1", null);
            }
            mappings.find({
                type: "clothing",
            }).toArray((err, mapping) => {
                if (err) {
                    callback("1", null);
                }
                var results = [];
                // Durchlaufe die einzelnen Requests
                for (var single_req in requests) {
                    // Durchlaufe die einzelnen Kleidungsstücke
                    for (var single_clothing in mapping) {
                        // Wenn das Kleidungsstück des Request gefunden wurde, werden Kleidungsattribute dem Request hinzugefügt
                        if (mapping[single_clothing].id == requests[single_req].cId) {

                            delete mapping[single_clothing].uId;
                            delete mapping[single_clothing].id;
                            delete mapping[single_clothing].uId;

                            var obj = Object.assign(requests[single_req], mapping[single_clothing]);
                            results.push(obj);

                        }
                    }

                }
                //send results back to handler
                callback(null, results);
            })
        }

    },

    // Funktion um Userprofile zu bearbeiten
    putUserProfile(uId, put, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
            callback(err);
        }
        // find all elements

        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $set: put
        }, (err) => {
            if (err) {
                callback(err);
            } else {
                callback(null);
            }
        })
    },

    // Anpassung von Bewertungen
    putUserRating(uId, id, put, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!put) {
            throw new Error('put is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('callback is missing.');
        }
        this.mappings.update({
            type: "userprofile",
            uId: uId,
            "rating.id": id
        }, {
            $push: {
                rating: put
            }
        }, (err) => {
            if (err) {
                callback(err);
            } else {
                callback(null)
            }
        })
    },

    /* PutRequest passt den Status eines Requests an und sendet Benachrichtigung über die Statusänderung */
    putRequest(body, uId, id, firebase, callback) {
        if (!body) {
            throw new Error('body is missing.');
            callback(err);
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('callback is missing.');
        }


        async.waterfall([
            // Sucht nach dem anzupassenden Request
            async.apply(findRequest, this.mappings, id, body),
            // Sendet Benachrichtigung über die Statusänderung
            async.apply(sendPush, this.mappings, body, firebase),
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                return callback(null, result);
            }
        });

        function findRequest(mappings, id, body, callback) {
            if (!body) {
                return callback("1", null);
            }
            if (!mappings) {
                return callback("1", null);
            }
            if (!id) {
                return callback("1", null);
            }
            if (!callback) {
                throw new Error('callback is missing.');
            }
            mappings.update({
                type: "userprofile",
                "requests.id": id,

            }, {
                $set: {
                    "requests.$.status": body.status,
                    "requests.$.confirmed": body.confirmed

                }
            }, (err) => {
                if (err) {
                    callback("1");
                } else {


                    callback(null)
                }
            })
        }

        function sendPush(mappings, body, firebase, callback) {
            if (!body) {
                return callback("1", null);
            }
            if (!mappings) {
                return callback("1", null);
            }
            if (!firebase) {
                return callback("1", null);
            }
            if (!callback) {
                throw new Error('callback is missing.');
            }
            mappings.findOne({
                uId: body.uId,
                type: "token"
            }, (err, mappings) => {
                if (err) {
                    callback(null);
                } else {
                    sendPushNotification(mappings.token, body.uId, mappings, "", body.status, firebase);
                    callback(null);
                }

            })
        }
    },

    // Trage neue Werte für Kleidung ein, die bearbeitet wurde
    putClothing(cId, put, callback) {
        if (!cId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!put) {
            throw new Error('put is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.update({
            type: "clothing",
            id: cId
        }, {
            $set: put,
        }, (err) => {
            if (err) {
                callback(err);
            } else {
                callback(null)
            }
        })
    },
    // Suche eine Liste von KLeidungsstücken, die einem bestimmten Benutzer(uId) gehören.
    getUserClothing(uId, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({
            type: "clothing",
            uId: uId
        }).toArray((err, mappings) => {

            if (err) {
                return callback(err);
            }
            // Lösche Bilder aus dem Kleidungsdatensatz
            for (var single_mappings in mappings)
                delete mappings[single_mappings].image;

            callback(null, mappings);
        })
    },
    // send all DB-Values
    getAll(callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.find({}).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },

    // Suche nach Bewertungen eines bestimmten(uId) Benutzers
    getUserRating(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.findOne({
            type: "userprofile",
            uId: uId
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },

    // Berechne Outfit für einen Bestimmten Nutzungskontext
    getOutfit(choise, art, params, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!art) {
            throw new Error('art is missing.');
            callback(err);
        }
        if (!params) {
            throw new Error('params is missing.');
            callback(err);
        }
        if (!choise) {
            throw new Error('choise is missing.');
            callback(err);
        }

        async.waterfall([
            // Suche nach allen Kleidungsstücken
            async.apply(findClothing, this.mappings),
            // Filtere Kleidungsstücke nach Distanz
            async.apply(calcDistance, params),
            // Berechne ein Outfit aus den verbleibenden Kleidungsstücken
            async.apply(calcNewOutfit, params)
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                callback(null, result);
            }
        });

        function findClothing(mappings, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!mappings) {
                return callback("1", null);
            }
            mappings.find({
                type: "clothing"
            }).toArray((err, clothing) => {
                if (err) {
                    callback("1", null);
                } else {

                    callback(null, clothing);
                }
            })
        }

        function calcDistance(params, mappings, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!mappings) {
                return callback("1", null);
            }
            if (!params) {
                return callback("1", null);
            }

            function queryCollection(mappings, params, callback) {
                calcClothingDistance(mappings, params.latitude, params.longitude, params.vicinity, function(mappings_new) {
                    callback(mappings_new);
                });
            }

            queryCollection(mappings, params, function(mappings_new) {
                if (mappings_new == "1") {
                    callback("1");
                } else {
                    callback(null, mappings_new);
                }
            });
        }

        function calcNewOutfit(params, mappings, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!mappings) {
                return callback("1", null);
            }
            if (!params) {
                return callback("1", null);
            }

            function queryCollection(mappings, callback) {
                calcOutfit("winter", mappings, false, params.gender, params.hSize, params.tSize, params.bSize, params.sSize, function(mappings_new) {
                    callback(mappings_new);
                });
            }

            queryCollection(mappings, function(mappings_new) {
                callback(null, mappings_new);
            });
        }
    },
    // Kleidungssuche mit Distanzfilter
    getAllClothingLocation(latitude, longitude, vicinity, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!latitude) {
            throw new Error('latitude is missing.');
            callback(err);
        }
        if (!longitude) {
            throw new Error('longitude is missing.');
            callback(err);
        }
        if (!vicinity) {
            throw new Error('vicinity is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.find({
            type: "clothing"
        }).toArray((err, mappings) => {
            if (err) {
                return callback(err);
            }

            // Speichert Kleidungsstücke, die innerhalb der angegeben Distanz(vicinity) liegen
            var mappings_new = [];
            delete mappings.image;

            // Durchlaufe alle Kleidungsstücke
            for (var i = 0; i < mappings.length; i++) {
                // Berechne Distanz zum angegeben Ort(longitude/latitude)
                var distance = calcDistance(mappings[i].latitude, mappings[i].longitude, latitude, longitude);
                // Wenn die Distanz innerhalb der angegeben maximalen Entfernung liegt
                if (distance <= vicinity) {
                    // Füge Entfernung zum Kleidungsstück hinzu
                    mappings[i].distance = distance;
                    // Füge Kleidungsstück zur neuen Liste hinzu
                    mappings_new.push(mappings[i]);
                }
            }
            callback(null, mappings_new);
        })
    },
    // Füge Kleidungsstück zum System hinzu und überprüft, ob ein Kleidungsstück von einer anderen Person gesucht wird
    addClothing(clothing, firebase, callback) {
        if (!clothing) {
            throw new Error('Clothing is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }

        try {
            // Wandelt übergebene Attribute des Kleidungsstück in JSON und speichert die einzelnen Werte anschließend als JavaScript Object
            clothing = JSON.parse(clothing);
            const mapping = {
                id: uuidv4(),
                longitude: clothing["longitude"],
                latitude: clothing["latitude"],
                size: clothing["size"],
                art: clothing["art"],
                color: clothing["colour"],
                style: clothing["style"],
                gender: clothing["gender"],
                fabric: clothing["fabric"],
                notes: clothing["notes"],
                brand: clothing["brand"],
                date: Date.now(),
                postalCode: clothing["postalCode"],
                city: clothing["city"],
                uId: clothing["uId"],
                type: "clothing",
                image: clothing["image"]
            };

            async.waterfall([
                // Füge Kleidung zur Datenbank hinzu
                async.apply(insertClothing, this.mappings),
                // Suche nach Modellen, für die das Kleidungsstück infrage kommt
                searchFits,
                // Suche Benutzer, die das Kleidungsstück gebrauchen könnten
                async.apply(findUsers, this.mappings),
                // Sende Suchenden Benutzern eine Nachricht
                async.apply(sendPush, this.mappings, firebase),
            ], function(err) {
                /* Wenn "err" eine "2" enthält, ist bei der Suche nach Personen, die das Kleidungsstück gebrauchen können etwas schief gelaufen,
                 * oder es wurde kein Benutzer gefunden, der das Kleidungsstück gebrauchen kann. Da das Kleidungsstück trotzdem erfolgreich eingetragen wurde,
                 * wird in diesem Fall dennoch eine positive Rückantwort gesendet*/
                if (err == "1") {
                    return callback(err);
                } else {
                    callback(null);
                }
            });

            function insertClothing(mappings, callback) {
                if (!callback) {
                    throw new Error('Callback is missing.');
                }
                if (!mappings) {
                    callback("1", null);
                }

                mappings.insertOne(mapping, err => {
                    if (err) {
                        callback("1");
                    } else {

                        callback(null, mappings, mapping);
                    }
                })

            }

            function searchFits(mappings, mapping, callback) {
                if (!callback) {
                    throw new Error('Callback is missing.');
                }
                if (!mappings) {
                    callback("1", null);
                }
                if (!mapping) {
                    callback("1", null);
                }
                // Suche nach Modellen, dessen Restriktionen vom neu erstellen Kleidungsstück erfüllt werden
                var fits = calcOutfit(null, mapping, true, null);
                callback(null, mapping, fits);
            }

            function findUsers(mappings, mapping, fits, callback) {
                if (!callback) {
                    throw new Error('Callback is missing.');
                }
                if (!mappings) {
                    callback("2", null);
                }
                if (!mapping) {
                    callback("2", null);
                }
                if (!fits) {
                    callback("2", null);
                }

                function queryCollection(mappings, callback) {
                    mappings.find({
                        type: "userprofile"
                    }).toArray(function(err, users) {
                        if (err) {
                            callback("2");
                        } else if (users.length > 0) {
                            callback(users);
                        }
                    });
                }

                queryCollection(mappings, function(users) {
                    callback(null, mapping, fits, users);
                });
            }

            function sendPush(mappings, firebase, mapping, fits, users, callback) {
                if (!callback) {
                    throw new Error('Callback is missing.');
                }
                if (!mappings) {
                    callback("2", null);
                }
                if (!firebase) {
                    callback("2", null);
                }
                if (!mapping) {
                    callback("2", null);
                }
                if (!users) {
                    callback("2", null);
                }
                if (!fits) {
                    callback("2", null);
                }

                try {
                    // Durchlaufe die einzelnen Benutzerprofile
                    for (var single_mapping in users) {
                        // Wenn der Benutzer Suchanfragen erstellt hat
                        if (users[single_mapping].subscription != null) {
                            // Durchlaufe die einzelnen Suchanfragen
                            for (var single_subscription in users[single_mapping].subscription) {
                                var i = 0;
                                // Durchlaufe Modelle, für die das Kleidungsstück sinnvoll ist
                                for (var single_fit in fits) {
                                    // Wenn der Benutzer eine für das Kleidungsstück gültiges Modell sucht
                                    if (fits[single_fit].model == users[single_mapping].subscription[single_subscription].type + "_" + users[single_mapping].subscription[single_subscription].missing) {
                                        // Suche nach Usertoken um PushNotification zu senden
                                        mappings.findOne({
                                            uId: users[single_mapping].uId,
                                            type: "token"
                                        }, (err, token) => {
                                            if (err) {
                                                callback("2");
                                            } else {
                                                sendPushNotification(token.token, mapping.id, mapping, fits[single_fit], "missing", firebase);
                                            }
                                        })

                                    }
                                }
                            }
                        }

                    }

                    callback(null);
                } catch (e) {
                    callback("2");
                }

            }
        } catch (e) {
            callback(err);
        }




    },
    // Fügt eine Anfrage auf ein bestimmtes Kleidungsstück hinzu
    postRequest(cId, body, firebase, callback) {
        if (!cId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!body) {
            throw new Error('body is missing.');
            callback(err);
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            cId: cId,
            uId: body.uId,
            ouId: body.ouId,
            status: "open",
            confirmed: "0",
            closed: "0",
            finished: "0"
        };
        //write mapping to Database

        async.waterfall([
            // Suche nach requests des Benutzers
            async.apply(findRequest, this.mappings, mapping),
            // Füge Request zum Userprofile hinzu
            async.apply(findUserProfile, this.mappings, mapping),
            // Sende dem Anbieter des Kleidungsstück eine Nachricht, dass jemand an seinem Kleidungsstück interessiert ist
            async.apply(sendMessage, this.mappings, body.ouId, mapping.cId)
        ], function(err) {
            if (err == "1") {
                return callback(err);
            } else {
                callback(null);
            }
        });


        function findRequest(mappings, mapping, callback) {

            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!mappings) {
                callback("1", null);
            }
            if (!mapping) {
                callback("1", null);
            }
            var flag = "0";
            mappings.findOne({
                uId: mapping.uId,
                type: "userprofile",
            }, (err, userprofile) => {
                if (err) {
                    callback("1", null);
                } else {
                    // Durchlaufe einzelne Requests des Benutzers wenn requests vorhanden sind

                    if (typeof userprofile.requests !== 'undefined') {
                        for (var single_req in userprofile.requests) {
                            // Wenn bereits ein Request auf dieses Kleidungsstück existiert
                            if (userprofile.requests[single_req].cId == mapping.cId && userprofile.requests[single_req]["ouId"] == mapping.ouId) {
                                // Kennzeichne, das passender request gefunden wurde
                                flag = "1";
                            }

                        }
                    }
                    // Führe Operation fort oder beende sie, je nachdem, ob Requests gefunden wurden
                    if (flag == "0") {
                        callback(null);
                    } else {
                        callback("1", null);
                    }
                }
            })
        }

        function findUserProfile(mappings, mapping, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!mappings) {
                callback("1", null);
            }
            if (!mapping) {
                callback("1", null);
            }
            mappings.update({
                type: "userprofile",
                uId: mapping.uId
            }, {
                $push: {
                    requests: mapping
                }
            }, (err) => {
                if (err) {
                    callback("1", null);
                } else {
                    callback(null);
                }
            })
        }

        function sendMessage(mappings, uId, cId, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!mappings) {
                callback("1", null);
            }
            if (!uId) {
                callback("1", null);
            }
            if (!cId) {
                callback("1", null);
            }
            mappings.findOne({
                uId: uId,
                type: "token"
            }, (err, mappings) => {
                if (err) {
                    callback("1", null);
                }
                sendPushNotification(mappings.token, cId, uId, "", "postRequest", firebase);
                callback(null);
            });
        }




    },
    // Speichert neuen Usertoken der Benutzer
    postUserToken(id, token, callback) {
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!token) {
            throw new Error('Token is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            uId: id,
            token: token,
            type: "token"
        };
        //write mapping to Database
        this.mappings.insertOne(mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null);
        });
    },

    // Trage Suchanfragen(body) ein
    postUserSearch(uId, body, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!body) {
            throw new Error('body is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        //write mapping to Database
        const mapping = {
            id: uuidv4(),
            type: body.model,
            missing: body.missing,
            time: "heute"
        };

        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $push: {
                subscription: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null);
        });
    },
    // Füge deine neue Nachricht(message) für einen bestimmten User(uId) hinzu und sende Pushnachricht an den Empfänger
    postMessage(uId, message, firebase, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!message) {
            throw new Error('message is missing.');
            callback(err);
        }
        if (!firebase) {
            throw new Error('firebase is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            from: message["from"],
            to: message["to"],
            rId: message["rId"],
            message: message["message"],
            attach: message["attach"],
            time: message["time"]
        };

        this.mappings.update({
            type: "userprofile",
            uId: message["from"]
        }, {
            $push: {
                messages: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }

        });
        this.mappings.findOne({
            uId: mapping["to"],
            type: "token"
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }

            sendPushNotification(mappings.token, message["to"], message, "", "message", firebase);
        })
        callback(null);
    },
    // Speicher Bewertung(rating) eines bestimmten Users(uId)
    postUserRating(uId, rating, callback) {
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!rating) {
            throw new Error('rating is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            type: "rating",
            from: rating["from"],
            choice: rating["choice"],
            comment: rating["comment"],
            tId: rating["tId"],
            time: rating["time"],
            rfrom: rating["rfrom"],
            finished: rating["finished"]
        };
        //write mapping to Database
        this.mappings.update({
            type: "userprofile",
            uId: uId
        }, {
            $push: {
                rating: mapping
            }
        }, mapping, err => {
            if (err) {
                return callback(err);
            }
            this.mappings.update({
                type: "userprofile",
                "requests.id": rating["tId"],

            }, {
                $set: {
                    "requests.$.status": "closed",
                    "requests.$.closed": rating["rFrom"],
                    "requests.$.finished": rating["finished"]
                }
            }, (err) => {
                if (err) {
                    callback("1");
                } else {
                    callback(null);
                }
            })

        });
    },

    // Löscht bestimmte Anfragen(id) eines bestimmten Benutzers(uId)
    deleteUserRequest(uId, id, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!id) {
            throw new Error('id is missing.');
            callback(err);
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }


        this.mappings.update({
                uId: uId,
                type: "userprofile"
            }, {
                $pull: {
                    requests: {
                        id: id
                    }
                }
            }, {},
            err => {
                if (err) {
                    return callback(err);
                }
                //send results back to handler
                return callback(null);
            })
    },

    // Lösche eine Komplette Konversation von zwei Benutzern(uId, ouId)
    deleteConversation(uId, ouId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('uId is missing.');
            callback(err);
        }
        if (!ouId) {
            throw new Error('ouId is missing.');
            callback(err);
        }
        this.mappings.remove({
            from: uId,
            to: ouId
        }), err => {
            if (err) {
                return callback(err);
            }
            callback(null, mappings);
        }
    },

    // Lösche Userprofile
    deleteUserProfile(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        this.mappings.remove({
            'uId': uId,
            type: "userprofile"
        }), err => {
            if (err) {
                return callback(err);
            }
            callback(null, mappings);
        }
    },

    // Lösche alle Kleidungsstücke eines Benutzers
    deleteUserClothing(uId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('id is missing.');
            callback(err);
        }
        // find all elements
        this.mappings.remove({
            'uId': uId,
            type: "clothing"
        }), err => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        }
    },
    // Erstelle neues Userprofil
    postUser(uId, callback) {
        if (!uId) {
            throw new Error('uId is missing.');
            callback(err);
        }
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        const mapping = {
            id: uuidv4(),
            uId: uId,
            gender: "?",
            type: "userprofile"
        };
        //write mapping to Database
        this.mappings.insertOne(mapping, err => {
            if (err) {
                return callback(err);
            }
            callback(null);
        });
    },
    // Suche Benutzer dem ein bestimmter Token(token) gehört
    getUserToken(token, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        // find all elements
        this.mappings.findOne({
            token: token,
            type: "token"
        }, (err, mappings) => {
            if (err) {
                return callback(err);
            }
            //send results back to handler
            callback(null, mappings);
        })
    },

    // Suche nach Konversationen zwischen zwei Benutzern (uId,ouId)
    getConversation(uId, ouId, rId, callback) {
        if (!callback) {
            throw new Error('Callback is missing.');
        }
        if (!uId) {
            throw new Error('uId is missing.');
            callback(err);
        }
        if (!ouId) {
            throw new Error('ouId is missing.');
            callback(err);
        }
        if (!rId) {
            throw new Error('rId is missing.');
            callback(err);
        }



        async.waterfall([
            // Finde gesendete Nachrichten
            async.apply(findOwnMessages, this.mappings, uId),
            // Finde empfangene Nachrichten
            async.apply(findOtherMessages, this.mappings, uId, ouId, rId)
        ], function(err, result) {
            if (err == "1") {
                return callback(err);
            } else {
                return callback(null, result);
            }
        });

        function findOwnMessages(mappings, uId, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!uId) {
                callback("1", null);
            }
            if (!mappings) {
                callback("1", null);
            }
            mappings.findOne({
                type: "userprofile",
                uId: uId,
            }, (err, mappings) => {
                if (err) {
                    callback("1", null);
                } else {
                    callback(null, mappings);
                }
            })
        }

        function findOtherMessages(mappings, uId, ouId, rId, ownMessages, callback) {
            if (!callback) {
                throw new Error('Callback is missing.');
            }
            if (!uId) {
                callback("1", null);
            }
            if (!mappings) {
                callback("1", null);
            }
            if (!ouId) {
                callback("1", null);
            }
            if (!ownMessages) {
                callback("1", null);
            }
            if (!rId) {
                callback("1", null);
            }
            mappings.find({
                type: "userprofile",
                "messages.from": ouId,
                "messages.to": uId,
            }).toArray((err, mapping) => {
                if (err) {
                    callback("1", null);
                }
                var allMessages = [];
                // Durchlaufe einzelne Userprofiles
                for (var single_mapping in mapping) {
                    // Durchlaufe einzelne Nachrichten
                    for (var one_message in mapping[single_mapping].messages) {
                        // Suche nach Nachrichten die für den jeweiligen Benutzer(uId) gelten
                        if (mapping[single_mapping].messages[one_message].to == uId && mapping[single_mapping].messages[one_message].rId == rId) {
                            // Trage Nachrichten in die Liste(allMessages) ein
                            allMessages.push(mapping[single_mapping].messages[one_message]);
                        }
                    }

                }
                // durchlaufe die Eigenen Nachrichten
                for (var single_Messages in ownMessages.messages) {
                    // Füge gesendete Nachrichten zur Liste hinzu
                    if (ownMessages.messages[single_Messages].rId == rId)
                        allMessages.push(ownMessages.messages[single_Messages]);
                }
                //send results back to handler
                callback(null, allMessages);
            })
        }
    },

};

function calcClothingDistance(mappings, latitude, longitude, vicinity, callback) {
    var mappings_new = [];
    for (var i = 0; i < mappings.length; i++) {
        // calc distance
        var distance = calcDistance(mappings[i].latitude, mappings[i].longitude, latitude, longitude);
        if (distance <= vicinity) {
            // add distance
            mappings[i].distance = distance;
            mappings_new.push(mappings[i]);
        }
    }
    callback(mappings_new);
    //return mappings_new;
}



module.exports = database;
