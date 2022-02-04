# Cooee Android SDK

![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/letscooee/cooee-android-sdk?label=Latest%20Release)

## What is Cooee?

Let’s Cooee powers hyper-personalized and real time engagements for mobile apps based on machine learning. The SaaS platform, hosted on
 cloud infrastructure processes millions of user transactions and data attributes to create unique and contextual user engagement
 triggers for end users with simple SDK integration that requires no coding at mobile app level.

 For More information visit our [website](https://www.letscooee.com/) and [documentation](https://docs.letscooee.com/developers/ios/quickstart).
 
## System Requirements

- Platform – Android
- Language - Java
- IDE - Android Studio, VSCode
- Minimum Android Support – Lollipop 5.0 (API level 21)

## Features

1. Plug and Play - SDK is plug and play for mobile applications. That means it needs to be initialized with the Application Context and it
 will work automatically in the background.
2. Independent of Application - SDK is independent of the application. It will collect data points with zero interference from/to the
 applications. Although applications can send additional data points (if required) to the SDK using API’s.
3. Rendering engagement triggers - SDK will render the campaign trigger at real-time with the help of server http calls.
4. Average SDK size – 5-6 MB(including dependency SDK’s).

## Installation & Uses

For detailed installation & uses, Refer [Android](https://docs.letscooee.com/developers/android/quickstart) documentation.

## Development

There are two projects-

1. `cooee-android-sdk` - This contains the code of actual Android SDK/library which will be shipped.
2. `app` - This contains an Android app which can be used to run the SDK.

### Publishing in Maven Local

Run-

```shell script
./gradlew cooee-android-sdk:publishToMavenLocal
```