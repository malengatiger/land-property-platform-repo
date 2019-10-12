#!/usr/bin/env bash

# ------------ start Bank NODE
cd build/nodes/Bank
java -jar corda.jar
#java -Dcapsule.jvm.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005" -jar corda.jar
