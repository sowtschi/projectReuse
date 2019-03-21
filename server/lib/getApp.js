'use strict';

const express = require('express');
const bodyParser = require('body-parser')
const requireAuthentication = require('./requireAuthentication');
const async = require("async");


var routes = require("./routes");
var login = false;

const getApp = function(database, firebase) {
    // check Database
    if (!database) {
        throw new Error('Database is missing!');
    }
    // define express Webframework
    const app = express();

    //include Body Parser for JSON req
    app.use(bodyParser.json({
        extended: true,
        limit: '5mb'
    }));

    app.use(bodyParser.urlencoded({
        extended: true,
        limit: '5mb'
    }));

    var logIn = function(req, res, next) {
        async.waterfall([
            async.apply(callrequireAuthentication, database, req.params.token)
        ], function(err, result) {
            if (err == "1") {
                return res.status(401).end();

            } else {
                login = result;
                next();
            }
        });

        function callrequireAuthentication(database, token, callback) {
            requireAuthentication(database, req.params.token, next, (err, mappings, next) => {
                if (mappings == true) {
                    return callback(null, true);
                } else {
                    callback("1", null);
                }
            })
        }
    }


    // Pfade die durch die Verifizierung(login()) eines Tokens gesichert werden
    app.use('/user/:uId/:token/requests/', logIn);
    //...

    // Routen definition
    app.get('/outfit/:art/:gender/:hSize/:tSize/:bSize/:sSize/:longitude/:latitude/:vicinity', routes.getOutfit(database, "false"));
    app.post('/user/:uId/search', routes.postUserSearch(database));
    app.post('/users/:id/token/:token', routes.postUserToken(database));
    app.post('/user/:uId/messages', routes.postMessage(database, firebase));
    app.get('/user/:uId/messages/:ouId/:rId', routes.getConversation(database));
    app.delete('/user/:uId/messages/:ouId', routes.deleteConversation(database));
    app.get('/user/:uId/clothing/', routes.getUserClothing(database));
    app.get('/user/:id', routes.getUserProfile(database));
    app.put('/user/:uId', routes.putUserProfile(database));
    app.delete('/user/:uId', routes.deleteUserProfile(database));
    app.delete('/user/:uId/clothing', routes.deleteUserClothing(database, firebase));

    app.get('/user/:uId/:token/requests/', routes.getUserRequests(database, firebase));
    app.delete('/user/:uId/requests/:id', routes.deleteUserRequest(database));
    app.get('/clothing/:brand/:style/:color/:art/:size/:latitude/:longitude/:vicinity', routes.getCustomeClothing(database));
    app.put('/user/:uId/:token/requests/:id', routes.putRequest(database, firebase));
    app.post('/user/:uId/rating', routes.postUserRating(database));
    app.get('/user/:uId/rating', routes.getUserRating(database));
    app.put('/user/:uId/rating/:id', routes.putUserRating(database));
    app.get('/user/:uId/outfits', routes.getUserOutfit(database));
    app.get('/user/:uId/outfits/:oId', routes.getUserOutfitClothing(database));
    app.post('/users', routes.postUser(database));

    app.put('/user/:uId/clothing', routes.putClothing(database));
    app.get('/clothing/:cId', routes.getClothing(database));
    app.get('/clothingOptions', routes.getClothingOptions());
    app.put('/clothing/:cId', routes.putClothing(database));
    app.post('/clothing/:cId', routes.postRequest(database, firebase));
    app.delete('/clothing/:cId', routes.deleteClothing(database));

    app.post('/klamotten', routes.postKlamotten(database, firebase));
    app.get('/klamotten/:latitude/:longitude/:vicinity/:uId', routes.getAllClothingLocation(database));


    app.get('/outfit/:art/:latitude/:longitude/:vicinity', routes.getOutfit(database, "true"));
    app.get('/all', routes.getAll(database));

    app.get('/', routes.getIndex());

    return app;
};

module.exports = getApp;
