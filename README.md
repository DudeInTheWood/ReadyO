# ReadyO!

A cassette-inspired Android player for internet radio and local music. Browse live stations, link a music folder, or scan your device. Swipe up on the mini player to open the animated cassette deck.

## Features

- Internet radio with category browsing, search, and a saved list of the 20 most recently played stations.
- Local music from device scans, linked folders, and imported files.
- Folder hierarchy with breadcrumbs, genre cards, and scroll positions preserved while browsing.
- Daily Mix with a saved daily queue, artist spacing, local listening history, and manual refresh.
- Embedded track metadata with filename fallback, audio quality details, and an animated cassette deck.
- Persistent scan cache and last-selected main tab.

## Screenshots

<p>
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/13c5346507db83a0240467e23bdc0aca530ded97/readyo/radio.webp" width="30%" alt="Radio tuner and numbered station presets">
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/13c5346507db83a0240467e23bdc0aca530ded97/readyo/library.webp" width="30%" alt="Local music library with Daily Mix">
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/13c5346507db83a0240467e23bdc0aca530ded97/readyo/player.webp" width="30%" alt="Cassette player with illuminated transport controls">
</p>

<p>
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/13c5346507db83a0240467e23bdc0aca530ded97/readyo/folders.webp" width="45%" alt="Folder hierarchy with storage roots and imported files">
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/13c5346507db83a0240467e23bdc0aca530ded97/readyo/genres.webp" width="45%" alt="Genre cards with matching archive icons and song counts">
</p>

### Now Playing on an unfolded screen

<img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/13c5346507db83a0240467e23bdc0aca530ded97/readyo/unfolded.webp" width="600" alt="Unfolded Now Playing screen with the cassette and playback controls side by side">

## Run on Android

Open the project in Android Studio with JDK 21 and the Android SDK installed. Select the androidApp configuration and run it on a phone or foldable emulator.

From PowerShell, build and test with:

~~~powershell
.\gradlew.bat :androidApp:assembleDebug :shared:testAndroidHostTest
~~~

The interface is built with Kotlin and Compose Multiplatform. Android playback uses Media3. The iOS entry point shares the interface and still needs a native playback adapter. See [Architecture](ARCHITECTURE.md) for the feature layout and data flow.

## Optimized Android build

Build a release APK signed with your existing local Android debug key:

~~~powershell
.\gradlew.bat :androidApp:assembleRelease -PsignReleaseWithDebugKey=true
~~~

The APK is written to androidApp/build/outputs/apk/release/androidApp-release.apk. R8 optimization and resource shrinking are enabled. Keep the same signing key when installing updates over an existing copy.

Music is indexed locally. Sources → Rescan picks up external file additions, removals, and tag changes. Tracks without genre tags appear under Unknown genre. Daily Mix saves up to 25 files for the day and adjusts future selections using listening time, completions, replays, and early skips. Listening history stays on the device.
