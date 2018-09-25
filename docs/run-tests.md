# Run tests

Before run the project's tests, please be sure to have properly [setup the project](project-setup.md) on your machine.


### 1. Run the docker environment

In order to execute the database and also be able to use phpMyAdmin interface:

```bash
docker-compose up
```

### 2. Run tests

Be sure the docker environment is up and correctly running.

On different shell

```bash
mvn test
```

**NOTE:**
Tests do require to have an **empty database** when they're executed. If not so, fake fails of some of them might occur.



