# Project setup

Before the project setup, please be sure to have [setup the environment](environment-setup.md) on your machine.


### 1. Clone the repo

```bash
git clone git@github.com:polimi-mt-acg/back2school.git

cd back2school
```


### 2. Configurations in docker-compose.yml file

Adjust configuration parameters into the `docker-compose.yml` file as you prefer. (e.g. `MYSQL_ROOT_PASSWORD=...`)

The already preset values can also be fine.


### 3. Download and run Docker images

The `docker-compose.yaml` file specify two services

- `mariadb` (the database)
- `phpmyadmin` (the notorious graphical management interface)

Run both of them by

```bash
docker-compose up
```

in the repository root.

On the first run you will see Docker downloading the images corresponding to the services. Future runs will reuse the already downloaded images.


### 4. Create the database

With the Docker environment running, proceed to create the database.

Connect to phpMyAdmin by visiting your local docker installation address on port `8001`.

- For Mac and Windows users: `http://<docker-virtual-machine-address>:8001`
- For Linux users: <http://localhost:8001>

phpMyAdmin login credentials:

- user: `root`
- password: *the one specified as `MYSQL_ROOT_PASSWORD` in the `docker-compose.yaml`*. Default: `rootpwd`.

Create a new database and give it any name. Example: `back2school`.

Suggested `Collation`'s value: *utf8_general_ci*.

At this step it's required to create just an empty database. The schema will be automatically managed by Hibernate.

### 3. hibernate.crg.xml files

To setup the connection's parameters for Hibernate, make a copy of the files

- `src/main/resources/hibernate.cfg.xml.tempalte`
- `src/test/resources/hibernate.cfg.xml.tempalte`

respectively as

- `src/main/resources/hibernate.cfg.xml`
- `src/test/resources/hibernate.cfg.xml`

Apply the following edits to **both** of the **copies** created.

At the lines
```xml
<property name="hibernate.connection.url">jdbc:dbms://uri:port/database</property>
<property name="hibernate.connection.username">username</property>
<property name="hibernate.connection.password">password</property>
```
change the values of `dbms`, `uri`, `port`, `database`, `username` and `password` 
according to which are the configuration of your machine.

- The `dmbs` value is `mysql`. (Since mriadb is used as DBMS)

- The `uri` parameter value depends on the machine and its docker installation. 
    
    - For Mac and Windows users: `<docker-virtual-machine-address>`
    - For Linux users: `localhost` or `127.0.0.1`
    
- The `database` parameter corresponds to the database name you specified at the step before. Default: `back2school`.

- The `port` parameter is the same you set in `docker-compose.yaml`. Default: `3306`.

- The `connection.username` and `connection.password` must correspond to a valid user for the database connection. The root user is fine (Default: `root:rootpwd`) but you can create any other user, with customized privileges, from phpMyAdmin and use its username and password for the connection.

Example on a Linux machine:
```xml
<property name="hibernate.connection.url">jdbc:mysql://127.0.0.1:3306/back2school</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">rootpwd</property>
```

---

Next: [Run application](run-application.md)
