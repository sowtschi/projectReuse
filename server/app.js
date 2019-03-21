'use strict';

//const https = require('https');
const http = require('http');
const fs = require('fs');
const getApp = require('./lib/getApp');
const database = require('./lib/database');

const serviceAccount = require("./???.json");
let firebase = require("firebase-admin");
var API_KEY = "?X?";
const app = getApp(database, firebase);

const mongoUrl = process.env.MONGO_URL || 'mongodb://user:pass@domain:57245/klamotten-verteiler';

firebase.initializeApp({
    credential: firebase.credential.cert(serviceAccount),
    databaseURL: "https://domain.com/",
    apiKey: API_KEY,
    projectId: "eis1718-ef0c1"
});


// Load HTTPS configuration
const options = {
    key: fs.readFileSync(__dirname + '/keys/key.pem', 'utf8'),
    cert: fs.readFileSync(__dirname + '/keys/server.crt', 'utf8')
};

const server = http.createServer(app);
// define server
//const server = https.createServer(options, app);

// server port
const port = process.env.PORT || 50262;



// initialize MongoDB
database.initialize(mongoUrl, err => {
    if (err) {
        console.log('Failed to connect to database.', {
            err
        });
        process.exit(1);
    }
});

const requestHandler = (request, response) => {
    console.log(request.url)
    response.end('Hello Node.js Server!')
}

// start server
server.listen(port, () => {
    console.log('Server is started.', {
        port
    });
});
