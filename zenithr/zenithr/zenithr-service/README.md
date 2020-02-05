# ZenithR

ZenithR is an application that will load a json-defined set of rules and start a Drools engine. 
Then a REST API will be exposed providing the possibility to execute such rules as a service.

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
$ RULES_DEFINITION=`cat src/test/resources/definitions/sysops-example.json` java -jar target/zenithr-1.0.0-SNAPSHOT-runner.jar
```

## Native application

```{bash}
$ mvn clean package -Pnative
$ RULES_DEFINITION=`cat src/test/resources/definitions/sysops-example.json` ./target/zenithr-1.0.0-SNAPSHOT-runner
```

## Invoke the service

### Complex types

The service expects one JSON object for each input defined in the definition.

Execution examples rules executions:

```{bash}
$ curl -v -X POST http://localhost:8080/ \                                                   
 -H "Content-Type: application/json" \
 --data-binary "@src/test/resources/requests/deployment-replicas3.json" | jq

[
  {
    "name": "deployment2",
    "path": "spec",
    "value": {
      "replicas": "2"
    }
  },
  {
    "name": "deployment3",
    "path": "metadata.labels",
    "value": {
      "example": "default-broker-filter"
    }
  }
]
```
```{bash}
$ curl -v -X POST http://localhost:8080/ \                                                   
 -H "Content-Type: application/json" \
 --data-binary "@src/test/resources/requests/deployment-replicas3.json" | jq

[
  {
    "name": "deployment2",
    "path": "spec",
    "value": {
      "replicas": 1
    }
  },
  {
    "name": "deployment3",
    "path": "spec",
    "value": {
      "replicas": 0
    }
  }
]
```
 