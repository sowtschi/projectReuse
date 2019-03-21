'use strict';

const postUser = function(database) {

    return function(req, res) {

        var newTokenKey = "AgwGYtZUPYKavwAQLRHw0kjxGKI7KQekKhS9MStDXxcVmQZ9ipUmkBoId7AAxC605I9UkpNcO9wQcbSNyIcDVx4uhCFZOErGLFp88tlzhJiCAbCY1FI8sydBHacK6UGfzlMQJ4qx3XreLxX4aBKqTtl5emHzCf0c9FVrpwQnsYEwjmuw2CtKe0FYriX1KxBybUVBRevYnLwif3eLOvGOHxu5bFWwwV7A4rLmHtuqDGWtcfw8xtn48XwS4tTmwbgxMIITv3UrfijcDoAThudrrpDO9B6kj9G07bsP68ydUn9ZkMalgJ6sP3G0ePPcKOVlkhjyDpYeOr8qZTIVZJNr3CEsnWTUQhXYf9lCRUTQp5i1PPZUJHsi7vFm71QdnOiJQWS3l8htAjecmqT2WSOZfKjANc7da6aooKwbllIrrBikUk0z46KAIt87C2CdjjSxoLi8080poQQ4oZimi7jrHuiixHwai0rAjqMyDpD2ippE5t2jAEy3Z4Li";

        if (req.body.key == newTokenKey) {
            database.postUser(req.body.uId, err => {
                if (err) {
                    console.log("Failed to add userprofile!");
                    return res.status(500).send("userprofile could not be added to the database!");
                } else {
                    console.log("Successfully added userprofile!");
                    return res.sendStatus(201);
                }
            });
        } else {
            return res.status(401).send("userprofile could not be added to the database!");
        }
    };
};

module.exports = postUser;
