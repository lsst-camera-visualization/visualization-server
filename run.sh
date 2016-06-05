#!/bin/bash

function print_usage {
	echo -e "\nUsage:\n$0 Port# \n"
}

if [ $# -ne 1 ]
then
	print_usage
	exit 1
fi

if [[ ( $1 == "--help") ||  $1 == "-h" ]] 
then 
	print_usage
	exit 0
fi 

echo "Running on port $1"

mvn install
mvn exec:java -Dexec.mainClass=org.lsst.ccs.web.visualization.rest.Main -Dexec.args="$1"

