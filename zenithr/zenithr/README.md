# ZenithR

ZenithR is an application that will load a json-defined set of rules and start a Drools engine. Then a REST API will be exposed providing the possibility to execute such rules as a service.

## Build the application

```{bash}
$ mvn clean package
```

## Run the application locally

When running the application, the `GET` environment variable must contain the rules definition.

```{bash}
$ GET=`cat src/test/resources/definitions/grade-letters.json` mvn thorntail:run 
```

If you want to remotelly debug the application, set the following parameter `-Dthorntail.debug.port=<DEBUG_PORT>`

## Run the application standalone

When running the application, the `GET` environment variable must contain the rules definition.

```{bash}
$ GET=`cat src/test/resources/definitions/grade-letters.json` java -jar target/zenithr-thorntail.jar
```

## Invoke the service

### Basic types only

For basic types a form-based URL is supported just type in your browser the following URL

http://localhost:8080/)

And both GET and POST can be used:

```{bash}
$ curl http://localhost:8080/\?grade\=30
F
```

```{bash}
$ curl -XPOST -H "Content-Type: application/json" http://localhost:8080/rest --data '{"grade": 60}'
D
```

### Complex types

For more complex types, the form-based URL will have a text input in which it is possible to add the json object.

Example of complex-objects rules:

```{bash}
$ curl -XPOST -H "Content-Type: application/json" http://localhost:8080/ --data '{"person": "{\"name\":\"Kermit\", \"age\": 18}"}'
Kermit can drive

$ curl -XPOST -H "Content-Type: application/json" http://localhost:8080/ --data '{"person": "{\"name\":\"Gonzo\", \"age\": 16}"}'
I don't know you but you can't drive
```
 