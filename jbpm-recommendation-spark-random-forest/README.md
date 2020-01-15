# jBPM Apache Spark Random forest prediction service

## Description

This module contains an example implementation of a prediction service
using Apache Spark as a backend.

The prediction service allows to set a model's predictions as the
Human Task output. If the prediction's `confidence` is higher than the
`confidenceThreshold` then the Human Task is automatically completed.

 The example Human Task has three inputs (`actor`, `level`, `item`) and one output (`approved`).
 
 The service can be configured using the following Java system properties:
 
 * `org.jbpm.prediction.randomforest.spark.master`, specifies the network address of the Spark cluster master. If it is empty
 it will default to `local[4]`, that is, the service will spawn a local cluster, running on the same JVM as the Kie Server. To specify an
 external, remote cluster use Spark's protocol (_i.e._ `spark://$HOST:$PORT`)
 * `org.jbpm.task.prediction.service.confidence_threshold` contains the confidence threshold (as a `String`).
 This can be changed at runtime and the default value is `"1.0"`.

 
 ## Installation
 
 To install the service example simply build the JAR using
 
 ```
$ mvn clean install
```

and place it in the jBPM server's classpath.

The example Human Task located in `test/resources/BPMN2-UserTask.bpmn2` can be imported into jBPM to run the example.

## Deploy a Spark cluster

If you don't have a running external Spark cluster, one can be deployed using the
_stand-alone_ mode.

From your Apache Spark installation directory, run:

```shell
./sbin/start-master.sh
```

This will display the address of your Spark master node (in the format `spark://$HOST:$PORT`).
Next, create one or more Spark worker nodes using the previous address.

```shell script
./sbin/start-slave.sh spark://$HOST:$PORT
```

You can now connect the prediction service to this cluster by setting the
property:

```shell
-Dorg.jbpm.prediction.randomforest.spark.master=spark://$HOST:$PORT
```