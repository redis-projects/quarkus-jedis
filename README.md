# Quarkus Redis-Stack Jedis Project

## Intro

A collection of projects testing Redis Modules with Jedis on Quarkus. 
Created specifically to test Quarkus Native compilation & compatibility.

- Please checkout the **quarkus-jedis-json** folder, to view a Quarkus project using RedisJSON + Search
- Please checkout the **quarkus-jedis-graph** folder, to view a Quarkus project using RedisGraph

### Tested Versions: 

Java: 
```bash
openjdk version "17.0.2" 2022-01-18
OpenJDK Runtime Environment Homebrew (build 17.0.2+0)
OpenJDK 64-Bit Server VM Homebrew (build 17.0.2+0, mixed mode, sharing)
```

GraalVM: 
```bash
$js -version
GraalVM JavaScript (GraalVM CE Native 22.0.0.2)

$ lli --version
LLVM 12.0.1 (GraalVM CE Native 22.0.0.2)

```
Redis-Stack
```bash
$ docker image ls
REPOSITORY             TAG      IMAGE ID       CREATED        SIZE
redis/redis-stack      <none>  04462b056858   2 months ago   563MB
$ docker image inspect b2b6dcd01edd
[
    {
        "Id": "sha256:b2b6dcd01edd87d57c2086611c0af02d2954a29aeb406e16ce32cc68a20819e1",
        "RepoTags": [
            "redis/redis-stack:latest"
        ],
        "RepoDigests": [
            "redis/redis-stack@sha256:c1a464b8d123578b490d82327f3baba996cde4617c6310347725d8e54408af19"
        ],
        "Parent": "",
        "Comment": "buildkit.dockerfile.v0",
        "Created": "2022-06-06T08:35:46.34001019Z",
```