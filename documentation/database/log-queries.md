# Log the queries from mariadb container

In order to log the executed queries, with the docker environment running:

```bash
docker exec -it <mariadb-container-name> tail -f /var/lib/mysql/queries.log

docker exec -it back2school_mariadb_1 tail -f /var/lib/mysql/queries.log
```
