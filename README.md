# ReadyO!

A cassette-inspired Android player for internet radio and local music. Browse live stations, link a music folder, or scan your device. Swipe up on the mini player to open the animated cassette deck.

## Screenshots

<p>
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/5ef06f65f5f7cee74be7e9d3b32c8b705d95b033/readyo/radio.webp" width="30%" alt="Radio tuner and numbered station presets">
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/5ef06f65f5f7cee74be7e9d3b32c8b705d95b033/readyo/library.webp" width="30%" alt="Local music collection with numbered record sleeves">
  <img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/5ef06f65f5f7cee74be7e9d3b32c8b705d95b033/readyo/player.webp" width="30%" alt="Cassette player with illuminated transport controls">
</p>

### Now Playing on an unfolded screen

<img src="https://raw.githubusercontent.com/DudeInTheWood/MyImage/5ef06f65f5f7cee74be7e9d3b32c8b705d95b033/readyo/unfolded.webp" width="600" alt="Unfolded Now Playing screen with the cassette and playback controls side by side">

## Run on Android

Open the project in Android Studio with JDK 21 and the Android SDK installed. Select the androidApp configuration and run it on a phone or foldable emulator.

From PowerShell, build and test with:

~~~powershell
.\gradlew.bat :androidApp:assembleDebug :shared:testAndroidHostTest
~~~

The interface is built with Kotlin and Compose Multiplatform. Android playback uses Media3. The iOS entry point shares the interface and still needs a native playback adapter. See [Architecture](ARCHITECTURE.md) for the feature layout and data flow.
