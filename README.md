# Setting up the environment

Requisites
------------
* [VirtualBox](https://www.virtualbox.org/)
* [Vagrant](http://docs.vagrantup.com/v2/installation/index.html)


To package the application
---------------------------
cd into the project directory and run the following command
```
./gradlew clean dist
```

To run the application locally
------------------------------
```
./gradlew runMCI
```

Properties
----------
* Since the application will be run using an embedded tomcat server, the configuration is read from environment variables.
* Add or remove properties to the local.properties file
* Grable will set environment variables using the properties specified in this file when running tests
* If you install the rpm that is genereated gradlew dist in a redhat machine, you will be able to edit the configuration in the
  /etc/default/mci file.


Steps to setup environment on a VM and get MCI working with Stub Identity Server and sample locations
------------------------------------------------------------------------------------------------------------------------
### git checkout the following repositories (under a common parent directory)
* [FreeSHR-Playbooks](https://github.com/SharedHealth/FreeSHR-Playbooks)
* [MCI-Service](https://github.com/SharedHealth/MCI-Service)
* [Identity-Server](https://github.com/SharedHealth/Identity-Server)

### Setup ansible group_vars
* replace FreeSHR-Playbooks/group_vars/all with FreeSHR-Playbooks/group_vars/all_example
* create a dummy ansible vault pass file in your user home folder.
```
touch ~/.vaultpass.txt
```



### cd to Identity-Server
* ./gradlew clean dist
* cp build/distributions/identity-server-0.1-1.noarch.rpm /tmp/


### cd to MCI-Service
* ./gradlew clean dist
* cp mci-api/build/distributions/mci-0.1-1.noarch.rpm /tmp/
* vagrant up | vagrant provision

Notes:
- The above will provision and deploy, MCI and a Stub Identity Server in 192.168.33.19. Cassandra is installed as a single node cluster.
- If you find cassandra not running, ssh to the vagrant box and start cassandra (service cassandra start)
- MCI will run in port 8081
- Stub Identity Server will run in port 8080

### load some location data in MCI
* cqlsh 192.168.33.19 -umci -pexample_mci_password
* describe keyspaces;
* use mci;
* copy and run the following scripts for some sample location data

```
INSERT INTO locations ("code", "name", "active","parent") VALUES ('30','Dhaka','1','00') IF NOT EXISTS;
INSERT INTO locations ("code", "name", "active","parent") VALUES ('26','Dhaka','1','30') IF NOT EXISTS;
INSERT INTO locations ("code", "name", "active","parent") VALUES ('02','Adabor','1','3026') IF NOT EXISTS;
INSERT INTO locations ("code", "name", "active","parent") VALUES ('20','Dhaka Dakshin City Corp.','1','302602') IF NOT EXISTS;
INSERT INTO locations ("code", "name", "active","parent") VALUES ('25','Dhaka Uttar City Corp.','1','302602') IF NOT EXISTS;
INSERT INTO locations ("code", "name", "active","parent") VALUES ('30','Urban Ward No-30 (43)','1','30260225') IF NOT EXISTS;
INSERT INTO locations ("code", "name", "active","parent") VALUES ('33','Urban Ward No-33 (part) (46)','1','30260225') IF NOT EXISTS;
```

NOTES: Before you can post to MCI Service, you need to sign-in with the IdP and get an access token.

Example steps:
* Login to IdP and get a token:
  * curl http://192.168.33.19:8080/signin -H "X-Auth-Token:41eeda45e711cc6b3e660e4abb2cb863f93ae90815f0edf40a134dffedf6d885" -H "client_id:18548" --form "email=angshus@thoughtworks.com" --form "password=activation"

(The above should return you an access_token)

* With the above token, now you can POST to http://192.168.33.19:8081/api/v1/patients a JSON content to create a patient, with the following headers
  * X-Auth-Token:{the token you received in the previous step}
  * client_id:6 { this is client id for the user who signed in}
  * From: angshus@thoughtworks.com
  * Content-Type:application/json

```
Sample json to create a patient:
{
    "given_name": "Salman",
    "sur_name": "Das",
    "nid": "1666321725072",
    "date_of_birth": "1992-07-14",
    "gender": "M",
    "present_address": {
        "address_line": "Street 1, Baridhara village",
        "division_id": "30",
        "district_id": "26",
        "upazila_id": "02"
    },
    "confidential": "No"
}
```

The above should return you a HTTP 201 response with something like the below content with the UHID created for the patient.
```
{
    "http_status": 201,
    "id": "98417155622"
}
```

* To view the patient record you just created do a GET to http://192.168.33.19:8081/api/v1/patients/98417155622 with the following headers
  * X-Auth-Token:{the token you received in the previous step}
  * client_id:6 { this is client id for the user who signed in}
  * From: angshus@thoughtworks.com
  * Content-Type:application/json
* You can also search by 'nid' parameter. GET http://192.168.33.19:8081/api/v1/patients?nid=1666321725072

The sub IdP doesn't expire the token unless the Identity-Service is restarted. So you can keep using the "access_token". In reality, the access_token is short-lived and also can be invalidated.


### Troubleshooting
* you might have to install sshpass. Please refer here [here](http://www.nextstep4it.com/sshpass-command-non-interactive-ssh/)
