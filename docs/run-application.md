# Run the application

Before run the application, please be sure to have properly [setup the project](project-setup.md) on your machine.


### 1. Run the docker environment

In order to execute the database and also be able to use phpMyAdmin interface:

```bash
docker-compose up
```

### 2. Run the application server

On different shell, run the application server:

```bash
<base-path-to-wildfly>/bin/standalone.sh
```

### 3. Deploy the application

Again on one more different shell, deploy the application with Maven:

```bash
mvn wildfly:deploy
```

Once the deployment succeed, the application will be ready to accept incoming requests.

**NOTE:** The first time the application is executed, if there's not any *admin* user into the database, a default one **is automatically created**.

Default admin user's credentials:

- email: `admin@email.com`
- password: `admin`

---

Next: [Postman APIs interaction and demo data deployment](postman-API-interaction-and-demo-data-deployment.md)


