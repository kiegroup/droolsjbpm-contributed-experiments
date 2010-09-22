The designer is a web-based BPMN2 editor for Drools Flow, integrated into Guvnor.  It is based on the open-source Oryx editor.

Oryx is a web based visual modeller hosted at google code:
http://code.google.com/p/oryx-editor/

Oryx is also backed by Signavio, who provide a professionally maintained version of Oryx:
http://www.signavio.com/en.html

Drools Flow is currently integrated with the WAR provided from the Oryx branch maintained by Antoine Toulme, http://github.com/atoulme. This branch is maintained with the aim of apply patches in a timely manner that can be upstream to the Oryx project. The latest version can be downloaded from:
http://github.com/intalio/process-designer/


How to use the designer:

Download the designer.war, deploy as exploded archive and start up the server.
Note that on some AS you might have to remove 
xercesImpl-2.9.1.jar 
xalan-2.7.1.jar 
slf4j-api-1.5.11.jar 
servlet-api-2.5.jar 
if those are already provided by the AS.

You should be able to open the designer at http://localhost:8080/designer/editor
You should also see the editor when you try to open a *.bpmn file in Guvnor.  Note however that Oryx still uses the BPMN2 beta1 format, so you should make sure you are using that version and not the latest (or Oryx will not be able to parse your input).

Remarks:
Oryx currently requires Java6
Oryx currently requires Google Chrome (currently FireFox support is not available but should be soon)
