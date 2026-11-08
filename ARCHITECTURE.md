# ReadyO architecture

ReadyO uses feature-first MVVM with unidirectional data flow. Shared Compose screens render immutable state and send actions upward. ViewModels coordinate asynchronous work and publish read-only `StateFlow` values. Repositories own data access; the platform audio service owns playback resources.

## Source map

```text
shared/src/commonMain/kotlin/app/readyo/
  app/                         App composition, typed destinations, dependency wiring
  core/
    model/                     AudioItem
    playback/                  PlayerController and playback state
    designsystem/
      theme/                   Colors, dimensions, spacing, shared Material theme
      component/               Headers, artwork fallback, rows, feedback states
  feature/
    radio/
      domain/                  Directory repository contract
      data/                    Radio Browser client and response mapping
      presentation/            RadioUiState, RadioAction, RadioViewModel
      ui/                      Home, search, category cards, station rows
    music/
      domain/                  Library contract, directories, folder membership and scan merging
      presentation/            MusicLibraryUiState, actions, ViewModel
      ui/                      Library and track rows
    player/
      presentation/            PlayerUiState, PlayerAction, PlayerViewModel
      ui/                      Mini-player, Now Playing, metadata and transport
        component/             Bundled Lottie cassette, brushed panels, VU and tactile keys
        motion/                Animation clock and sheet gesture mechanics

androidApp/src/main/kotlin/app/readyo/
  MainActivity.kt              OS picker registration and shared UI entry point
  app/                        Activity-retained Android dependency container
  core/playback/              Media3 controller adapter and MediaSessionService
  feature/music/data/         MediaStore scanner, SAF folder scanner, source grants/index and imports
```

Tests follow the same feature/package structure in `shared/src/commonTest`.

Shared offline artwork lives in `shared/src/commonMain/composeResources`. `files/cassette_transport.json` is the user's original Lottie; `drawable/cassette_still.png` is its unchanged base image for loading/failure fallback. Android resource packaging is explicitly enabled in the shared target.

## State and actions

```text
User input → Screen callback / typed Action → ViewModel
                                            ↓
                                     Repository / PlayerController
                                            ↓
                               immutable StateFlow → Screen
```

- `RadioViewModel` owns query, selected filter, loading, results and recoverable directory errors. A new query cancels the previous job; cancellation never clears a newer request's loading state.
- `MusicLibraryViewModel` maps repository snapshots into screen state and dispatches imports, source changes and rescans. The Android repository serializes index/source mutations and writes metadata atomically. Folder/permission picker results are platform effects routed back to the ViewModel. Separate native scanners own MediaStore queries and scoped folder traversal; domain merging removes duplicate file identities.
- Music browsing selection belongs to `MusicLibraryViewModel`: All songs, folder list, or one selected folder. It publishes `visibleTracks` separately from the full library. The app passes that visible list into playback, so Play all and Next stay in the chosen folder. Back from a folder returns to the folder list before leaving My Music. Expanding the source-management panel is transient UI state.
- Scanners attach a `MusicDirectory` to each indexed file. Device and linked-folder scans normalize shared-storage directory identities consistently. Folder membership resolves to the canonical merged track ID, preventing duplicate or empty folder filters when sources overlap. Imported copies form the explicitly named Imported files collection. Different physical directories stay separate even when their names match.
- `PlayerViewModel` owns expanded/collapsed presentation and translates transport actions into the single audio controller. Opening or dismissing the sheet does not restart playback.
- The playback state exposes next availability. Radio keeps one live item in the native queue and preserves its station selection context in media metadata; local tracks use the native queue. Compact transport controls only emit player actions.
- `SessionPlayer` gives system Stop a clear meaning: stop and clear the queue. Pause remains resumable. The service handles Media3 dismissal events and the notification's explicit Stop and dismiss command; the native adapter publishes empty state when its playlist is cleared. Activity closure is independent of these stop events.
- `AppViewModel` owns typed root/detail destinations. It does not perform networking or file access.
- `ReadyOApp` is the route/composition boundary: obtain lifecycle-owned ViewModels, collect state, connect feature callbacks and render screens. Feature screen functions do not receive repositories or native controllers.

## Lifetimes and dependencies

The Android dependency container is retained by the Activity's ViewModel store. Shared feature ViewModels use that same store because both bottom tabs and the shared playback layer are app-level experiences. The container releases its client/controller when cleared; the service independently owns and releases ExoPlayer and MediaSession. UI collection follows lifecycle state.

Use constructor injection and small interfaces. Keep platform APIs in the platform host/data layer. Add use-case classes when behavior is reused or substantial; avoid empty forwarding layers, generic base ViewModels and separate modules for every screen.

## UI and motion rules

Archive Dark is now the default, including on devices in light mode, as explicitly requested by the user. Use `ReadyOColors.ArchiveDark` through `ReadyOTheme` and Material semantic colors. `PaperLight` remains available through an explicit theme argument; no theme settings screen is added. Keep physical cassette materials in `ReadyOCassettePalette` and scoped player components.

Essential text scales with system font size and wraps in normal layout. The supplied cassette's baked printing is decorative and excluded from accessibility; actual station/track metadata is accessible text outside it. Compact local playback prioritizes its real timeline and controls over decorative meters.

`CassetteTitleLabel` duplicates the current title in the cassette's handwriting area. Its bounds and type size scale with the artwork, preserving the logo, side markings and printed rule. It samples blank paper from the existing cassette asset, leaving the original Lottie untouched. Overflow uses a leftward marquee with a short initial/repeat pause; fitting titles remain static. Reduced motion and window focus govern scrolling. The full scalable title below the artwork remains the accessibility source.

Animation offsets, pressed state, scroll positions and frame phases are local UI state. Playback, search and library state belong to ViewModels/repositories. Canvas animation reads phase during drawing, not whole-screen recomposition. Motion remains tied to actual playback, window focus and system duration scale.

`CassetteReelAnimation` renders the eight-second Lottie through Compottie Lite, driven by `DeckMotion` rather than an independent repeating animator. Pause, buffering, error, focus loss and reduced motion therefore retain a still frame; opening or closing Now Playing does not reset the hubs. Analog VU needles use the same clock and are explicitly described as decorative.

Transport keys keep their interaction animation local to `TransportKey`. A press or click briefly lights the translucent cap; the center lamp also follows the actual Playing state. `TranslucentKeyCap` draws the smoked plastic, bevel, ribs and inset amber window from the cassette material palette. The socket stays fixed while the cap travels. Standard Compose animation respects the system motion scale, and the keys emit the existing player actions without owning playback state.

## Checks

```powershell
.\gradlew.bat :androidApp:assembleDebug :shared:testAndroidHostTest
.\tools\format-kotlin.ps1 -Check
```

Run `format-kotlin.ps1` without `-Check` to format source/build files using ktfmt's four-space Kotlin style. JDK 21 is required; the formatter is cached in the OS temporary directory, outside the project because its name contains `!`.

Before finishing layout changes, inspect folded and unfolded screens, enlarged system text, search/error/empty states, and swipe/transport behavior. iOS runtime validation requires macOS/Xcode; the iOS native player and importer are still pending.

Architecture references: [Android UI layer](https://developer.android.com/topic/architecture/ui-layer), [multiplatform ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html).

## Daily Mix and browsing persistence

Music genres and folder hierarchy are derived in MusicLibraryViewModel. MusicLibraryScreen owns saveable scroll state per browse location. AndroidMusicLibraryRepository owns the versioned scan cache and genre migration for older imports.

DailyMixViewModel exposes DailyMixRepository state and refresh actions. AndroidDailyMixRepository combines the cached library with device-local listening statistics and a persisted daily selection. ListeningTracker belongs to PlaybackService so listening continues to be counted while the Activity is closed. It uses elapsed audible time rather than seek position. Refreshing a mix only changes future selection, and PlayerViewModel starts the saved queue when Play mix is pressed.

AppViewModel reads and writes AppTabStore through platform dependency injection. RecentStationStore records stations once audio actually starts and publishes preference changes to RadioViewModel.
