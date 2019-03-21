var data = [];

//Kleidungsarten
var clothingOptions = {topic:"Art", options: []};

var kategorieObj0 = {topic:"Kopfbedeckung", options: []};
var kategorieObj1 = {topic:"Oberbekleidung", options: []};
var kategorieObj2 = {topic:"Unterbekleidung", options: []};
var kategorieObj3 = {topic:"Fußbekleidung", options: []};
var kategorieObj4 = {topic:"Accessoires", options: []};

kategorieObj0.options.push("Wollmütze","Hut","Mütze","Beanie","Cap");
kategorieObj1.options.push("Pullover","Hoodie","Sweatshirt","T-Shirt","Shirt","Jacke",
                           "Hemd","Bluse","Wintermantel","Mantel","Weste","Strickjacke","Anzug",
                           "Blazer","Kleid","Bademantel","Regenjacke","Sonstiges");
kategorieObj2.options.push("Jeans","Hose","Rock","Unterwäsche","Shorts","Leggings",
                           "Jogginghose","Badehose","Sonstiges");
kategorieObj3.options.push("Stiefel","Sneaker","Sportschuhe","Hausschuhe",
                           "Sandalen","Schuhe","High-Heels","Ballerinas",
                           "Segelschuhe","Outdoorschuhe","Pumps","Sonstiges");
kategorieObj4.options.push("Schmuck","Sonnenbrille","Brille","Uhr","Gürtel",
                           "Handtasche","Tasche","Koffer","Rucksack","Sonstiges");


clothingOptions.options.push(kategorieObj0, kategorieObj1, 
                             kategorieObj2, kategorieObj3,
                             kategorieObj4);

//Geschlecht
var genderOptions = {topic:"Gender", options: []}

genderOptions.options.push("Männlich Erwachsen","Weiblich Erwachsen","Unisex Erwachsen","Männlich Kind","Weiblich Kind","Unisex Kind");

//Kleidungsgroessen
var sizeOptions = {topic:"Size", options: []};


var sizeObj0 = {topic:"Kopfbedeckung Größe", options: []};
var sizeObj1 = {topic:"Oberbekleidung Größe", options: []};
var sizeObj2 = {topic:"Unterbekleidung Größe", options: []}; 
var sizeObj3 = {topic:"Fußbekleidung Größe (Herren)", options: []};
var sizeObj4 = {topic:"Fußbekleidung Größe (Frauen)", options: []};

sizeObj0.options.push("XS","S","M","X","XL","XXL")
sizeObj1.options.push("XS","S","M","X","XL","XXL");
sizeObj2.options.push("40","42","44","46","48","50","52","54","56",
                      "58","60","62","64","66","68","70","72","74");
sizeObj3.options.push("38.5","39","40","40.5","41","42","42.5","43",
                      "44","44.5","45","45.5","46","47","47.5","48",
                      "48.5","49.5","50.5","51.5","52.5");
sizeObj4.options.push("35.5","36","36.5","37.5","38","38.5","39","40",
                      "40.5","41","42","42.5","43","44","44.5");

sizeOptions.options.push(sizeObj0,sizeObj1,sizeObj2,sizeObj3,sizeObj4);

//Kleidungsstil
var styleOptions = {topic:"Style", options: []};

styleOptions.options.push("Elegant/Schick", "Funktionswäsche/Arbeitskleidung", "Casual", 
                          "Sportlich/Freizeit","Sonstiges");

//Kleidungsfarbe
var colorOptions = {topic:"Color", options: []};

colorOptions.options.push("Rot", "Gelb", "Grün", "Cyan", "Blau", "Magenta", "Rot",
                          "Schwarz", "Weiß", "Hellgrau", "Dunkelgrau", "Hellbraun", 
                          "Dunkelbraun","Sonstiges");

//Kleidungsstoff
var fabricOptions = {topic:"Fabric", options: []};

fabricOptions.options.push("Baumwolle","Wolle","Viskose","Modal","Polyester","Leinen","Polyacryl",
                           "Polyamid","Seide","Kaschmir","Denim","Leder","Synthetik");

//Kleidungsmarke
var brandOptions = {topic:"Brand", options: []};

brandOptions.options.push("Sonstiges","Adidas","Nike","Mango","Converse","Jack&Jones","Vans","Esprit",
                          "H&M","Diesel","Calvin Klein","Puma","Levis","Lacoste","superdry","Reebok","Marc O'Polo");

//Export der Daten
data.push(clothingOptions,genderOptions,sizeOptions,styleOptions,
          colorOptions,fabricOptions,brandOptions);
exports.data = data;