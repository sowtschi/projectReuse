'use strict';

const postUserToken = function(database) {

    return function(req, res) {
        var newTokenKey = "nIWP3D9OZFoEb36ZpukAoCD67Gm50SW0pPEuKwuSnNv5lFcx5EcNBHNhac1h6mZR2vxrWJWIyBTWISJJIsf4brBbmAqVL2GO2bfmWECj1lkGaui4nN6C8vepsTu2oYPFR3u7uREyZ4ztf1GfVCCPfEKvoQzpaeEZKfzyFAaF07bxipxz9KYKx42KExExKCwNfh1EHJOnRnNGvUkuE53kTcuZc8bc3tb5hqgIhJ9GYExeIwRHtutHZ03uP9Hh        VE6lJs8acr8y4IYfwqvMX8RyPe3JseguJb3qA0MmGgAb5CM8APdrAVuezB8QYyHg5PqJIazX83ICyTMJPhjceI9NDPJAU0t6zSaCWIo2oJuaKwDmAUW2fCo4PLNyuxom0vOsK4KGALFIkvHysiV2lXyDBwTK5sd4EIKm1UJPoZKG3jRHBCGKwT7t9BRcWYZaVxVkqi0wa0oWcROv7Hg4EbEtwZDi5o9RI8orwO1EUc4rPOVTI7fj71cKREAz";

        // Wenn korrekter TokenKey gesendet wurde
        if (req.body.key == newTokenKey) {
            database.postUserToken(req.params.id, req.params.token, err => {
                if (err) {
                    console.log("Failed to add clothing!");
                    return res.status(500).send("Token could not be added to the database!");
                } else {
                    console.log("Successfully added Token!");
                    return res.sendStatus(201);
                }
            });
        } else {
            return res.status(401).send("Could not add Token!");
        }

    };
};

module.exports = postUserToken;
