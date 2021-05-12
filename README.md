# Cooee Android SDK

[![GitHub version](https://badge.fury.io/gh/letscooee%2Fcooee-android-sdk.svg)](https://badge.fury.io/gh/letscooee%2Fcooee-android-sdk)

## What is Cooee?

Let’s Cooee powers hyper-personalized and real time engagements for mobile apps based on machine learning. The SaaS platform, hosted on
 cloud infrastructure processes millions of user transactions and data attributes to create unique and contextual user engagement
 triggers for end users with simple SDK integration that requires no coding at mobile app level.
 
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

## Development

There are two projects-

1. `cooee-android-sdk` - This contains the code of actual Android SDK/library which will be shipped.
2. `app` - This contains an Android app which can be used to run the SDK.

## Installation

Following are the guidelines for installing Cooee SDK on to your Android mobile app.

### Step 1: Project Level

Add following in project level `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url "https://letscooee.jfrog.io/artifactory/default-maven-local" }
    }
}
```

### Step 2: App Level

Add following in app level `build.gradle`:

```groovy
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.letscooee:cooee-android-sdk:x.x.x'
}
```

### Step 3: Configure credentials

Add following in `AndroidManifest.xml` inside `application` tag:

```xml
<meta-data
        android:name="COOEE_APP_ID"
        android:value="MY_COOEE_APP_ID"/>

<meta-data
        android:name="COOEE_APP_SECRET"
        android:value="MY_COOEE_APP_SECRET"/>
```

Replace `MY_COOEE_APP_ID` & `MY_COOEE_APP_SECRET` with the app id & secret given to you separately.

### Step 4: Initialize the Cooee SDK

In the `onCreate()` method of your application’s main activity, add the below line. This will initialize the Cooee SDK.

```java
CooeeSDK cooeeSDK = CooeeSDK.getDefaultInstance(this);
```

### Step 5: Track custom Events

Once you integrate the SDK, Cooee will automatically start tracking events. You can view the collected events in System Default Events. Apart from these, you can track custom events as well.

```java
CooeeSDK sdkInstance = CooeeSDK.getDefaultInstance(context);

Map<String, Object> eventProperties = new HashMap<>();
userData.put("product id", "1234");
userData.put("product name", "Wooden Table");

sdkInstance.sendEvent("Add To Cart", eventProperties);
```

### Step 6: Track User Properties

As the user launches the app for the first time, Cooee will create a user profile for them. By default, we add multiple properties for a
 particular user which you can see in System Default User Properties. Along with these default properties, additional custom attributes
 properties can also be shared. We encourage mobile apps to share all properties for better machine learning modelling.

```java
CooeeSDK sdkInstance = CooeeSDK.getDefaultInstance(context);

Map<String, String> userProperties = new HashMap<>();
userProperties.put("purchased_before", "yes");
userProperties.put("foo", "bar");
userProperties.put("product_viewed", 5);

Map<String, String> userData = new HashMap<>();
userData.put("name", "John Doe");
userData.put("mobile", "9876543210");
userData.put("email", "john@example.com");

sdkInstance.updateUserProfile(userData, userProperties);
```