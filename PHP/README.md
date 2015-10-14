## PHP Scripts ##

Description

These scripts are for the benchmark comparison of an Android application where the proxy is on the server side.

Android should not directly to a mysql DB. These PHP scripts provide a web service for the Android application to interact with a remote data base; in this case a proxy running CryptDB.

The service receives POST requests with JSON data in the packet data. It replies with JSON responses.

Instructions

I use Apache `sudo apt-get install apache2` to launch these scripts.

The database info is stored in the file `DB_CONFIG.php`. It contains the following defines: DB_USER, DB_PASSWORD, DB_DATABASE, DB_SERVER.