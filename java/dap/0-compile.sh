#!/bin/bash -x
javac -cp ../javarest/JavaREST.jar DAPJava.java Variable.java
jar cvf DAPJava.jar *.class 
