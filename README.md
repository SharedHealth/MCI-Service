Dependencies
------------
* Cassandra

To package the application
---------------------------

cd into the project directory

./gradlew dist

To run the application locally
------------------------------

./gradlew runMCI

Properties
----------

* Since the application will be run using an embedded tomcat server, the configuration is read from environment variables.
* Add or remove properties to the local.properties file
* Grable will set environment variables using the properties specified in this file when running tests
* If you install the rpm that is genereated gradlew dist in a redhat machine, you will be able to edit the configuration in the
  /etc/default/mci file.
