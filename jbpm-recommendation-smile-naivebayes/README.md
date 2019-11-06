# jBPM SMILE naïve Bayes prediction service

## Description

This module contains an example implementation of a prediction service
backend using a SMILE naïve Bayes-based example.

The prediction service allows to set a model's predictions as the
Human Task output. If the prediction's `confidence` is higher than the
`confidenceThreshold` then the Human Task is automatically completed.

 The example Human Task has three inputs (`actor`, `level`, `item`) and one output (`approved`).
 
 The service can be configured using the following property files:
 
 * `inputs.properties`, a key/value list of attribute name/attribute type (e.g. `item=NOMINAL` means `item` is a nominal attribute)
 * `output.properties` contains the output attribute name and type and the confidence threshold

 
 ## Installation
 
 To install the service example simply build the JAR using
 
 ```
$ mvn clean install
```

and place it in the jBPM server's classpath.

The example Human Task located in `test/resources/BPMN2-UserTask.bpmn2` can be imported into jBPM to run the example. 