#!/bin/bash
redis-cli KEYS farmhand:\* | xargs redis-cli DEL
lein uberjar && java -jar target/uberjar/farmhand-benchmark-0.1.0-SNAPSHOT-standalone.jar
