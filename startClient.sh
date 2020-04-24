#!/usr/bin/env bash
mvn package -q -DskipTests
java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-*.jar io.github.googlielmo.slopchat.app.client.Slopchat "$@"
