#!/bin/bash
# Start the backend server with the local profile

mvn spring-boot:run -Dspring-boot.run.profiles=local
