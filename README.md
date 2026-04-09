# AoCA Smart App / Dementia Tester

## Table of Contents:
- [Project Overview](#overview)
  - [Features](#features)
  - [Directory Structure](#directory-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Firebase setup](#firebase-setup)
  - [Building and running](#building-and-running)
    - [Android](#android)
    - [iOS](#iOS)
- [Deployment](#deployment)
- [Learn More](#learn-more)

## Overview
The AoCA (Agent-oriented Cognition-based Smart Assistant) Smart App is a cross-platform mobile application 
designed to help assess and monitor cognitive abilities in individuals, particularly those at risk of or 
experiencing dementia. The app provides tools for cognitive assessment, activity tracking, appointment 
scheduling, and communication with healthcare providers, supporting both early detection and ongoing 
management.

In addition to structured assessments, the app offers features that encourage routine engagement, 
such as reminders for daily activities, progress tracking over time, weekly health surveys, and secure 
data sharing with clinicians or carers. These capabilities aim to give users and their support networks 
better visibility of cognitive changes and allow healthcare professionals to make more informed decisions.

### Features
- **Dashboard**: Central hub with access to reminders, cognitive tests, games, and progress tracking
- **Health Survey**: Tool for assessing cognitive health and tracking changes over time
- **Cognitive Assessments**: Structured tests to evaluate memory, attention, and other cognitive abilities
- **Mini Games**: Short interactive logic games designed to engage users while measuring cognitive performance
- **User Reminders**: Customisable reminders for daily activities, surveys, and appointments
- **Progress Tracking**: Visual reports to monitor trends and changes in cognitive performance
- **Appointment Booking**: Schedule appointments with healthcare providers
- **Contact & Chat**: Communication tools to connect with healthcare professionals
- **Local notifications**: Local push notifications for task reminders, and survey and quiz reminders
- **Doctor specific views**: Specific functionality for doctors to view and manage patients' results

### Directory Structure
* `/composeApp` - Contains code shared across platforms using Compose Multiplatform
  * `/src/commonMain` - Common code for all platforms
    * `/kotlin/org/example/dementia_tester_app` - Main application code
      * `App.kt` - Main application entry point
      * `Platform.kt` - Platform-specific interface
      * `/auth` - Authentication service logic
      * `/ui` - User interface components
        * `/components` - Reusable UI components
        * `/screens` - Application screens
      * `/data` - Database logic
      * `/notifications` - Local push notification logic
      * `/utils` - Utility functions
  * `/src/androidMain` - Android-specific code
  * `/src/iosMain` - iOS-specific code
  * `/src/commonTest` - Unit tests
* `/iosApp` - iOS application entry point

## Getting Started

### Prerequisites
- Android Studio or IntelliJ IDE
- Xcode 16+ or newer (for iOS development)
- JDK 22+
- Kotlin Multiplatform Mobile plugin

### Firebase Setup
To get the `google-services.json` file for Firebase auth and database access, follow the below steps:

1. Log into the Firebase project
2. Go to the Project overview page
3. At the top of the page under the title, click on the `Dementia-Tester` app, then click settings.
4. Scroll down and under the section `Your apps` find the android version of the `google-services.json` and download it.
5. Once downloaded, place the file in the root of `/composeApp`.

**Notes**:
* Do not commit the `google-services.json` file to GitHub.
* For iOS development, follow the same steps as above but download the `GoogleService-Info.json` file for the iOS app and 
place the file in the root of `/iosApp`.

### Building and Running

#### Android
1. Open the project in Android Studio or IntelliJ IDEA
2. Wait for ~30 seconds for the IDE to load the Gradle settings
3. Select the "composeApp" configuration
4. Choose an Android device or emulator
5. Click the "Run" button

#### iOS
1. Open the `/iosApp` directory in Xcode
2. Wait for ~30 seconds for the IDE to load the Gradle settings
3. Choose an iOS device or simulator
4. Click the "Build" button

## Deployment
Both deployment options are automated using GitHub actions. For android the action builds a 
android app bundle file (.aab), and for iOS it builds and uploads the app to TestFlight.

### Android:
To deploy the app to the Google Play Store using the Google Play console, follow the below steps:

1. In the [composeApp/build.gradle.kts](composeApp/build.gradle.kts) file, under the android {},
increment the `versionCode` number and commit it to GitHub.
2. In the `Actions` tab in the GitHub repository, click on the `ComposeApp .aab Build` workflow.
3. Click `Run workflow`, choose the branch you want to run the workflow with.
4. Wait for the workflow to finish (May take 5–10 minutes to complete).
5. Click on the workflow, scroll down, and there should be an `Artifacts` section with a file in it, 
this is your `.aab` file that gets uploaded to the Google Play console. Download this file.
6. Log into the [Google Play console](https://play.google.com/console), using the dementia tester login,
choose the `AoCA SmartApp`, upload the .aab file, and publish it for testing/production. Consult the 
[Google Play console docs](https://play.google.com/console/about/guides/) for guides on uploading, testing, and publishing the app.

### iOS:
To deploy the app to Apple Store Connect, follow the below steps:

1. In the `Actions` tab in the repository, click the `iOS-deploy.yml` workflow.
2. Click `Run workflow` and choose the branch you want to run the workflow with.
3. Wait for the workflow to finish (May take 15–20 minutes to complete).
4. When the workflow is finished, the app is now built and uploaded to TestFlight.
5. Log into the [App Store Connect](https://appstoreconnect.apple.com/), (either using the 
dementia tester login, or if you are a user on the app, use your login) and go to the 
`AoCA Smart App` app.
6. In the `TestFlight` tab you will see the uploaded build.
7. Consult the [App Store Connect documentation](https://developer.apple.com/help/app-store-connect)
for guides on testing and publishing the app.

**Note:** The version number does not need to be updated for the iOS build as Apple only requires the `build number`
to be unique, which gets auto incremented every time you run the GitHub workflow. For major updates/releases
you should update the version number and to do that follow these steps:
1. Open the project in Xcode.
2. Select the app target, then the General tab.
3. Under the Identity section, increment the `Version` number. The `Build` number under that is the 
unique number that gets auto incremented every workflow build, do not explicitly set the `Build` number.

## Learn More
Below are some helpful resources:
- [Compose Multiplatform Docs](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Material components for Compose](https://developer.android.com/develop/ui/compose/components)
- [IntelliJ IDEA docs](https://www.jetbrains.com/help/idea/getting-started.html)
- [Android Studio docs](https://developer.android.com/studio)
- [Xcode docs](https://developer.apple.com/documentation/xcode)
- [App Store Connect docs](https://developer.apple.com/help/app-store-connect)
- [Firebase docs](https://firebase.google.com/docs)
- [Google Play console docs](https://play.google.com/console/about/guides/)

