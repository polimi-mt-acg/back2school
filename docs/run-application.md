# Run the application

Before run the application, please be sure to have properly [setup the project](project-setup.md) on your machine.


### 1. Run the docker environment

In order to execute the database and also be able to use phpMyAdmin interface:

```bash
docker-compose up &
```

### 2. Run the application server & Deploy the application

Compile, tear the application server up and deploy the application:

```bash
mvn install -DskipTests
```

Once the deployment succeed, the application will be ready to accept incoming requests.

**NOTE:** The first time the application is executed, if there's not any *admin* user into the database, a default one **is automatically created**.

Default admin user's credentials:

- email: `admin@email.com`
- password: `admin`

### 3. Tear down
To tear the system down, press `CTRL+C` in your shell and wait for the application server to close. Then stop phpMyAdmin and MariaDB:

```bash
docker-compose down
```

---

Next: [Postman APIs interaction and demo data deployment](postman-API-interaction-and-demo-data-deployment.md)


