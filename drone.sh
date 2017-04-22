#!/bin/bash

# Build script for drone.io
# Usage:
#    source drone.sh
#
# Requires following environment variables:
#    ORG_GRADLE_PROJECT_bitbucketDevApiKey
#    ORG_GRADLE_PROJECT_bitbucketDevApiSecret
#    ORG_GRADLE_PROJECT_bitbucketDevCallbackScheme
#    ORG_GRADLE_PROJECT_bitbucketDevCallbackHos
#    ORG_GRADLE_PROJECT_bitbucketProdApiKey
#    ORG_GRADLE_PROJECT_bitbucketProdApiSecret
#    ORG_GRADLE_PROJECT_bitbucketProdCallbackScheme
#    ORG_GRADLE_PROJECT_bitbucketProdCallbackHost

# echo commands being executed but do not show contents of environment variables
set -v

PATH=$(echo $PATH | sed 's/\/opt\/android-sdk-linux//')
sudo apt-get update -qq
sudo apt-get install -qq --force-yes expect
if [ `uname -m` = x86_64 ]; then sudo apt-get install -qq --force-yes libgd2-xpm ia32-libs ia32-libs-multiarch > /dev/null; fi

# Install Android SDK components (and emulator for tests if needed)
COMPONENTS="build-tools-23.0.3,android-23,extra-android-m2repository"
LICENSES="android-sdk-license-c81a61d9"
curl -L https://raw.github.com/embarkmobile/android-sdk-installer/version-2/android-sdk-installer | bash /dev/stdin --install=$COMPONENTS --accept=$LICENSES && source ~/.android-sdk-installer/env

# Use the same debug key on every build which makes updating from one build to another possible
cp debug.keystore ~/.android/

# Run PMD, CheckStyle, lint and unit tests
./gradlew -Djava.awt.headless=true :app:check

# Prepare an .apk for downloads
./gradlew -Djava.awt.headless=true :app:assembleDevDebug

# Provide test results as an downloadable Artifact
sudo apt-get install -qq zip
mv app/build/outputs/lint-results*.html app/build/outputs/reports/
mv app/build/outputs/lint-results*_files/ app/build/outputs/reports/
mv app/build/reports/tests/ app/build/outputs/reports/
cd app/build/outputs/reports/
zip -r testResults.zip .
