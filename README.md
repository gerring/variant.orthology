
## Variant Orthology
The purpose of this project is to be a self contained and reproducible mechanism for
importing orthology graphs to Neo4j. See the 'doc' folder for an explanation of the
intended architecture on the cloud. This is the component in the diagram which is the import 
mechanism. We want to import data in the hundreds of Gb range therefore 500Mb files
have been added to the tests here to get the fastest local mechanism practically 
possible. This then should be deployed as an import pipeline in the cloud.


Current timings per node for importing and creating the graph are as follows:
Run on Macbook pro, 16Gb RAM various test up to 1.7 million nodes. Time in ms/node
* Python (1 thread py2neo):  								8.8
* Java (1 thread):											4.9
* Java (1 thread, multi-transaction commit/1000):			2.1
* Java (parallel, commit/1000):								0.6


This means that the python script running in the cloud without parallel processing 
for 170Gb of data which is node count 33 - 578 million will take hours
80 - 1412 unless multi-processed in the cloud using DASK. The Java parallel will take
5.5 - 96. So parsing and adding the nodes of a 170Gb dataset of variant data will still
take many days.

# How It Works
This project builds a fat jar file which may be deployed on GCP/Azure etc. This file
will be deployed as a google function triggered by new google cloud storage events to 
read new gtf and gvf files as they are  uploaded.

This project covers only the data engineer part of the architecture.

# Getting Started
* JDK 11 LTS
* Install maven, e.g. on MacOs `brew install maven`
* If using Eclipse the pom.xml will resolve neo4j libraries.
* install docker, e.g. on MacOS:  https://docs.docker.com/docker-for-mac/install/
* `docker pull neo4j:4.1.1`    (or your version, if not using 4 pom.xml will need to be changed in this product)
* You will need to set IDE to use JUnit 5 if 4 is its default.

# TODO
* apoc library and 4.1.1 is possible but no docker container.
* apoc writer to replace shell script in original version.

# Build 

`mvn install` This will run tests, code coverage and source code compliance.


# Recommended Eclipse Plugins
If using Eclipse IDE, here is a list of recommended plugins:
1. Maven
2. Checkstyle
3. EclEmma for Jacoco
4. Pydev (if editing python fromv variant orthology V1.)