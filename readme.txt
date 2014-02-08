---------------------------
README FOR MESSAGE COMPARE
---------------------------
Technology & Versions : 
a) akka 2.2.3 (http://akka.io/downloads/)
b) database : inmemorydb (part of code base)
c) queues : 2 one each for source and target (deployed in weblogic)
d) queue readers : standard java classes.

Package Contents
1) Project Code - messagecompareengine
2) Weblogic script - setup domain, admin server and two queues.

For enterprise deployment
1) Replace inmemorydb with a preffered database.
2) Use MDB to read the queues
3) Use seperate message infrastructure such as IBM MQ 

How to use it
1) Import the project in eclipse
2) Setup weblogic using the weblogic script
3) Start the weblogic server
4) CompareMain is the MAIN class for the project
5) Run CompareMain with the below 3 arguments.
t3://localhost:7001 jms/SourceConnectionFactory,jms/MySourceQueue,SOURCE jms/TargetConnectionFactory,jms/MyTargetQueue,TARGET

