# Change Log

## v1.3.5

### Fixes

1. Fix push notification rendering in Android 12.
2. Fix close In-App animation from corners.

## v1.3.4

### Fixes

1. Prevent uploading screenshot if token is not acquired.
2. Fix handling of key-value in CTA.
3. Prevent possibility of animation close exception if in-app wasn't loaded properly.

## v1.3.3

### Improvement 

1. AR SDK is now optional.

## v1.3.2

### Feature

1. Add entry and exit animation from corners.
2. Remove requirement for `COOEE_SECRET_KEY`.
3. Push Notification can perform CTA action.

### Improvement

1. InAppBrowser is replaced with CustomTabsIntent.
2. InApp will have its own base property.
3. InApp will have default close CTA.
4. Update property key for duration in CTA, Foreground, Background.
5. Expose `CooeeSDK.sendEvent(String)` and `CooeeSDK.updateUserProfile(Map)` API.

### Deprecated

1. Deprecated `CooeeSDK.updateUserData(Map)`, `CooeeSDK.updateUserProperties(Map)`, `CooeeSDK.updateUserProfile(Map, Map)`.

## v1.1.1

### Feature

1. Add ability to move In-App at any side of screen.
2. Now In-App can be of any size.

### Improvements

1. Collection of event property is now grouped and renamed.

### Fixes

1. Fix issue with Part Rendering.
2. Fix issue with view's background & border.
3. Fix issue with Image rendering when out of viewport.

## v1.1.0

### Feature

1. Templates Engagement (Using payload version 4)
2. Tracking of expired Engagement
3. Add Support to the latest Android 12

### Fixes

1. Fix issue with `DebugInfoActivity`. Now developer will decide when to activate
2. Job scheduler issue with Android M
3. Fix issue with permission data

## v1.0.2

### Fixes

1. Add theme to `DebugInfoActivity`.
2. Fix issue for CTA when position is `Position.PositionType.ABSOLUTE`

## v1.0.1

### Fixes

1. Expose `InAppTriggerActivity#captureWindowForBlurryEffect` to be used in other plugins.

### Chore

1. Run test cases & publish to maven local for every merge requests.

## v1.0.0

### Required Changes
1. Rename `InAppNotificationClickListener` to `CooeeCTAListener`

```diff
- public class YourActivity implements InAppNotificationClickListener {
+ public class YourActivity implements CooeeCTAListener {
```

2. Update overridden method `onInAppButtonClick` to `onResponse`

```diff
- public void onInAppButtonClick(HashMap<String, Object> hashMap) {
+ public void onResponse(HashMap<String, Object> hashMap) {
```

3. Update `setInAppNotificationButtonListener()` to `setCTAListener()`

```diff
- cooeeSDK.setInAppNotificationButtonListener(this);
+ cooeeSDK.setCTAListener(this);
```

### New Feature

1. Added Shake detection  
Add SHAKE_TO_DEBUG_COUNT meta in your AndroidManifest.xml to manual configuration of shake detector

```xml
<!-- Change value to 0 if you don't want to open debug information when device shake-->
<meta-data
    android:name="SHAKE_TO_DEBUG_COUNT"
    android:value="ANY_NUMBER" />
```
2. Added Augmented Reality for better user experience

### Improvements

1. In-App triggers are now template-less.
2. Both in-app and external browser support on CTA.
3. Multiple permission request prompts on CTA.
4. Share content support on CTA
5. Augmented Reality support on CTA  
4. Device and User info screen with shake detector for debugging.

## v0.3.2

### Improvements

1. Give preference to `bluetooth_name` before `device_name`.

### Bug Fixes

1. Fixed non-ASCII characters issue for device name in header.

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
