# ReadyO development

The original requirements define product behavior. The current visual direction is Archive Dark from `ReadyO_Cassette_Prototype/ReadyO_V2_Dark_Theme.md` and `ReadyO_V2_Dark_NowPlaying.png`. The user's explicit dark-default request overrides the documents' system-theme default. The supplied HTML is a reference, not application code. Demo stations/tracks are never production data. See [ARCHITECTURE.md](ARCHITECTURE.md) for the current feature-first MVVM/UDF structure and ownership rules.

## Structure
- `shared/src/commonMain`: app composition, reusable `core` contracts/design system, and Radio/Music/Player features with presentation state, actions, ViewModels and stateless UI.
- `androidApp`: small Activity/picker bridge, retained dependency container, Media3 adapter/service under `core/playback`, and native music import under `feature/music/data`.
- `shared/src/iosMain`: shared UI entry point; native AVPlayer adapter remains to be implemented and validated on macOS/Xcode.

## Local library scopes

- My Music now offers **All songs** and **Folders**. Folder cards show their name, storage path and song count. Opening a folder shows its songs with **All folders** navigation; system Back returns to the folder list. Play all and individual song selection build their actual playback queue from the visible scope. Changing browsing scope does not interrupt playback.
- Source management is under **Sources** instead of occupying the main library. Linking folders, device scans, individual imports, Rescan and Unlink remain available. An empty library exposes these actions immediately. Existing imported files and saved source links remain compatible.
- Folders represent directories containing indexed songs, including nested directories, rather than only linked root grants. Each folder count covers its direct songs. Imported copies appear under **Imported files**. Device/folder overlaps share normalized directory identities and canonical track IDs; identical folder names at different paths remain separate.
- Added five shared tests for canonical folder membership, matching/nested folder names, filtered playable lists, rescan/removal updates and folder Back behavior. All **20 shared tests** pass.
- Runtime checks on the folded Pixel 9 Pro Fold used four temporary silent files: two songs in one Mixes folder, one in a different Mixes folder, and one in a nested Live folder. All songs showed seven total files including the original library; Folders showed five collections with correct direct counts. Playing the two-song folder produced a native queue of exactly two songs, and Next did not leave that folder. Linking the same test root preserved the counts; Back returned to Folders. Large text was inspected and the original scale restored. Temporary files and the test folder link were removed after verification.
- Final installed build: the original three songs and two folders remained after cleanup/restart. Folded folder browsing and unfolded folder contents were inspected. Build and formatter check pass, and all 20 shared tests pass. Captures: `verification/music-all-songs.png`, `music-folders.png`, `music-folder-contents-wide.png` and `music-folders-large-text.png`.

## Archive Dark and supplied Lottie

- The three full-player transport keys now resemble backlit transistor-radio controls: fixed dark sockets, smoked translucent plastic, beveled reflective rims, molded ribs and inset amber lamp windows. Presses move the caps inward; Previous/Next illuminate briefly and cool back down, while the center lamp stays on during actual playback and fades off when paused. The mini-player retains its compact controls.
- Key illumination uses a 100 ms warm-up, a short click hold and a 420 ms fade, with spring travel and return. Colors/materials stay scoped to player hardware. Existing native actions, disabled states and source/queue behavior remain intact. Folded local playback and steady lit/unlit states were inspected; a short press/motion recording is `verification/radio-keys-motion.mp4`.
- Recording frames confirmed momentary amber illumination on both Previous and Next, followed by the unlit cap state. Final folded/unfolded layouts and 150% button text were inspected, with the original font scale restored. Lit legends were darkened for readability. Android build, formatter check and all 20 shared tests pass. Captures: `verification/radio-keys-lit.png`, `radio-keys-unlit.png`, `radio-keys-large-text.png` and `radio-keys-wide.png`.

- The cassette handwriting area now displays the current station or song title. Long titles scroll right to left after 1.4 seconds, with a pause between loops; short titles stay still. The proportional paper overlay preserves the original logo/side marks, and the underlying Lottie is unchanged. Full metadata below remains scalable and accessible.
- Label verification: a real SomaFM title visibly moved left across successive captures; the fitting local title "From The Start" remained pixel-identical. With reduced motion enabled, the long label was pixel-identical. Folded/unfolded layouts and logo clearance were inspected; the system animation setting was restored. Captures: `verification/cassette-label-long-{a,b}.png`, `cassette-label-song-{a,b}.png`, `cassette-label-reduced-{a,b}.png` and `cassette-label-folded.png`.

- Archive Dark is the app default regardless of the device theme. All existing screens share graphite surfaces, cream primary text, taupe metadata and restrained copper/sage accents. Native Android startup and status/navigation bars also use dark appearance. The existing light palette remains available to explicit theme callers.
- Now Playing uses the supplied blackened-metal cassette artwork, editorial heading, scalable live metadata, brushed deck panels, amber analog needle meters and mechanical transport keys. Radio retains an ON AIR state without a fake timeline. Compact local playback prioritizes its real seek bar and visible transport controls; wide local layouts retain the decorative meters.
- `ReadyO_Cassette_Tape_Animated.json` is copied unchanged into shared Compose resources and rendered by Compottie Lite 2.3.3. Both hubs follow the retained playback clock. The mini-player uses the same artwork, with its next-station/track action and tap/swipe expansion preserved. The animation and all embedded images are bundled for offline use; the base cassette image provides a static loading/failure fallback.
- SHA-256 of the original and bundled JSON: `4A64ABE825F1CC08E6385E176CD5E97A7637D56B2FAD9EE75D5FA4A9A5BA9859`.
- Verified on the Pixel 9 Pro Fold in device light mode: dark home/search/library, folded and unfolded radio playback, local playback and actual duration, 150% text and scroll access to enlarged controls. Two playing captures differed in both hub regions; two paused captures were pixel-identical in the cassette and meters. With system animation scale zero, the cassette was pixel-identical while the native session remained PLAYING. Original text and animation settings were restored.
- Final installed-build checks also verified native seeking through the thin local scrubber, full visibility of compact local transport controls, mini-player next-track advancement and swipe-down/up transitions. Android build, formatter check and all 15 shared tests pass. iOS runtime remains unverified on Windows. Visual captures are under `verification/archive-dark-*.png`; animation evidence is `verification/lottie-{playing,paused,reduced}-{a,b}.png`.

Integration references: [Compottie](https://github.com/alexzhirkevich/compottie), [shared resource setup](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html).

## Milestones
- [x] M1 Android slice: compile shared/Android app; real stream and imported file playback; mini-player and cassette Now Playing.
- [ ] M1 iOS: native playback adapter, Files import and Xcode host project; compile and verify on macOS.
- [ ] M2: directory discovery/search, caching, favorites and history.
- [ ] M3: MediaStore library, iOS import, collections and editable queue.
- [ ] M4: interruption/lifecycle tests, accessibility, reduced motion, foldable and dark theme polish.

Start with a real playable vertical slice. Android file import is an initial path; MediaStore indexing follows. iOS cannot be built or runtime-tested on Windows. Do not claim a release-ready MVP from the first slice.

## Verified first increment

- Android debug APK builds; two common directory parsing tests pass on Android host JVM.
- Installed on `ReadyO_Foldable_API_36`, Pixel 9 Pro Fold / Android 16.
- Radio Browser returned real stations; Radio Armisa reached native `PLAYING` state.
- Folding/unfolding preserved live playback; unfolded player uses two columns.
- System media controls exposed the station title and pause action.
- Imported a generated 12-second WAV using Android's document picker; native session reached `PLAYING` with the imported title and 12-second duration. Switching replaced the radio session.
- App-managed copy remained after force-stop/relaunch and removal of the source file from Downloads.
- Fixed a runtime crash caused by nested vertical scroll containers in the narrow player.

## Remaining work / limits

This is not the complete MVP. Saved stations/history/cache, album/artist collection views, queue editing, shuffle/repeat, artwork loading, playback restoration and final visual polish remain. Android now has MediaStore discovery and linked folder scanning. iOS has targets and the shared view-controller entry point only; no native iOS player/importer or runnable Xcode host yet. Lock-screen, interruptions, Bluetooth, broad codec compatibility and airplane-mode tests remain unverified.

## Notification stop and local music sources

- Closing ReadyO from Recent Apps preserves active playback. The media notification has an explicit **Stop and dismiss** action that stops audio, clears its queue/metadata, removes the notification and clears the in-app mini-player/Now Playing.
- Android can hide a media carousel card on swipe without notifying the app or cancelling its notification, including on this emulator. The explicit stop action is the reliable close control. Actual Media3 notification-dismissal events and system Stop commands also clear playback. Pausing remains resumable and drops foreground-service status immediately.
- Android My Music offers **Choose music folder**, **Scan device music**, **Import individual files**, **Rescan** and per-folder **Unlink**. Linked folders include nested folders and play directly through persisted read grants. Unlinking does not delete music. Folder names/links and the device-scan choice survive relaunch.
- Device scans use MediaStore and version-appropriate audio permission only. Denied/revoked audio access displays a recovery message; explicitly linked folders continue working through their scoped grant. No all-files permission is requested.
- Folder/device results are merged by storage identity to avoid indexing the same file twice. Distinct files with the same title remain distinct. Existing individual imports and their index remain compatible.
- Runtime checks on the Pixel 9 Pro Fold: device scan found real indexed tracks; a generated folder and subfolder were linked; their two tracks appeared once with device scanning also enabled; access survived app restart; revoked device permission still allowed linked-folder playback; Rescan found a newly added third file; Unlink removed the source without deleting files. Temporary test music and links were removed, and the original granted audio permission was restored.
- Native local playback continued after dismissing ReadyO from Recent Apps. Pressing **Stop and dismiss** yielded an empty native playback state, no ReadyO notification, and no in-app mini-player. Media3's dismissal intent also cleared the selected item. Android's carousel swipe alone is not claimed to stop active audio.
- Android build and formatter check pass; all **15 shared tests** pass. iOS source capabilities remain disabled until native adapters exist. Existing toolchain warnings are unchanged.

Sources used for Android integration: [MediaSessionService](https://developer.android.com/media/media3/session/background-playback), [MediaStore audio](https://developer.android.com/training/data-storage/shared/media), [persistent folder access](https://developer.android.com/training/data-storage/shared/documents-files), [Media3 dismissal events](https://github.com/androidx/media/blob/1.9.0/libraries/session/src/main/java/androidx/media3/session/MediaNotification.java).

## Refined collapsed player

- Reworked the mini-player into an inset warm-paper strip with an animated cassette, drag handle, source/status label, clearer station/track title, and separate 48 dp play/pause and next controls. Buttons have a spring press response; local tracks have a thin progress line.
- Next station cycles through the selected radio results without expanding Now Playing. Radio context stays in the service-owned media metadata so navigation still works after the Activity is recreated during background playback. Local next follows the native queue and disables at its end.
- Narrow layouts and enlarged text prioritize metadata and controls, hiding the decorative cassette when needed. Titles wrap to two lines; secondary details truncate instead of colliding with transport controls.
- Verified folded/unfolded layouts, 150% text, real station changes from the collapsed player, swipe-up expansion, swipe-down collapse, next station after reopening the Activity, and local next advancing the native queue while staying collapsed. Text scale and the unfolded display were restored, and test audio was paused. Android build, formatter check and all 15 shared tests pass. Captures: `verification/mini-player-wide.png`, `verification/mini-player-folded.png`, `verification/mini-player-large-text.png`, `verification/mini-player-local.png`.

## Animated player increment

- Shared `feature/player/ui/motion/DeckMotion.kt` retains the animation phase across pause/resume and player expand/collapse. Animation values are read in canvas/layer drawing to avoid recomposing the screen every frame.
- Independent reels, scrolling tape threads, drifting reflection, breathing LED, subtle deck sway and decorative simulated VU bars run during actual `Playing` state. They freeze during buffering, pause, end and error. VU bars are explicitly labeled as decorative, not measured audio.
- Android's Compose motion-duration scale disables the continuous clock, and window focus loss suspends it. Emulator screenshots confirmed a moving cassette during playback, identical cassette pixels while paused, and identical cassette pixels while playing with system animation scale set to zero. The original emulator setting was restored after the test.
- Mini-player swipes up to expand; the full player swipes down to dismiss. Short drags settle back with a spring; intentional flings commit. Child scrolling hands downward overscroll to the dismiss gesture only at its top. The library stays composed behind the player so its scroll position survives.
- Added slide/fade player transitions, receding library, spring-loaded transport keys, sliding metadata labels and an animated cassette in the mini-player.
- Swipe open/close verified on folded and unfolded Pixel 9 Pro Fold layouts. Four host tests pass (two directory, two gesture-decision tests). Android debug build succeeds; iOS motion behavior remains unverified.

Player components and motion now live under `feature/player/ui`; theme tokens live under `core/designsystem/theme`. The earlier animation preview is `verification/readyo-motion.mp4`.

## UI and architecture cleanup

- Replaced mixed screen/network/file/player code with feature-first MVVM and UDF. Screens render state and emit actions; lifecycle-owned ViewModels coordinate work through repository/player contracts.
- Moved import/index logic out of MainActivity; the native library repository serializes mutations and writes the metadata index atomically. Existing imported files and metadata remain compatible.
- Extracted app navigation, feature contracts, ViewModels, screens, drawing, motion, typography, shared rows, theme and native adapters into focused files. Removed compressed statement chains and added explicit imports, descriptive state names, four-space formatting and `tools/format-kotlin.ps1`.
- Removed the printed rules crossing the cassette title; moved all decorative printing into proportional artwork layout; essential metadata remains normal scalable text. Corrected heading line heights, header wrapping, mini-player spacing, seek handling and warm dark-mode surfaces.
- Verified the library and player on folded/unfolded displays, normal/150% text, and light/dark themes. Rechecked local playback, tab/search navigation and player swipes after the refactor. Original font-size and night-mode settings were restored.
- Android debug build succeeds; all 12 directory/gesture/ViewModel tests pass; formatter check passes.
- `lintDebug` completes and reports dependency/target-SDK warnings. It also logs an upstream library-path resolution warning because the workspace name includes `!`; do not treat that run as exhaustive static analysis. Existing AGP 9.2.0 / compile SDK 37.2 warning remains. These do not prevent building or running the app.
- Final layout captures: `verification/cleanup-player-folded.png`, `verification/cleanup-player-large-wide.png`, `verification/cleanup-player-large-dark-folded.png`.

The build uses Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.2.0 and Gradle 9.4.1. Compose requires API 37 compilation; the locally installed SDK is 37.2. AGP warns its tested maximum is 37.0, although the build succeeds. Target SDK stays 36. Revisit the AGP/SDK pairing before release.

## Build

Use JDK 21 and the installed SDK. On this Windows setup, set `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\AndroidDev-NoUnixSockets` for the packaged-app Java networking workaround. Run:

```powershell
.\gradlew.bat :androidApp:assembleDebug :shared:testAndroidHostTest
```

Debug artifact: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

## References
- https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html
- https://developer.android.com/kotlin/multiplatform/plugin
- https://developer.android.com/media/media3/session/background-playback
- https://docs.radio-browser.info/
