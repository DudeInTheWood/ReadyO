# ReadyO — Phase 1 Product Requirements & Implementation Brief

**Status:** Implementation-ready MVP specification  
**Platforms:** Android and iOS  
**App framework:** Compose Multiplatform (CMP) — shared Android + iOS UI and application code  
**Core promise:** A simple, reliable internet-radio and offline local-music player with excellent default playback behavior and no user configuration.

> **For the coding agent:** Read this entire file before changing code. First inspect the repository, identify what already exists, and produce a short implementation plan mapped to the existing project. Then begin implementing vertical slices, starting with playback and one working end-to-end flow. Do not replace existing working architecture without a concrete reason. If there is no repo yet, scaffold a **Compose Multiplatform app** using the official CMP Android + iOS project structure. KMP is the underlying compilation mechanism, not a request for separate native Android and SwiftUI frontends. Make sensible choices for routine details and document assumptions; only ask about genuinely blocking decisions.

## 1. Product definition

ReadyO has two equal primary features:

1. **Radio:** Discover and play *internet streams of radio stations*, including FM stations that broadcast online. This is **not** a hardware FM-tuner app. Radio requires internet access.
2. **My Music:** Discover or import locally accessible, user-owned audio files and play them offline with strong native playback support.

The app is lightweight, listening-first, account-free, and ad-free in Phase 1. Users should be able to open the app, find a station or a song in a couple of taps, lock their phone, and keep listening reliably.

### Product principles

- **Zero configuration:** No Settings screen, EQ, DSP, bitrate selector, output-device selector, or mandatory onboarding in Phase 1.
- **One playback session:** One active audio source at a time; starting a local song stops radio, and starting radio stops local music.
- **Native-quality playback:** Use native platform decoders/players and preserve source quality. Do not transcode, normalize, or modify sound by default.
- **Offline-first for local music:** The local library must work without any internet connection.
- **Graceful failures:** Display understandable states for missing permissions, failed streams, unsupported/corrupt files, and unavailable sources.
- **Consistent interface:** Share screen presentation and state management where practical, but integrate native OS audio capabilities properly.

## 2. Navigation and screen inventory

Exactly **two bottom navigation destinations**: **Radio** and **My Music**. A persistent mini-player sits above the bottom navigation while a playback session is present. Tapping it opens the shared full-screen Now Playing view.

**Six primary functional screens:**

| ID | Screen | Entry | Responsibility |
|---|---|---|---|
| R1 | Radio Home | Radio bottom tab | Continue listening, discover stations, browse categories |
| R2 | Radio Search / Browse | Search/category from Radio Home | Search stations and filter by country, genre, language |
| R3 | Saved Stations | Radio Home | Favorite stations and recent stations |
| M1 | My Music Library | My Music bottom tab | Library categories, recently played music, search |
| M2 | Music Collection | From library/category/search | Show songs, album tracks, artist tracks, or folder contents |
| M3 | Play Queue | From music player | View, reorder, and remove upcoming tracks |

**Shared overlays/components (not counted in the six):**

- **Now Playing:** Full-screen player with radio and local-music modes.
- **Mini-player:** Persistent playback summary on either root tab.
- **Dialogs / bottom sheets:** File-access explanation, errors, track overflow actions, optional station details. Do not create separate destination screens unless necessary.

### Navigation tree

```text
ReadyO
├── Radio [bottom tab]
│   ├── R1 Radio Home
│   ├── R2 Search / Browse
│   └── R3 Saved Stations (Favorites / Recently Played)
├── My Music [bottom tab]
│   ├── M1 Library
│   ├── M2 Collection (Songs / Albums / Artists / Folders / Search Results)
│   └── M3 Play Queue
└── Shared playback layer
    ├── Mini-player
    └── Now Playing (Radio state OR Music state)
```

## 3. Screen-by-screen UI and behavior

### R1 — Radio Home

```text
┌─────────────────────────────────┐
│ ReadyO                     🔍  │
│ Listen to the world.            │
│                                 │
│ CONTINUE LISTENING              │
│ [station logo] Last station  ▶  │
│                                 │
│ Explore stations                │
│ [Thailand]      [Worldwide]     │
│ [Pop]           [Jazz]          │
│                                 │
│ Popular stations       See all  │
│ [logo] Station name        ▶    │
│ [logo] Station name        ▶    │
│                                 │
│ ┌ Mini-player (when active) ┐   │
│ │ Name / live info       ⏯  │   │
│ └───────────────────────────┘   │
│     Radio       My Music        │
└─────────────────────────────────┘
```

**Required:** Continue listening to last successfully played station; discover stations by country/tag; show popular stations; tap to play; navigate to search and saved stations. Directory data must have loading, empty, and failure states. Never present fabricated station entries as real content.

### R2 — Radio Search / Browse

```text
┌─────────────────────────────────┐
│ ← Find a station                │
│ [Search station name...]        │
│ [All] [Country] [Genre] [Lang]  │
│                                 │
│ Results                         │
│ [logo] Station       ♡     ▶    │
│ [logo] Station       ♡     ▶    │
└─────────────────────────────────┘
```

**Required:** Debounced station-name search; browsable country/genre/language filters; results show station name, optional logo, country/genre as available; favorite/unfavorite; tap to play. Avoid showing unavailable metadata as if known. Handle duplicate or dead stream entries gracefully.

### R3 — Saved Stations

```text
┌─────────────────────────────────┐
│ ← Your stations                 │
│ [Favorites] [Recently played]   │
│ [logo] Station           ♥  ▶   │
│ [logo] Station           ♥  ▶   │
└─────────────────────────────────┘
```

**Required:** Persist favorites and a deduplicated, newest-first recent-stations history locally. Tapping a station plays it; favorite toggle works without an account. Each section has its own empty state.

### M1 — My Music Library

```text
┌─────────────────────────────────┐
│ My Music                    🔍  │
│ Your music. Offline.            │
│                                 │
│ [Songs]     [Albums]            │
│ [Artists]   [Folders]           │
│                                 │
│ Recently played          See all│
│ [art] Track 1               ⋮   │
│ [art] Track 2               ⋮   │
│                                 │
│ ┌ Mini-player (when active) ┐   │
│ └───────────────────────────┘   │
│     Radio       My Music        │
└─────────────────────────────────┘
```

**Required:** Browse local Songs, Albums, Artists, Folders; search local tracks; show embedded artwork and song/artist/album metadata when available, with tasteful fallbacks; recently played tracks. Show an empty-library state with a clear platform-appropriate action.

**Platform access differences:**

- **Android:** Query accessible media using `MediaStore`; request only necessary modern audio permissions (`READ_MEDIA_AUDIO` on applicable Android versions), respecting version-specific behavior. Do not request broad all-files access.
- **iOS:** Do **not** assume arbitrary device-wide filesystem scanning. Provide user-initiated import via Files/document picker for compatible audio. Copy imported files into an app-managed local library (or use a safely managed persistent-access strategy if explicitly supported), so playback survives app relaunch and the original external file moving or becoming unavailable. Clearly account for storage use. No Apple Music subscription/catalog integration in Phase 1.

### M2 — Music Collection

```text
┌─────────────────────────────────┐
│ ← Songs                     🔍  │
│ 128 tracks                  Sort │
│ [▶ Play all]     [⤨ Shuffle]    │
│ [art] Track name            ⋮   │
│ [art] Track name            ⋮   │
│ [art] Track name            ⋮   │
└─────────────────────────────────┘
```

**Required:** One reusable collection UI for songs, album tracks, artist tracks, folders, and local search results. Play all; shuffle all; tap individual song; overflow actions **Play next** and **Add to queue**. Support stable predictable ordering; use simple sensible default sorting. Album and artist lists can be rendered within this screen pattern rather than creating more independent navigation destinations.

### M3 — Play Queue

```text
┌─────────────────────────────────┐
│ ← Up next                       │
│ Now playing                     │
│ [art] Current track         🔊  │
│                                 │
│ Playing next                    │
│ [art] Track 2                 ☷ │
│ [art] Track 3                 ☷ │
│ [art] Track 4                 ☷ │
│ Drag to reorder • Swipe remove  │
└─────────────────────────────────┘
```

**Required:** Show current local song and queued songs; drag to reorder upcoming items and remove items; queue changes must be reflected in actual playback. Radio does **not** show a music queue.

### Shared Now Playing

**Radio state:**

```text
[⌄]           LIVE RADIO       [⋯]

        [Station artwork/logo]

             Station name
        Artist — Song title*
                LIVE

   [♥]    [Previous] [⏯] [Next]  [Share]
               (stations)
```

**Music state:**

```text
[⌄]           MY MUSIC        [⋯]

          [Album artwork]

             Track name
           Artist — Album

         ━━━━━●──────────
         1:24          3:42

   [Shuffle] [Previous] [⏯] [Next] [Repeat]

            [♥*]    [Queue]
```

`*` Radio track metadata is best-effort and only displayed when provided by the stream or reliable service; fall back to station information. Music favorites can be omitted from Phase 1 if a supported favorites/library model is not implemented—**do not show an inert heart button**. Similarly, show Share only when a working native share action exists. Local audio timeline is seekable; live radio should not show a fake seek bar. Radio skip actions navigate previous/next station in the current browse/listening context, not tracks within a live stream.

**Mini-player:** Show artwork/placeholder, title, secondary info, play/pause; tap outside the button to expand. Keep visible when navigating between Radio and My Music. No mini-player when nothing has been selected and no restorable session exists.

## 4. Functional requirements

### F1 — Radio data and streaming

- Use the **Radio Browser API** for station directory/discovery. Review its current documentation and endpoint availability before integration.
- Support station discovery by search and country/tag/language, plus popular stations when the API supports a meaningful ranking.
- Maintain station ID, display name, URL/stream URL, icon, country, language, tags, codec where supplied; treat codec and metadata as advisory.
- Stream playback over the network; handle buffering, errors, station timeout and recovery without freezing the UI.
- A failed station must surface a retry action and allow easy selection of another station. Do not silently hop to a different station unless the user explicitly requested next station.
- Station/song title metadata should be **best-effort**, not a blocker for the MVP. No speech-to-text or song-recognition fallback in Phase 1.
- Persist favorite station IDs and recently played station references with sufficient cached display data to render saved screens offline. Playing them still requires network.
- Cache directory responses sensibly and don't repeatedly hammer public APIs. Handle provider outages with stale cache and error information when possible.

### F2 — Local music library

- Index or import playable local audio via platform-appropriate methods.
- Read available title, artist, album, duration, track/disc numbers and embedded art. Use fallbacks for missing/invalid metadata.
- Support common formats such as MP3, AAC/M4A and FLAC **where platform-native decoding allows**. Don't promise unsupported codecs or universal bit-perfect output.
- Library supports Songs, Albums, Artists, Folders and search; Android folders can be grouped by relative path. On iOS, imported folder metadata may be preserved during import when feasible; otherwise show the imported collections appropriately and do not fake directory hierarchy.
- Support play, pause, seek, previous, next, play all, shuffle, repeat off/all/one, queue ordering and queue operations.
- Resume last local track and playback position after app relaunch where source is still accessible; avoid automatic unsolicited audio playback on launch.
- **No EQ, DSP, volume normalization, crossfade, ReplayGain or configuration UI in Phase 1.** Try for gapless local playback when supported by native player and audio format; test it and don't claim it unconditionally across platforms.

### F3 — Playback and OS integration

- Central playback coordinator with a single active playback source (`None | Radio | LocalMusic`).
- Switching source stops prior source cleanly, updates metadata, and avoids overlapping audio.
- Support playback in background, screen locked, and app lifecycle changes.
- Expose native now-playing metadata and transport controls to lock screen, notification (where applicable), headphones and Bluetooth devices.
- Handle audio focus/interruption and noisy audio output events (e.g. unplugged headphones) according to OS recommendations; do not unexpectedly blast music through speakers.
- For radio, distinguish `connecting`, `buffering`, `playing`, `paused`, `error`; for local files include `loading`, `playing`, `paused`, `ended`, `error`.
- No repeated playback sessions, notification duplication or stuck foreground services.

### F4 — Storage, privacy and performance

- No account, ads, analytics dependency, or cloud-sync requirement.
- Persist favorites, listening history, imported-file metadata and playback restoration state locally.
- Ask for access only when needed; handle denied permission and empty library without crashing.
- Prefer incremental/asynchronous indexing and cancelable searches; don't block main/UI thread.
- Keep UI responsive for libraries with thousands of songs, and use thumbnail artwork loading with bounded caching.
- Avoid logging file paths, stream URLs with secrets, or identifying information in production logs.

## 5. Technical approach

### Selected technology stack (CMP-first)

| Area | Choice | Notes |
|---|---|---|
| App framework / UI | **Compose Multiplatform (CMP)** | **Required:** Share actual screen composables across Android and iOS in `commonMain`. Do **not** build Jetpack Compose Android + a separate SwiftUI interface. |
| Language / build | Kotlin, Gradle Kotlin DSL, Kotlin Multiplatform plugin (as required by CMP) | Use the official CMP template and compatible stable toolchain versions. Underlying KMP tooling is expected; **CMP is the product/framework choice**. |
| Design components | Compose Material 3 | One shared theme, responsive layout, reusable mini-player and Now Playing composables. |
| Navigation | JetBrains Compose Multiplatform Navigation (or existing equivalent) | Two root tabs; shared navigation state. Avoid competing navigation frameworks. |
| Async / state | Kotlin coroutines, Flow, StateFlow | Shared UI state holders; lifecycle-aware collection on Android and appropriate iOS lifecycle integration. |
| Networking | Ktor Client + Kotlinx Serialization | Radio Browser API; platform engine as appropriate; timeouts, errors and light cache. |
| Images | Coil 3 with Compose Multiplatform support (if compatible) | Station icons and local embedded art; placeholders and bounded caching. |
| Local persistence | SQLDelight **if** relational persistence is warranted; otherwise lightweight multiplatform storage | Favorites, recent plays, library index and restored session. Avoid storing large audio blobs in a DB. |
| Android audio | Media3 ExoPlayer + MediaSessionService | Native audio engine behind a shared Kotlin interface; foreground/background playback and system transport controls. |
| Android files | MediaStore | Local music discovery; scoped media permission handling. |
| iOS audio | AVPlayer + AVAudioSession + MPNowPlayingInfoCenter / MPRemoteCommandCenter | Native audio behind the same shared interface, including background audio configuration. |
| iOS files | Native document picker / Files importer | Import to app-managed local storage; no promise of unrestricted device music-library access. |
| Tests | `kotlin.test` for shared logic, platform tests for playback adapters, Compose UI tests where supported | Prioritize reliable player transitions and queue logic. |

**CMP implementation constraint:** Screens, navigation, reusable UI components, state holders and domain logic belong in shared CMP source sets. Platform-specific source sets contain only OS-bound functionality (audio engines, permissions, media scanning/import, notifications/remote controls). Prefer `expect/actual` or injected interfaces for these seams; do not create two separate UIs.

**CLI / agent workflow:** The development CLI is **already installed**. Before adding installation steps or selecting a CLI command, inspect which executable and subcommands are actually available in the environment (`--help` / version). Use the existing CLI to assist with repo exploration, code changes, build/test loops and verification when supported; do not invent flags or assume it replaces Gradle, Xcode, Media3, or iOS native APIs. Use Gradle tasks for Android/shared code, and a macOS/Xcode-capable environment for iOS compilation. Do not block Android progress when iOS tooling is unavailable; document unverified iOS work.

Use existing repository conventions where reasonable. **Preferred architecture:**

```text
shared/
  commonMain/
    model/            RadioStation, LocalTrack, Album, Artist, QueueItem
    domain/           PlayRadio, PlayTrack, SearchStations, LoadLibrary...
    repository/       RadioRepository, MusicLibraryRepository, FavoritesRepository
    playback/         PlayerController contract, PlayerCoordinator, PlayerState
    presentation/     ViewModels/StateHolders, UiState, Compose screens/components
  androidMain/
    playback/         Media3 ExoPlayer + MediaSession/Service integration
    library/          MediaStore and permission handling
    persistence/      Platform storage implementations if needed
  iosMain/
    playback/         AVPlayer, AVAudioSession, Now Playing/remote commands
    library/          Files import + persisted app-managed file library
```

Recommended conceptual contracts (adapt naming to the project):

```kotlin
sealed interface AudioSource {
    data class Radio(val stationId: String) : AudioSource
    data class LocalMusic(val trackId: String) : AudioSource
}

sealed interface PlaybackStatus {
    data object Idle : PlaybackStatus
    data object Connecting : PlaybackStatus
    data object Buffering : PlaybackStatus
    data object Playing : PlaybackStatus
    data object Paused : PlaybackStatus
    data class Error(val message: String, val canRetry: Boolean) : PlaybackStatus
}

interface PlayerController {
    val state: kotlinx.coroutines.flow.StateFlow<PlayerState>
    suspend fun playRadio(station: RadioStation)
    suspend fun playTracks(tracks: List<LocalTrack>, startIndex: Int)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun setShuffle(enabled: Boolean)
    fun setRepeat(mode: RepeatMode)
    // Add queue operations and close/release lifecycle as needed.
}
```

**Important:** Radio and music may use distinct native playback resources internally, but expose **one coherent session** and one consistent observable state. On Android favor Media3 ExoPlayer with MediaSession/MediaSessionService; on iOS favor AVPlayer with AVAudioSession and native remote controls. Use Kotlin coroutines and StateFlow for shared reactive state. Choose lightweight persistence appropriate to existing code (e.g., SQLDelight for structured cross-platform data and settings storage for small preferences) rather than adding a large stack without need.

## 6. Visual design direction

- Modern, clean, music-centric, uncluttered.
- Clear hierarchy: cover/station art, title, context, large transport controls.
- Use Compose Material 3-style foundations as a starting point, but keep branding flexible.
- Bottom nav has exactly two entries; no third settings or library-only tab.
- Support system light/dark mode by default without a theme configuration screen.
- Provide loading skeletons/progress, empty states and errors for network and file-library experiences.
- Touch targets must be accessible and reasonably sized; icons require meaningful accessibility labels.

## 7. Explicitly out of scope — Phase 1

- Hardware FM tuner functionality.
- Authentication, accounts, cloud library sync, paid plans, advertising.
- Settings screen, manual audio quality selections, EQ, DSP, loudness boosts.
- Live lyrics, LRCLIB integration, STT, Shazam-style detection, automatic song identification.
- External song links to Spotify, Apple Music, YouTube Music, etc.
- Custom playlists or playlist import/export; music queue is in scope.
- Recording radio streams, downloading internet radio content, podcast management.
- Chromecast/AirPlay-specific feature UI, cross-device synchronization, advanced audio routing.
- Apple Music catalog or subscription streaming.

## 8. Acceptance criteria / definition of done

### Navigation / UI

- [ ] Bottom tabs only Radio and My Music; mini-player behaves consistently.
- [ ] All six screen types implemented with meaningful states and navigation.
- [ ] Shared Now Playing displays controls appropriate to active source, with no fake/inert controls.
- [ ] Both modes show loading, empty, and error states.

### Radio

- [ ] Can search and play a real, working internet station on both platforms.
- [ ] Can browse stations by supported discovery categories.
- [ ] Favorites and recent station history persist after relaunch.
- [ ] Buffering, unavailable URL, and network-loss errors are recoverable.
- [ ] Radio metadata is shown only when genuinely available.

### Local music

- [ ] Android library handles available MediaStore tracks and permission denial gracefully.
- [ ] iOS user can import supported files and play them after app relaunch.
- [ ] Local music still works in airplane mode.
- [ ] Songs, Albums, Artists, Folders and search have functional data paths.
- [ ] Play all, shuffle, repeat, seek and track skipping work.
- [ ] Queue reorder and remove affect actual playback.
- [ ] File format or corrupt-file errors do not crash the app.

### Playback reliability

- [ ] Only one source plays at a time during Radio ↔ Music switching.
- [ ] Background/locked-screen playback works with OS controls where supported.
- [ ] Audio interruptions and headphone disconnect are handled appropriately.
- [ ] Restoration of the last local track and seek position works without auto-playing.
- [ ] No uncaught UI errors or application-not-responding issues from large libraries/network failures.

## 9. Suggested implementation order

### Milestone 1 — Functional playback foundation

1. Inspect/scaffold the **Compose Multiplatform (CMP)** app; verify shared Compose UI and Android/iOS compilation paths.
2. Implement shared playback models, state, player coordinator and native adapters.
3. Build shared Now Playing and mini-player components.
4. Prove one local file plays end-to-end, and one remote audio URL streams, on each target.

### Milestone 2 — Radio

1. Add Radio Browser API client, models, cache, error handling.
2. Build R1 Radio Home and R2 Search/Browse with real data.
3. Build R3 Favorites/Recently Played persistence.
4. Validate background streaming, metadata and network recovery.

### Milestone 3 — Local music

1. Implement Android MediaStore and iOS Files import.
2. Build M1 Library and M2 Collection, artwork, metadata and search.
3. Implement queue, shuffle, repeat, resume and M3 Queue screen.
4. Verify offline playback and file-access persistence.

### Milestone 4 — Polish and test

1. OS playback integration, lock-screen metadata, headphones and Bluetooth controls.
2. Test large libraries, codec support, corrupted files and failed radio stations.
3. Resolve UI/UX issues, lifecycle problems and resource leaks.
4. Prepare release builds and document known platform-specific limitations.

**Milestones are an implementation guide, not permission to skip validation.** Use small vertical slices, working builds, and testable increments rather than building all screens with mocked data before connecting playback.

## 10. Instructions to the ChatGPT coding agent

**Start by performing these actions:**

1. Inspect the repository and summarize the existing modules, build system, dependencies, platform targets, and reusable code.
2. Compare the repository against the requirements above and identify gaps without discarding completed work.
3. Produce a brief file/module plan and milestone checklist, listing any reasonable assumptions.
4. Begin **Milestone 1 immediately**, implementing an actual playable vertical slice rather than only describing an architecture.
5. Build/test whatever platforms are available in the environment. Clearly mark what could not be run (especially iOS builds on non-macOS hosts).
6. After each increment, summarize **files changed**, **behavior verified**, **remaining issues**, and **next task**.
7. Avoid adding Phase 2 features or unnecessary configuration. If a nice-to-have conflicts with reliable basic playback, prioritize playback reliability.

**Decisions already made (do not re-ask):** **Compose Multiplatform (CMP) as the app framework and shared UI (not two native frontends)**; Kotlin/KMP only as CMP’s underlying toolchain; existing CLI installation; Android + iOS; internet radio and offline local music both in Phase 1; two bottom navigation tabs; no settings or audio enhancements; shared Now Playing and mini-player; platform-native playback integrations.

**Important implementation nuance:** Don't interpret the six screen count as six distinct navigation routes if reusable screens or transient overlays make the implementation cleaner. The count describes user-facing functional views. Similarly, do not implement fake native functionality simply to make the UI look complete.

---

**ReadyO Phase 1 success test:** A user launches the app, finds a working internet radio station or a local song within a few taps, presses play, locks the screen, and continues listening. Radio works reliably online; local music works reliably offline; switching modes never results in overlapping playback.
