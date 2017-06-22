#MCI Service
The Master Client Index (MCI) is a repository of the patients in the health ecosystem.
The MCI stores the patient information and provides an unique Health ID to the patient.
This Health ID is referenced in all the other systems to uniquely related information regarding a patient.

## Setting up the environment

###Prerequisites

* [VirtualBox](https://www.virtualbox.org/)
* [Vagrant](http://docs.vagrantup.com/v2/installation/index.html)
* [Ansible](https://www.ansible.com/)
* You should have installed [Health-Id Service](https://github.com/SharedHealth/HealthId-Service).

####Steps to setup environment on a VM and get MCI working with Stub Identity Server and sample locations
##### Checkout the following repositories (under a common parent directory)
* [FreeSHR-Playbooks](https://github.com/SharedHealth/FreeSHR-Playbooks)
* [MCI-Service](https://github.com/SharedHealth/MCI-Service)
* [Identity-Server](https://github.com/SharedHealth/Identity-Server)

##### Setup ansible group_vars
* replace FreeSHR-Playbooks/group_vars/all with FreeSHR-Playbooks/group_vars/all_example
* create a dummy ansible vault pass file in your user home folder.
```
touch ~/.vaultpass.txt
```

##### Build Identity-Server
* ./gradlew clean dist
* cp build/distributions/identity-server-0.1-1.noarch.rpm /tmp/


##### Build MCI-Service
* ./gradlew clean dist
* cp mci-api/build/distributions/mci-2.7-1.noarch.rpm /tmp/

##### Cassandra Setup
* Login to cassandra box 192.168.33.19 and run command below 
* `cqlsh 192.168.33.19 -ucassandra -ppassword`
Cqlsh terminal run commands below
```
- CREATE KEYSPACE IF NOT EXISTS mci WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': '1'};`
- CREATE USER IF NOT EXISTS mci WITH PASSWORD 'password' NOSUPERUSER;
- ALTER USER mci WITH PASSWORD 'password' NOSUPERUSER;
- GRANT ALL ON KEYSPACE mci TO mci ;
```

##### Start Provision
* Run `vagrant up` from MCI-Service directory.

Notes:
- The above will provision and deploy, MCI and a Stub Identity Server in 192.168.33.19. Cassandra is installed as a single node cluster.
- If you find cassandra not running, ssh to the vagrant box and start cassandra (service cassandra start)
- MCI will run in port 8081
- Stub Identity Server will run in port 8084

### load some location data in MCI
Login to cassandra box 192.168.33.19 and run command below
* cqlsh 192.168.33.19 -u cassandra -p password
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

### Create a patient
>Before this step you should have installed HealthId-Service as mentioned in prerequisite. You need to generated few HIDs from HealthId Service.

Now you can create a patient. Before you can interact with MCI Service, you need to sign-in with the IdP and get an access token. You need to post as a correct user.
Example steps:
* Login to IdP and get a token:
  >curl http://192.168.33.19:8084/signin -H "X-Auth-Token:local-user-auth-token" -H "client_id:18700" --form "email=local-user@test.com" --form "password=password"
  

(The above should return you an access_token)

* With the above token, now you can POST to http://192.168.33.19:8081/api/v1/patients a JSON content to create a patient, with the following headers
  * X-Auth-Token:{the token you received in the previous step}
  * client_id:18700 { this is client id for the user who signed in}
  * From: local-user@test.com
  * Content-Type:application/json

Sample json to create a patient:
```
{
    "given_name": "Pavan",
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

The above should return you a HTTP 201 response with something like the below content with the HID created for the patient.
```
{
    "http_status": 201,
    "id": "98000173958" //this is a sample HID
}
```

* To view the patient record you just created do a GET to `http://192.168.33.19:8081/api/v1/patients/98000173958` with the following headers
  * X-Auth-Token:{the token you received in the previous step}
  * client_id:18700 { this is client id for the user who signed in}
  * From: local-user@test.com
* You can also search by 'nid' parameter. `GET http://192.168.33.19:8081/api/v1/patients?nid=1666321725072

The sub IdP doesn't expire the token unless the Identity-Service is restarted. So you can keep using the "access_token". In reality, the access_token is short-lived and also can be invalidated.


### Troubleshooting
* you might have to install sshpass. Please refer here [here](http://www.nextstep4it.com/sshpass-command-non-interactive-ssh/)
