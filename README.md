# You will remember me when computing metrics for Java dataset

Counting the number of Maven modules is a must-do if you are working on Java
research.
This metric helps to assess the complexity of the projects selected for study.

Googling "how to count maven module maven project" resulted in the following 
StackOverflow post:

#### [How to list active sub-modules in a Maven project?](https://stackoverflow.com/questions/3662291/how-to-list-active-sub-modules-in-a-maven-project)

However, none of the solutions provided in the post worked for me well.

Let's try current solutions on [handlebars.java v4.2.1](https://github.com/jknack/handlebars.java/tree/2afc50fd5dcd32af28f8305b59689b3fec4a3b07)

### [Solution 1](https://stackoverflow.com/a/51824145/11751642)

```shell
mvn -Dexec.executable='echo' -Dexec.args='${project.artifactId}' exec:exec -q
```

> TLDR: 1) maven based solutions are too slow 2) build must pass

Output:

```txt
handlebars.java
handlebars
handlebars-helpers
handlebars-springmvc
handlebars-jackson2
handlebars-markdown
handlebars-humanize
handlebars-proto
handlebars-guava-cache
handlebars-maven-plugin
handlebars-maven-plugin-tests
```

Time:
```text
real 4.30
user 17.33
sys 1.06
```

### [Solution 2](https://stackoverflow.com/a/41766170/11751642)

```shell
find -name pom.xml | grep -v target | sort
```

1. Great to count number of modules, but won't give GAV coordinates.
2. Is prone to error since some `pom.xml` may not be part of reactor. Eg, [spoon](https://github.com/INRIA/spoon) has many modules
  but they are not submodules.

Others did not work for me :) Please correct me if I am wrong.

## Our solution

```shell
./gradlew build
java -jar build/libs/maven-module-graph-1.0-SNAPSHOT.jar \
  --project-root <path/to/maven/project/root> \
  --json <path/to/output.json> \
  --plain-text <path/to/output.txt>
```

> TLDR: 1) extremely fast, 2) provides GAV coordinates, 3) provides a graph in JSON format

> I wanted to learn Gradle so I built using Gradle.

Output:

```txt
com.github.jknack:handlebars.java:4.2.1
  com.github.jknack:handlebars-maven-plugin-tests:4.2.1
  com.github.jknack:handlebars-maven-plugin:4.2.1
  com.github.jknack:handlebars-guava-cache:4.2.1
  com.github.jknack:handlebars-proto:4.2.1
  com.github.jknack:handlebars-humanize:4.2.1
  com.github.jknack:handlebars-markdown:4.2.1
  com.github.jknack:handlebars-jackson2:4.2.1
  com.github.jknack:handlebars-springmvc:4.2.1
  com.github.jknack:handlebars-helpers:4.2.1
  com.github.jknack:handlebars:4.2.1
```

Time:
```text
real 0.31
user 0.52
sys 0.07
```
