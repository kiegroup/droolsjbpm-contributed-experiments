-- The Drools BRMS Insurance example --

This example aims to show how you could integrate the BRMS with your application. 

There are 2 main components: 

1) Sample repository (repository_export.xml)
2) Sample end user application

The sample application is NOT the BRMS, it is just a very simple one screen application which consumes rules, using the rule agent. The BRMS manages the rules. 

To use: 

1) Download JBoss Application server (4.2 is recommended)
2) Download and install maven 2 from maven.apache.com (if you haven't already)
3) Open the pom.xml in this directory
 - note the depdency section, there is a dependency on drools-core - when using the agent, this is generally the only drools specific dependency used. 
 - set the dependency to the appropriate version of drools. 
4) Deploy the BRMS war (separate download) to the Application Server (from step 1)
5) Run "mvn package" in this directory - this will build and generate a war for the insurance sample application. 
6) Deploy the insurance sample war to the Application Server (from step 1). 

This will have both BRMS and Sample application running in the one application server, but of course it does not have to be this way (you can adjust settings as appropriate). 





