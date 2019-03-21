'use strict';

/** Die Funktion require Authentication bestimmt prüft, ob ein Token, der vom Benutzer während eines Aufrufs mitgesendet wird,
 * innerhalb des Systems bekannt ist und kann so feststellen, ob eine Anfrage von einem Benutzer der Applkationb oder
 * von einem Fremdsystem stammt. Nach jedem Login Vorgang über die Applikation wird hierfür ein neuer Token generiert und
 * in der Datenbank eingetragen.
 *
 * @param databse Verbindungspunkt zur Datenbank
 * @param token Der vom User gesendete Token
 * @param next Callback im Falle, dass ein Benutzer einen gültigen Token gesendet hat
 *
 */



const requireAuthentication = function(database, token, next, callback) {
    if (!database) {
        throw new Error('Database is missing.');
    } {
        // call database

        database.getUserToken(token, (err, mappings) => {
            if (err) {
                return callback(null, false, null);
            } else {
                // Wenn der Token in der Datenbank eingetragen ist
                if (mappings != null) {
                    // Benutzer konnte in der Datenbank gefunden werden
                    return callback(null, true, next);
                } else {
                    // Benutzer konnte nicht in der Datenbank gefunden werden
                    return callback(null, false, null);
                }
            }
        })

    };
};

module.exports = requireAuthentication;
