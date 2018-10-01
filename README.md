# back2school
*A JEE web application backend of REST APIs for a primary school backend.*


## Project assignment

The goal of this service is to create a backend for managing primary school bureaucracy. The service should be exposed as a REST API.
The back end should support the day-to-day operations of three kinds of users: parents, teachers, and administrators. 

A fourth kind of user exists in the system, that is students. However, students do not have access to the API.

**Parents** should be able to:

- see/modify their personal data
- see/modify the personal data of their registered children
- see the grades obtained by their children
- see/modify appointments that they have with their children's teachers (calendar-like support for requesting appointments)
- see the monthly payments that have been made to the school in the past
- see/pay (fake payment) upcoming scheduled payments (monthly, material, trips)
- see general/personal notifications coming from the school

**Teachers** should be able to:

- see/modify their personal data
- see the classrooms in which they teach, with information regarding the argument that they teach in that class, the students that make up the class, and the complete lesson timetable for that class
- provide grades for the students in their class
- see/modify the appointments that they have with parents

**Administrators** should be able to:

- see the students that are enrolled in each class
- create new students in the system and enroll them in classes
- create new administrator/parent/teacher accounts
- issue new payment requests to parents
- send general notifications to all the parents/teachers in the school, to the parents/teachers of a specific class, or to a single specific parent/teacher.

Use appropriate authentication to ensure that specific users can only interact with specific
resources.

Evaluated on: design of the API (resources/URIs/representations) + hypermedia


## Our solution

The project has been implemented as [JEE] [REST] web application.

The application consist of a backend that exposes a set of [REST] API implemented with [JAX-RS] technology.
To persist the data we used Hibernate ORM, a JPA implementation that abstract the data manipulation and ease the database interaction.

Standards and technologies employed:

- [Java 1.8] programming language
- [Maven] project management and comprehension tool
- [JEE] standard with: [Wildfly] application server ([JBoss] EAP: Enterprise Application Platform)
- [JAX-RS] standard with: [Jersey] abstraction for RESTful Web Services in Java
- [JPA] standard with: [Hibernate] ORM to handle data persistence and database interaction
- [SQL] underling data query language, [MariaDB] database engine
- [JSON] standard for structured data transfer
- [Docker] paravirtualization technology for containerization of services and dev environment setup speed up
- [phpMyAdmin] interface for database data visualization
- [TDD] test driven development approach with [Arquillian]-[JUnit] framework for automated testing
- [Postman] popular HTTP Request composer for by hand APIs tests 


## Project documentation

Checkout the [documentation](docs) to initialize and run this project.

## APIs documentation

Checkout the [APIs documentation] (made thanks to [Postman]) to discover all the implemented endpoints' functionality.

## Project presentation

[back2school project presentation - Arcari, Cilloni, Gregori]


## Project context

This project has been developed for the [Middleware Technologies for Distributed Systems course]
(A.Y. 2017/2018) at [Politecnico di Milano]. Look at the [polimi-mt-acg] page for other projects. 


[JEE]: https://wikipedia.org/wiki/Java_Platform,_Enterprise_Edition
[REST]: https://wikipedia.org/wiki/Representational_state_transfer

[Java 1.8]: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[Maven]: https://maven.apache.org/
[Wildfly]: http://www.wildfly.org/
[JBoss]: http://www.jboss.org/
[JAX-RS]: https://wikipedia.org/wiki/Java_API_for_RESTful_Web_Services
[Jersey]: https://jersey.github.io/
[JPA]: https://wikipedia.org/wiki/Java_Persistence_API
[Hibernate]: http://hibernate.org/
[SQL]: https://wikipedia.org/wiki/SQL
[MariaDB]: https://mariadb.org/
[JSON]: https://www.json.org/
[Docker]: https://www.docker.com/
[phpMyAdmin]: https://www.phpmyadmin.net/
[TDD]: https://wikipedia.org/wiki/Test-driven_development
[Arquillian]: http://arquillian.org/
[JUnit]: https://junit.org
[Postman]: https://www.getpostman.com/

[APIs documentation]: https://documenter.getpostman.com/view/4476607/RWaPv6um

[back2school project presentation - Arcari, Cilloni, Gregori]: https://docs.google.com/presentation/d/19D_GSEKrngPAhNXyjk0vRb9G2AsmLWfxzo1ExqEa_uI/edit?usp=sharing

[Middleware Technologies for Distributed Systems course]: https://www4.ceda.polimi.it/manifesti/manifesti/controller/ManifestoPublic.do?EVN_DETTAGLIO_RIGA_MANIFESTO=evento&aa=2017&k_cf=225&k_corso_la=481&k_indir=T2A&codDescr=090931&lang=EN&semestre=1&idGruppo=3589&idRiga=216904
[Politecnico di Milano]: https://www.polimi.it
[polimi-mt-acg]: https://github.com/polimi-mt-acg
