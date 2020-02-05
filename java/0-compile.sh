#!/bin/bash -x
GSON=./gson/gson-2.8.5.jar
PAS=./pas/PASJava.jar
DAP=./dap/DAPJava.jar
JAVAREST=./javarest/JavaREST.jar
ONBOARD_SRC="OnboardProject.java AccessRequest.java AccountRequest.java Identity.java"

javac -cp $GSON:$PAS:$DAP:$JAVAREST $ONBOARD_SRC
echo "Main-Class: OnboardProject" > manifest.txt
echo "Class-Path: $GSON $PAS $DAP $JAVAREST" >> manifest.txt
jar cvfm OnboardProject.jar manifest.txt *.class 

rm manifest.txt
