# Quarkus jedis-test Project

## Versions

graalvm-ce-java11-22.0.0.2

## Intro

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/jedis-test-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)


## Running this example 

1. Build
```shell script
./mvnw package -Pnative
```

2. Start Docker
```shell script
docker pull redis/redis-stack:latest
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest

docker container ls
CONTAINER ID   IMAGE                      COMMAND            CREATED          STATUS          PORTS                                            NAMES
e2bc9a81477b   redis/redis-stack:latest   "/entrypoint.sh"   25 seconds ago   Up 25 seconds   0.0.0.0:6379->6379/tcp, 0.0.0.0:8001->8001/tcp   redis-stack

```

4. Run the native app
- By default the app will bootstrap 600+ student json records (-Dredis.bulk.load.data=true) 
```shell script
./target/jedis-test-1.0.0-SNAPSHOT-runner

__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2022-04-04 20:21:57,237 INFO  [org.acm.red.con.JedisConfig] (main) Checking to see if Students Index exists...
2022-04-04 20:21:57,251 INFO  [org.acm.red.con.JedisConfig] (main) No student index exists, will attempt to create
2022-04-04 20:21:57,251 INFO  [org.acm.red.con.JedisConfig] (main) Creating idx:student Index in Redis
2022-04-04 20:21:57,254 INFO  [org.acm.red.con.JedisConfig] (main) loading data from json file...
2022-04-04 20:21:57,260 INFO  [org.acm.red.con.JedisConfig] (main) Serialised 602 json objects
2022-04-04 20:21:57,921 INFO  [org.acm.red.con.JedisConfig] (main) Successfully inserted '602' into Redis
2022-04-04 20:21:57,922 INFO  [org.acm.red.con.JedisConfig] (main) Redis DBSize : '602' values
2022-04-04 20:21:57,925 INFO  [io.quarkus] (main) jedis-test 1.0.0-SNAPSHOT native (powered by Quarkus 2.7.5.Final) started in 0.713s. Listening on: http://0.0.0.0:8080
2022-04-04 20:21:57,925 INFO  [io.quarkus] (main) Profile prod activated. 
2022-04-04 20:21:57,925 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, vertx]


```

5. Example Requests 
```shell script
// Return Student with hardcoded id ('student:1')
curl GET 'http://localhost:8080/student/example/maya'
{
    "title": "Mrs",
    "firstName": "Maya",
    "lastName": "Jayavant",
    "gender": "Female",
    "birth": "07/01/1985",
    "email": "mjayavant@redis.com",
    "city": "Paris",
    "company": "Redis",
    "preferredColour": "Green",
    "streetAddress": "45 Rue des Saints-Pères",
    "plannedCareer": "Engineer",
    "university": "Université Paris Cité",
    "age": 37
}

// Return Student with parth param id 
curl GET 'http://localhost:8080/student/id/100
{
    "title": "Rev",
    "firstName": "Weider",
    "lastName": "Nisen",
    "gender": "Female",
    "birth": "02/05/2000",
    "email": "wnisen2p@un.org",
    "city": "Agdangan",
    "company": "Tagfeed",
    "preferredColour": "Orange",
    "streetAddress": "345 Shoshone Plaza",
    "plannedCareer": "Automation Specialist I",
    "university": "Technological University of the Philippines",
    "age": 21
}

// Search Student by firstName & lastName
curl GET http://localhost:8080/student/search/name/Oliwia?offset=1000&page=0
{
    "totalResults": 1,
    "documents": [
        {
            "id": "student:2",
            "score": 1.0,
            "properties": [
                {
                    "$": "{\"title\":\"Mr\",\"firstName\":\"Oliwia\",\"lastName\":\"Jagoda\",\"gender\":\"Male\",\"birth\":\"04/05/1985\",\"email\":\"ojagoda@redis.com\",\"city\":\"Paris\",\"company\":\"Redis\",\"preferredColour\":\"Blue\",\"plannedCareer\":\"Engineer\",\"university\":\"University of Mumbai\",\"age\":36}"
                }
            ]
        }
    ]
}

// Search Student by city (TAG field)
http://localhost:8080/student/search/city/Paris?offset=1000&page=0
{
    "totalResults": 2,
    "documents": [
        {
            "id": "student:1",
            "score": 1.0,
            "properties": [
                {
                    "$": "{\"title\":\"Mrs\",\"firstName\":\"Maya\",\"lastName\":\"Jayavant\",\"gender\":\"Female\",\"birth\":\"07/01/1985\",\"email\":\"mjayavant@redis.com\",\"city\":\"Paris\",\"company\":\"Redis\",\"preferredColour\":\"Green\",\"streetAddress\":\"45 Rue des Saints-Pères\",\"plannedCareer\":\"Engineer\",\"university\":\"Université Paris Cité\",\"age\":37}"
                }
            ]
        },
        {
            "id": "student:2",
            "score": 1.0,
            "properties": [
                {
                    "$": "{\"title\":\"Mr\",\"firstName\":\"Oliwia\",\"lastName\":\"Jagoda\",\"gender\":\"Male\",\"birth\":\"04/05/1985\",\"email\":\"ojagoda@redis.com\",\"city\":\"Paris\",\"company\":\"Redis\",\"preferredColour\":\"Blue\",\"plannedCareer\":\"Engineer\",\"university\":\"University of Mumbai\",\"age\":36}"
                }
            ]
        }
    ]
}


// Full Text Search Student by 'generic' search term accross all fields)
http://localhost:8080/student/search/generic/Paris?offset=1000&page=0
{
    "totalResults": 3,
    "documents": [
        {
            "id": "student:1",
            "score": 1.0,
            "properties": [
                {
                    "$": "{\"title\":\"Mrs\",\"firstName\":\"Maya\",\"lastName\":\"Jayavant\",\"gender\":\"Female\",\"birth\":\"07/01/1985\",\"email\":\"mjayavant@redis.com\",\"city\":\"Paris\",\"company\":\"Redis\",\"preferredColour\":\"Green\",\"streetAddress\":\"45 Rue des Saints-Pères\",\"plannedCareer\":\"Engineer\",\"university\":\"Université Paris Cité\",\"age\":37}"
                }
            ]
        },
        {
            "id": "student:369",
            "score": 1.0,
            "properties": [
                {
                    "$": "{\"title\":\"Ms\",\"firstName\":\"Augy\",\"lastName\":\"Muldoon\",\"gender\":\"Male\",\"birth\":\"27/08/1997\",\"email\":\"amuldoona6@pcworld.com\",\"city\":\"Toulouse\",\"company\":\"Flashspan\",\"preferredColour\":\"Yellow\",\"streetAddress\":\"97741 Mockingbird Plaza\",\"plannedCareer\":\"Operator\",\"university\":\"Université René Descartes (Paris V)\",\"age\":24}"
                }
            ]
        },
        {
            "id": "student:601",
            "score": 1.0,
            "properties": [
                {
                    "$": "{\"title\":\"Mrs\",\"firstName\":\"Ginnifer\",\"lastName\":\"Spittal\",\"gender\":\"Male\",\"birth\":\"23/01/1997\",\"email\":\"gspittalgm@vkontakte.ru\",\"city\":\"Périgny\",\"company\":\"Mycat\",\"preferredColour\":\"Indigo\",\"streetAddress\":\"3847 Pepper Wood Court\",\"plannedCareer\":\"Quality Control Specialist\",\"university\":\"Université Panthéon-Assas (Paris II)\",\"age\":25}"
                }
            ]
        }
    ]
}
```

6. // TODO - Provide some advanced examples of using RediSearch capabilities 

```

