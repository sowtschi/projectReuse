'use strict';
const uuidv4 = require('uuid/v4');
var request = require('request');


const sendPushNotification = function(token, cId, payload, fits, from, firebase) {

    switch (from) {

        case "message":
            var message = {
                data: {
                    message: payload["message"],
                    sender: from,
                    ouId: payload["from"]
                },
                notification: {
                    title: "Check your Chat!",
                    body: "Someone wrote you a message."
                }
            }
            break;

        case "success":
            var message = {
                data: {
                    rId: payload["id"],
                    message: payload["message"],
                    sender: from,
                    ouId: payload["from"]
                },
                notification: {
                    title: "Check your requests!",
                    body: "Someone declared your transaction as successfully."
                }
            }
            break;

        case "confirmed":
            var message = {
                data: {
                    sender: from,
                },
                notification: {
                    title: "Check your requests!",
                    body: "Someone confirmed successfully transaction."
                }
            }
            break;

        case "waiting":
            var message = {
                data: {
                    sender: from
                },
                notification: {
                    title: "Check your requests!",
                    body: "Someone declared your transaction as successfully."
                }
            }
            break;

        case "accepted":
            var message = {
                data: {
                    uId: cId,
                    sender: from
                },
                notification: {
                    title: "Check your requests!",
                    body: "Someone accepted your request."
                }
            }
            break;

        case "missing":
            var message = {
                data: {
                    cId: cId,
                    sender: from
                },
                notification: {
                    title: "New clothing for you!",
                    body: "Someone entered clothing you are missing."
                }
            }
            break;

        case "postRequest":
            var message = {
                data: {
                    sender: from
                },
                notification: {
                    title: "Check your requests!",
                    body: "Someone is interested in your clothing."
                }
            }
            break;
    }

    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    firebase.messaging().sendToDevice(token, message, options).then(function(response) {
        console.log("Successfully", response.results);
    }).catch(function(error) {
        console.log("Error", error);
    });


}



module.exports = sendPushNotification;
