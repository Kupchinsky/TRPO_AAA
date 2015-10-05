#!/bin/bash
java -jar "target/AAA-1.0-SNAPSHOT.jar" $*
echo Exit code: $?
exit $?