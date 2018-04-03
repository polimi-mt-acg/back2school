# Selectively run tests

JUnit categories allow to run selectively groups/classes of tests.

### Categories for tests

<https://github.com/junit-team/junit4/wiki/categories>

Categories for the project are defined as interfaces at:

[/src/main/java/com/github/polimi_mt_acg/back2school/utils/TestCategory.java](../../src/main/java/com/github/polimi_mt_acg/back2school/utils/TestCategory.java)


### Run only one category of tests

In the main [pom.xml](../../pom.xml)

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.19.1</version>
    <configuration>
        <groups>com.github.polimi_mt_acg.back2school.utils.TestCategory$Transient</groups>
    </configuration>
</plugin>
```

To run all tests remove the defined groups.
