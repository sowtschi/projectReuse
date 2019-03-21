'use strict';

/** Die Funktion calcOutfit bietet die Möglichkeit aus einzelnen Kleidungsstücken sinnvolle Outfits zusammenzustellen und darüber hinaus
* eine Möglichkeit Kleidungsstücke daraufhin zu prüfen, für welchen Nutzungskontext sie zu gebrauchen sind.
* Fest definierte Modelle, die sich jeweils auf die Bekleidung von bestimmten Bereichen des Körpers beziehen,
* enthalten hierbei Restriktionen, die sich auf die verwendete Art von Stoff beziehen.
* Die einzelnen Arten von Stoffen besitzen passend dazu jeweils eine Datenstruktur, der verschiedene Eigenschaften des Stoffes festhält.
*
* Darüber hinaus halten die Modelle fest, für welchen Nutzungskontext ein bestimmtes Kleidungsstück sinnvoll ist.
*
* Anhand der festegehaltenen Eigenschaften und den Einschränkungen innerhalb des Modells können durch Vergleichsoperationen sinnvolle Outfits zusammengestellt
* werden.
*
* @param {String} context - Definiert den gewünschen Nutzungskontext eines Outfits.
* @param {Object} clothing - Enhält die Kleidungsstücke, die bei der Berechnung berücksichtigt werden sollen.
* @param {Boolean} single - Unterscheidet zwischen Outfitsuche und Zuordnung von einem Kleidungsstück zu Nutzungskontexten
* @param {String} gender - Bestimmt das Geschlecht für welches Kleidungsstücke gesucht werden sollen
* @param {String} hSize - Bestimmt Größe für head-Schicht
* @param {String} tSize - Bestimmt Größe für Oberkörper-Schicht
* @param {String} bSize - Bestimmt Größe für Bottom-Schicht
* @param {String} sSize - Bestimmt Größe für "Shoes"
*
*/



const calcOutfit = function(context, clothing, single, gender, hSize, tSize, bSize, sSize, callback) {

/* Models enthalten Informationen darüber, welche Merkmale ein Kleidungsstück erfüllen muss, um für den angegebenen Kontext gültig zu sein.
*  Dabei werden jeweils ein "high" und ein "low"-Wert für verschiedene Eigenschaften des Stoffes, der verwendet wird, eingetragen. Stoffe, die innerhalb des Bereichs der Definition liegen, werden
*  bei der späteren Berechnung von Outfits als gültige Stoffarten registriert. Ebenfalls die Art der Kleidung, die gültig sein soll, wird festgehalten, da für einen bestimmten Kontext nur bestimmte Arten von Kleidungsstücken zulässig sind.
*  Einige Schichten besitzen zudem eine Alternative, deren Restriktionen etwas weniger streng sind und zum Einsatz kommen, falls kein passendes Kleidungsstück gefunden werden konnte.
*/

var wintermodel = {
        wintermodel_head: {
          respiratory_activity_low: 0,
          respiratory_activity_high: 0,
          warmth_low: 0,
          warmth_high: 0,
          moisture_pickup_low: 0,
          moisture_pickup_high: 0,
          art: ["Wollmütze","Mütze","Beanie"],
          model: "wintermodel_head"
        },
        wintermodel_layer1: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["T-Shirt", "Shirt","Hemd","Bluse"],
            model: "wintermodel_1layer"
        },
        wintermodel_layer1_alternative: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["T-Shirt", "Shirt","Hemd","Bluse"],
            model: "wintermodel_1layer_alternative"
        },
        wintermodel_layer2: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 4,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Pullover","Hoodie","Sweatshirt","Weste","Strickjacke","Blazer"],
            model: "wintermodel_2layer"
        },
        wintermodel_layer2_alternative: {
            respiratory_activity_low: 7,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Pullover","Hoodie","Sweatshirt","Weste","Strickjacke","Blazer"],
            model: "wintermodel_1layer_alternative"
        },
        wintermodel_layer3: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 8,
            warmth_high: 10,
            moisture_pickup_low: 0,
            moisture_pickup_high: 5,
            art: ["Wintermantel","Mantel","Jacke"],
            model: "wintermodel_3layer"
        },
        wintermodel_layer3_alternative: {
            respiratory_activity_low: 4,
            respiratory_activity_high: 10,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 5,
            moisture_pickup_high: 10,
            art: ["Wintermantel","Mantel","Jacke"],
            model: "wintermodel_3layer_alternative"
        },
        wintermodel_bottom: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Jeans","Hose","Leggings"],
            model: "wintermodel_bottom"
        },
        wintermodel_shoes: {
            respiratory_activity_low: 0,
            respiratory_activity_high: 0,
            warmth_low: 0,
            warmth_high: 0,
            moisture_pickup_low: 0,
            moisture_pickup_high: 0,
            art: ["Stiefel","Schuhe","Segelschuhe","Outdoorschuhe"],
            model: "wintermodel_shoes"
        }

    }

    /* Die folgende Definition dient als Container für das fertige Outfit. Diese Unterscheiden sich bei den meisten Modellen. Ein Winteroutfit bietet beispielsweise Platz für drei Schichten der Oberkörperbekleidung, bei einem Sommeroutfit ist es lediglich eine Schicht.*/
    var winteroutfit =    {
            model: "wintermodel",
            head: 0,
            layer1: 0,
            layer2: 0,
            layer3: 0,
            bottom: 0,
            shoes: 0,
            layers: ["head", "layer1", "layer2", "layer3", "bottom", "shoes"],
            layerArt: ["head","layer","layer","layer","bottom","shoes"]
            }



    /* Die Datenstruktur characs ist wesentlicher Bestandteil bei der Auswahl von Kleidungsstücken. Sie repräsentiert die verschiedenen Stoffarten und ihre Eigenschaften. */
    var charac = [{
            name: "Baumwolle",
            warmth: 6,
            moisture_pickup: 2,
            respiratory_activity: 10
        },
        {
            name: "Wolle",
            warmth: 9,
            moisture_pickup: 4,
            respiratory_activity: 9
        },
        {
            name: "Viskose",
            warmth: 6,
            moisture_pickup: 3,
            respiratory_activity: 9
        },
        {
            name: "Modal",
            warmth: 6,
            moisture_pickup: 3,
            respiratory_activity: 9
        },
        {
            name: "Polyester",
            warmth: 9,
            moisture_pickup: 9,
            respiratory_activity: 7
        },
        {
            name: "Leinen",
            warmth: 4,
            moisture_pickup: 1,
            respiratory_activity: 10
        },
        {
            name: "Polyacryl",
            warmth: 7,
            moisture_pickup: 8,
            respiratory_activity: 8
        },
        {
            name: "Polyamid",
            warmth: 7,
            moisture_pickup: 8,
            respiratory_activity: 4
        },
        {
            name: "Seide",
            warmth: 3,
            moisture_pickup: 7,
            respiratory_activity: 9
        },
        {
            name: "Kaschmir",
            warmth: 10,
            moisture_pickup: 8,
            respiratory_activity: 8
        },
        {
            name: "Denim",
            warmth: 7,
            moisture_pickup: 4,
            respiratory_activity: 8
        },
        {
            name: "Leder",
            warmth: 8,
            moisture_pickup: 9,
            respiratory_activity: 8
        },
        {
            name: "Synthetik",
            warmth: 6,
            moisture_pickup: 10,
            respiratory_activity: 2
        }

    ];

    var models = [wintermodel];
    var fits;
    var res;
    if (single) {
        // Durchlaufe alle Modelle
        for (var single_model in models) {
            // Durchlaufe alle Schichten der einzelnen Modelle
            for (var single_layer in models[single_model]) {

                // Berechne für das jeweilige Kleidungsstück, ob die jeweilige Schicht gültig ist.
                res = calcLayerClothing(models[single_model][single_layer], clothing, charac, single);
                if (res != null) {
                    fits=res;
                }
            }
        }
      return fits;
    }

    // Wenn nach Outfits gesucht wird und der Anwendungszweck Winter ist
    if (!single && context== "winter") {
      // Berechne passende Kleidungsstücke für einen bestimmten Layer eines Modells
      winteroutfit.head = calcLayerClothing(wintermodel["wintermodel_head"], clothing, charac, false);
      // Wenn kein Kleidungsstück gefunden wurde
      winteroutfit.layer1 = calcLayerClothing(wintermodel["wintermodel_layer1"], clothing, charac, false);
            // Wenn kein Kleidungsstück gefunden wurde
      if (winteroutfit.layer1 == 0) {
          // Suche erneut mit Alternativem Modell für die jeweilige Schicht
          winteroutfit.layer1 = calcLayerClothing(wintermodel["wintermodel_layer1_alternative"], clothing, charac, false);
      }
      winteroutfit.layer2 = calcLayerClothing(wintermodel["wintermodel_layer2"], clothing, charac, false);
      if (winteroutfit.layer2 == 0) {
          winteroutfit.layer2 = calcLayerClothing(wintermodel["wintermodel_layer2_alternative"], clothing, charac, false);

      }
      winteroutfit.layer3 = calcLayerClothing(wintermodel["wintermodel_layer3"], clothing, charac, false);
      if (winteroutfit.layer3 == 0) {
          winteroutfit.layer3 = calcLayerClothing(wintermodel["wintermodel_layer3_alternative"], clothing, charac, false);

      }
      winteroutfit.bottom = calcLayerClothing(wintermodel["wintermodel_bottom"], clothing, charac, false);

      winteroutfit.shoes = calcLayerClothing(wintermodel["wintermodel_shoes"], clothing, charac, false);
      winteroutfit= filterClothing(wintermodel, winteroutfit, gender, hSize, tSize, bSize, sSize);
          // Gib das komplette Outfit zurück

          callback(winteroutfit);
    }


   /** calcLayerClothing berecnet für eine Menge von Kleidungsstücken, ob diese die Bedingungen, die sich aus dem Modell ergeben
   * erfüllen oder für welche Schichten ein Kleidungsstück gültig ist.
   *
   * @param {String} context - Definiert den gewünschen Nutzungskontext eines Outfits.
   * @param {Object} clothing - Enhält die Kleidungsstücke, die bei der Berechnung berücksichtigt werden sollen.
   * @param {Boolean} charac - Enthält Informationen über die verschiedenen Stoffarten
   * @param {Boolean} single - Unterscheidet zwischen Outfitsuche und Zuordnung von einem Kleidungsstück zu Nutzungskontexten
   *
   */

    function calcLayerClothing(model, clothing, charac, single) {

    // Zwischenspeicher für gültige Kleidungsstücke bzw. Modelle die gültig für ein Kleidungsstück sind
    var result = [];

    // Durchlauf der einzelnen Stoffarten
    for (var single_charac in charac) {
      // Überprüfung ob Eigenschaften der jeweiligen Stoffart die Bedingungen des Modells erfüllen
            if ((model.respiratory_activity_low <= charac[single_charac].respiratory_activity && model.respiratory_activity_high >= charac[single_charac].respiratory_activity) || (model.respiratory_activity_high == 0 && model.respiratory_activity_low == 0)) {
                if ((model.warmth_low <= charac[single_charac].warmth && model.warmth_high >= charac[single_charac].warmth) || (model.warmth_high == 0 && model.warmth_low == 0)) {
                    if ((model.moisture_pickup_low <= charac[single_charac].moisture_pickup && model.moisture_pickup_high >= charac[single_charac].moisture_pickup) || (model.moisture_pickup_high == 0 && model.moisture_pickup_low == 0)) {
                        // Durchlaufe Kleidungsarten, die für ein bestimmtes Modell gültig sind
                        for (var art in model.art) {
                            // Sollte das Flag single gesetzt sein liefert die Funktion passende Modells für ein bestimmtes Kleidungsstück
                            // Wenn die Art und der Stoff des Kleigunsstücks für das gewählte Modell gültig sind
                            if (single && clothing.art == model.art[art] && clothing.fabric == charac[single_charac].name) {
                                // Speichert die ID des Kleidungsstücks und die Modelle für das es gültig ist
                                result.push({
                                    id: clothing.id,
                                    model: model.model
                                });
                            }
                        }
                        // Wenn single nicht gesetzt werden Kleidungsstücke ermittelt, die für einen bestimmten Anwendungszweck geeignet sind.
                        if (!single) {
                            // Durchlaufe die verfügbaren Kleidungsstücke
                            for (var single_clothing in clothing) {
                                // Durchlaufe die gültigen Kleidungsarten
                                for (var art in model.art) {
                                    // Wenn die Art und der Stoff des Kleidungsstück passend ist
                                    if (clothing[single_clothing].art == model.art[art] && clothing[single_clothing].fabric == charac[single_charac].name) {
                                        // Füge passende Kleidungsstücke "result" hinzu
                                        result.push(clothing[single_clothing]);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
            // Sende das Ergebniss der jeweiligen Operation
            return result;
        }
    };

    /** filterClothing überprüft die einzelnen Kleidungsstücke eines Outfits, ob diese, den
    * vom Benutzer definierten Anforderungen (im genauen Geschlecht und Größen) entsprechen.
    *
    * @param {String} context - Definiert den gewünschen Nutzungskontext eines Outfits.
    * @param {Object} clothing - Enhält die Kleidungsstücke, die bei der Berechnung berücksichtigt werden sollen.
    * @param {Boolean} charac - Enthält Informationen über die verschiedenen Stoffarten
    * @param {Boolean} single - Unterscheidet zwischen Outfitsuche und Zuordnung von einem Kleidungsstück zu Nutzungskontexten
    *
    */

    function filterClothing(model, outfit, gender, hSize, tSize, bSize, sSize) {

      //Kopiere aktuelles Outfit
      var new_outfit = JSON.parse(JSON.stringify(outfit));;
      var i=0;
      var keys = Object.keys(new_outfit);
      // Lösche bereits vorhandene Kleidungsstücke aus "new_outfit"
      for (var single_layer in new_outfit) {
        // Modelldaten nicht löschen!
        if (i > 0  && i < keys.length - 2) {
          new_outfit[single_layer]= [];
        }
        i++;
      }
      var art;
      // Durchlaufe allgemeine Arten
      for (var single_layerArt in outfit.layerArt) {
        // Durchlaufe spezielle Arten
        for (var single_layer in outfit.layers) {
            // Durchlaufe einzelne Kleidungsstücke
            for (var single_clothing in outfit[outfit.layers[single_layer]]) {
          // Überprüfe ob Kleidungsstück richtige Art & richtige Größe hat und für das richtige Geschlecht iist
          if (outfit.layerArt[single_layerArt] == "head" && (outfit[outfit.layers[single_layer]][single_clothing].size == hSize || hSize==0) && (outfit[outfit.layers[single_layer]][single_clothing].gender == gender || gender==0 ) && outfit.layers[single_layer] == outfit.layers[single_layerArt]) {
              // Wenn ein KLeidungsstück die gewählten Anforderungen erfüllt, füge es zu einer Liste hinzu
              new_outfit[outfit.layers[single_layer]].push(outfit[outfit.layers[single_layer]][single_clothing].id);
          }
          if (outfit.layerArt[single_layerArt] == "layer" && (outfit[outfit.layers[single_layer]][single_clothing].size == tSize || tSize==0)  && (outfit[outfit.layers[single_layer]][single_clothing].gender == gender || gender==0 ) && outfit.layers[single_layer] == outfit.layers[single_layerArt] ) {
              new_outfit[outfit.layers[single_layer]].push(outfit[outfit.layers[single_layer]][single_clothing].id);
          }
          if (outfit.layerArt[single_layerArt] == "bottom" && (outfit[outfit.layers[single_layer]][single_clothing].size == bSize || bSize==0) && (outfit[outfit.layers[single_layer]][single_clothing].gender == gender || gender==0 ) && outfit.layers[single_layer] == outfit.layers[single_layerArt]) {
              new_outfit[outfit.layers[single_layer]].push(outfit[outfit.layers[single_layer]][single_clothing].id);
          }
          if (outfit.layerArt[single_layerArt] == "shoes" && (outfit[outfit.layers[single_layer]][single_clothing].size == sSize || sSize==0) && (outfit[outfit.layers[single_layer]][single_clothing].gender == gender || gender==0 ) && outfit.layers[single_layer] == outfit.layers[single_layerArt]) {
              new_outfit[outfit.layers[single_layer]].push(outfit[outfit.layers[single_layer]][single_clothing].id);;
          }
        }

     }

  }
      // Gebe neues Outfit zurück
     return new_outfit;
  }

    module.exports = calcOutfit;
