# ZenithR

ZenithR is an application that will load a json-defined set of rules and start a Drools engine. Then a REST API will be exposed providing the possibility to execute such rules as a service.

## Build the application

```{bash}
$ mvn clean package
```

## Run the application locally

When running the application, the `RULES_DEFINITION` environment variable must contain the rules definition.

```{bash}
$ RULES_DEFINITION=`cat src/test/resources/definitions/grade-letters.json` mvn quarkus:dev
```

If you want to remotely debug the application, set the following parameter use port 5005 (default).

## Run the application standalone

When running the application, the `RULES_DEFINITION` environment variable must contain the rules definition.

```{bash}
$ RULES_DEFINITION=`cat src/test/resources/definitions/grade-letters.json` java -jar target/zenithr-1.0.0-SNAPSHOT-runner.jar
```

## Native application

```{bash}
$ mvn clean package -Pnative
$ RULES_DEFINITION=`cat src/test/resources/definitions/grade-letters.json` ./target/zenithr-1.0.0-SNAPSHOT-runner
```

## Invoke the service

### Basic types only

For basic types a form-based URL is supported just type in your browser the following URL

http://localhost:8080/)

And both GET and POST can be used:

```{bash}
$ curl http://localhost:8080/\?grade\=30
{"id":"output","value":"F"}%
```

```{bash}
$ curl -XPOST -H "Content-Type: application/json" http://localhost:8080/ --data '{"grade": 60}'
{"id":"output","value":"D"}%
```

### Complex types

For more complex types, the form-based URL will have a text input in which it is possible to add the json object.

Example of complex-objects rules:

```{bash}
$ curl -XPOST -H "Content-Type: application/json" http://localhost:8080/ --data '{"person": "{\"name\":\"Kermit\", \"age\": 18}"}'
{"id":"output","value":"Kermit can drive"}

$ curl -XPOST -H "Content-Type: application/json" http://localhost:8080/ --data '{"person": "{\"name\":\"Gonzo\", \"age\": 16}"}'
{"id":"output","value":"I don't know you but you can't drive"}
```
 