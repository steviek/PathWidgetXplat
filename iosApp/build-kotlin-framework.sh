#!/bin/bash

# Kotlin Framework Build Script for Xcode
# This script ensures Java is available and builds the Kotlin framework

set -e  # Exit on any error

# Check if we should skip the build
if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
    echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
    exit 0
fi

# Check if Android dependencies are available
if [ ! -d "$SRCROOT/../composeApp/src/androidMain" ]; then
    echo "Android dependencies not found, skipping Kotlin framework compilation"
    exit 0
fi

# Set up Java environment
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Use absolute path to Java to avoid PATH issues
JAVA_BIN="$JAVA_HOME/bin/java"

# Verify Java is available
if [ ! -f "$JAVA_BIN" ]; then
    echo "Error: Java not found at $JAVA_BIN"
    echo "Available Java installations:"
    ls -la /opt/homebrew/opt/openjdk@*/bin/java 2>/dev/null || echo "No Java found in Homebrew"
    exit 1
fi

echo "Using Java: $JAVA_BIN"
echo "Java version: $($JAVA_BIN -version 2>&1 | head -1)"

# Change to project root and run Gradle
cd "$SRCROOT/.."
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
