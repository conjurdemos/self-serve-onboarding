#!/bin/bash -x
javac JavaREST.java
echo "Main-Class: JavaREST" > manifest.txt
jar cvfm JavaREST.jar manifest.txt *.class 
rm manifest.txt

