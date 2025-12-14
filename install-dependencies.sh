#!/bin/bash

echo "Installing Java and Maven for Nutrition Tracker..."

# Update package list
sudo apt update

# Install OpenJDK 17
echo "Installing Java 17..."
sudo apt install -y openjdk-17-jdk

# Install Maven
echo "Installing Maven..."
sudo apt install -y maven

# Verify installations
echo ""
echo "Verification:"
java --version
echo ""
mvn --version

echo ""
echo "Installation complete! You can now run:"
echo "  mvn spring-boot:run    # to start the backend"
