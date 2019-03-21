'use strict';

const calcDistance = function(la1, lo1, la2, lo2) {
    // Seiten-Kosinussatz
    // see https://www.kompf.de/gps/distcalc.html

    // Radius of the earth in km
    var R = 6378.388;
    var aLa = (la2 - la1) * Math.PI / 180;
    var aLo = (lo2 - lo1) * Math.PI / 180;
    var a = Math.sin(aLa / 2) * Math.sin(aLa / 2) +
        Math.cos(la1 * Math.PI / 180) * Math.cos(la2 * Math.PI / 180) *
        Math.sin(aLo / 2) *
        Math.sin(aLo / 2);
    // Distance in km
    var km = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var distance = R * km;
    return distance;
};

module.exports = calcDistance;
