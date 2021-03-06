# Change Log

## v0.3.1

### Bug Fixes

1. Fixed occassional `IllegalArgumentException` on first launch.
2. Fixed InApp image distortion in terms of aspect ratio.
3. Added CTA Label property in Trigger Closed event.

## v0.3.0

### Internal Features
1. Added offline compatibility to handle outgoing events.
2. Add payload v2 check.
3. Configurable importance, lights, sound & vibration in push triggers.
4. Support of colours in glassmorphism.

### Improvements

1. Code refactored from the ground up to make it maintainable and modular.
2. Handle push notification according to its importance.
3. Glassmorphism effect now works properly and is configurable.
4. Introduced `Timer` class to handle background thread.
5. Removed Flutter specific code.
6. Sentry related improvements.
7. Log more KPIs from push notifications.
8. Updated KPIs in app launches.
9. Improved In-App display time when app is already in background/foreground.

### Bug Fixes

1. Fixed ability to show push notification without image.
2. Fixed ability to show In-app trigger without image/video.
3. Fixed sending duplicate KPIs when the app is going/coming to/from the background/foreground.
4. Fixed sending FCM token multiple times.

## v0.2.11

### Improvements

1. User properties can now be sent as `<String, Object>`.
2. Improving app load time by initialising Sentry in a separate thread.
3. Increase app launch delay to 6 seconds (temporary).

### Bug Fixes

1. Handling edge cases while rendering push notifications.
2. Fixed Glassmorphosis effect.

## v0.2.10

### Feature

1. Configurable close button, timer and progress bar.

### Bug Fixes

1. In-app triggers rendering for some devices (Samsung) fixed.
2. Some server calls were resulting in 406.
3. Send CTA data in `Map<String, Object>` format.
4. Prevent push notification to restart the app.
5. Initialize Sentry early with global tags to get tags as early as possible.
6. Create Sentry filter to segregate exception from app and SDK.
7. Handle `JsonSyntaxException` for activeTrigger. 

## v0.2.9

1. Fix exception in Android 11 because [new restricted permission](https://developer.android.com/about/versions/11/behavior-changes-11#apn-database-restrictions)
of Telephony manager APN database #COOEE-127

## v0.2.8

This release does not bring any code changes. It only changes the artifactory/maven location of publishing. We have now moved from
 Bintray to JFrog because Bintray has been shutdown. Read more https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/.

### Required Changes

**In your root/project level `build.gradle`-

```diff
allprojects {
    repositories {
-       maven { url "https://dl.bintray.com/wizpanda/maven" }
+       maven { url 'https://letscooee.jfrog.io/artifactory/default-maven-local' }
    }
}
```

## v0.2.7

1. Added heatmap tracking.
2. A supportive fix for Glassmorphism effect in Flutter.
3. Change in activity lifecycle

## v0.2.6

1. Sentry Added to library.
2. Side-Pop Trigger added

## v0.2.5

1. Carousel Notification added.
2. Custom sound for Notification.

## v0.2.3

1. Glassmorphism effect on orientation changes
2. Added GIF layer on triggers  
3. Active triggers tracking

## v0.2.2

1. Testing application has been changed to include the functionality of our SDK for demo purposes. Now has multiple screens.
2. Client's Application icon instead of our default icon in Push Notification.
3. Updated UI/UX for Action Button.
4. Mute button position updated (moved more towards corner) and clickable area of the button is also increased.
5. Removed deprecated functions and used new ones.
6. Progress bar on close button loading.
7. Emoji text on trigger tested.
8. Close button on top right position.
9. Updated UI/UX for landscape mode.

## v0.2.1

1. Fixed sending null value in the request headers.

## v0.2.0

1. Internal change in trigger payload.
2. Glassmorphism effect.
3. Ability to provide callback to the app on trigger button clicks.
4. Updates in the KPI from engagement triggers.
5. Passing device name & user id in the request header.

## v0.1.1

1. Storing Firebase token via a separate endpoint.
2. Keep alive code is now working.
3. Storing user's id in the cache.

## v0.1.0

1. Support of tracking sessions.
2. Support of linking events with screen names.
3. Showing engagement triggers.

## v0.0.3

1. Fixed creating duplicate event `CE App Installed` instead of `CE App Launched`.
2. Various code cleanup and optimization.

## v0.0.2

1. Removed unused `android:networkSecurityConfig`.

## v0.0.1

First working SDK to collect user properties & events.
