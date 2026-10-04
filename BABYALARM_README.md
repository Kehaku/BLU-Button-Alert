# BLU ntfy Alarm

General-purpose Android adaptation of Sara Button for supported Shelly BLU/BTHome remotes.

## What it does
- Detects supported Shelly BLU button events in the background.
- User enters their own ntfy server, topic, and message **after installation**.
- Sends an ntfy notification with fixed priority **5 (max)** when the configured press event is detected.
- Includes a **Testalarm senden** button.
- No ntfy topic is hard-coded in this repository.
- No phone number or call permission is required for the alarm path.

## Privacy
The ntfy settings are stored locally in Android app preferences. Do not publish screenshots containing a private topic. Anyone who knows an unauthenticated ntfy topic can publish to it.

## Build
GitHub Actions workflow: `.github/workflows/build-babyalarm.yml`.
Download the `BLU-ntfy-Alarm-APK` artifact after a successful workflow run.

## Original project
Based on Sara Button by Ofer Tiber, used under its MIT license. See LICENSE.
