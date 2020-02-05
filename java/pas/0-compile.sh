#!/bin/bash -
PASPLATFORM_SRC="PASPlatformList.java PASPlatform.java PASPlatformGeneral.java PASPlatformProperties.java" 
PASSAFE_SRC="PASSafeList.java PASSafe.java PASSafeAdd.java"
PASACCOUNT_SRC="PASAccountList.java PASAccount.java PASSecretManagement.java PASRemoteMachinesAccess.java"
PASACCOUNTDETAILS_SRC="PASAccountDetailList.java PASAccountDetail.java KeyValue.java" 
PASACCOUNTGROUP_SRC="PASAccountGroup.java PASAccountGroupMember.java"
javac -cp ../gson/gson-2.8.5.jar:../javarest/JavaREST.jar \
	PASJava.java \
	$PASPLATFORM_SRC \
	$PASSAFE_SRC \
	$PASACCOUNT_SRC \
	$PASACCOUNTDETAILS_SRC \
	$PASACCOUNTGROUP_SRC
jar cvf PASJava.jar *.class 
