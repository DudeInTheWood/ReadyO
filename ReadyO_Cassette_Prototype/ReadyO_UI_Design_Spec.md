# ReadyO — Phase 1 UI Design Specification

**Design name:** SIDE A — Cassette Edition  
**Spec version:** 3.0  
**Source of truth for product behavior:** [`ReadyO_Phase1_Requirements.md`](ReadyO_Phase1_Requirements.md)  
**Clickable visual reference:** [`ReadyO_UI_Prototype.html`](ReadyO_UI_Prototype.html)  
**Target:** Compose Multiplatform (CMP), shared Compose UI in `commonMain` on Android and iOS.

> **Coding-agent instruction:** Treat screen identifiers, route names, component names, and token names below as stable implementation contracts. The visual style has been updated from the previous direction. Keep **the same screen structure**, **the same route IDs**, and **the same reusable component names**. The HTML file is a visual prototype only; implement the actual product in shared CMP UI.

## Design rationale

**SIDE A** draws from 1970s–1990s portable cassette recorders and Japanese hi-fi packaging. Aesthetics take priority in `NowPlayingScreen`: the entire viewport feels like holding a real cassette and tape deck, with animated reels, a ribbon window, printed J-card labels, cross-head screws, a stripe marking, and raised mechanical transport buttons. All other screens are calmer, with stationery-paper backgrounds and record-shop labeling. **No cyberpunk, neon glow, futuristic dashboards, glassmorphism, or dark sci-fi gradients.**

Preserve the earlier flow and element placement; this version is a visual overhaul, not a new navigation system.

### Essential visual characteristics
- Main canvas: warm ivory / aged paper (`#EFE7D6`), not dark charcoal.
- Panels: shell-white and parchment, with warm-gray seams and subtle printed rules.
- Signature red-brown (`#B75035`) and olive (`#6C785D`) as vintage hardware/ink accents.
- Cassette plastics: off-white ABS, beige spindle hubs, dark brown translucent tape window, exposed reels, authentic-looking screws.
- Type: legible contemporary UI sans for interface text; Georgia-like printed serif for record labels, monospace for tiny industrial markings.
- Shapes: restrained small radii, squared tactile controls and label cards, minimal shadows resembling real physical material.
- Most elaborate visual treatment belongs to `NowPlayingScreen`; lists and search stay clean and usable.

## 1. Stable screen IDs and routes

| Requirements ID | Stable route | Compose screen | Function |
|---|---|---|---|
| R1 | `radio/home` | `RadioHomeScreen` | Hero resume, categories, popular stations |
| R2 | `radio/search` | `RadioSearchScreen` | Search, category filters and results |
| R3 | `radio/saved` | `SavedStationsScreen` | Favorites, recents |
| M1 | `music/library` | `MusicLibraryScreen` | Library categories, recent tracks |
| M2 | `music/collection/{kind}/{id?}` | `MusicCollectionScreen` | Songs / Albums / Artists / Folders and search results |
| M3 | `music/queue` | `PlayQueueScreen` | Actual queue editing |
| Shared overlay | `player/now-playing` | `NowPlayingScreen` | Radio or music mode, one player |

Bottom navigation destinations remain **exactly** `Radio` and `My Music`. `MiniPlayer` is a shared persistent element above bottom navigation. `NowPlayingScreen` is still the single fullscreen player for both radio and music.

## 2. Theme direction — full analog, no futuristic UI

Treat this like **a real cassette photographed as a user interface**, not a dark music app decorated with tape icons. Compose the cassette with layered vector/CMP shapes or illustration assets: `CassetteShell`, `CassetteLabel`, `TapeWindow`, `TapeRibbon`, `ReelLeft`, `ReelRight`, `TapeScrews`, `CassetteStripe`. Preserve exact track and station metadata as live text *outside* decorative assets.

The player includes a decorative analog VU visualization (`AnalogVUMeter`) and tactile playback keys. The animated pieces never imply real audio metering unless connected to actual output levels. Avoid false indicators of buffering or recording.

## 3. Semantic color tokens

The Kotlin and CSS names below are stable contracts. The v3 palette replaces every prior dark theme value.

| Kotlin token name | Prototype CSS token | Value | Usage |
|---|---|---|---|
| `ReadyOColors.background` | `--color-bg` | `#EFE7D6` | Aged-paper app background |
| `ReadyOColors.surface` | `--color-surface` | `#F8F1E3` | Cream list cards |
| `ReadyOColors.surfaceRaised` | `--color-surface-raised` | `#E5D9C3` | Raised analog decks |
| `ReadyOColors.surfaceHover` | `--color-surface-hover` | `#E9DEC9` | Press state |
| `ReadyOColors.textPrimary` | `--color-text-primary` | `#332E27` | Ink headings |
| `ReadyOColors.textSecondary` | `--color-text-secondary` | `#706653` | Warm-gray metadata |
| `ReadyOColors.textMuted` | `--color-text-muted` | `#938671` | Printed labels |
| `ReadyOColors.accentPrimary` | `--color-accent-primary` | `#B75035` | Rust-red transport/CTA |
| `ReadyOColors.accentSecondary` | `--color-accent-secondary` | `#6C785D` | Olive accent stripe |
| `ReadyOColors.danger` | `--color-danger` | `#BB523C` | Favorite / destructive action |
| `ReadyOColors.border` | `--color-border` | `#C5B79E` | Hardware seam / panel outline |
| `ReadyOColors.onAccent` | `--color-on-accent` | `#FFF6E9` | Text on primary action |
| `ReadyOColors.labelSurface` | `--color-label-surface` | `#FDF8E9` | Tape insert / printed labels |
| `ReadyOColors.labelText` | `--color-label-text` | `#352D25` | Text on label panels |

### Extra illustration colors (internal; not global theme tokens)

Use cassette-specific illustration values (`#4B4943` smoked window, `#EDE6D5` plastic shell, `#B3533E` label stripe, `#D8A77B` mustard stripe). Keep these scoped to the player illustration, not the app's common semantic palette.

## 4. Typography, shape and spacing tokens

| Token | Value | Usage |
|---|---|---|
| `ReadyOSpacing.xs` | `4.dp` | Tiny gaps |
| `ReadyOSpacing.sm` | `8.dp` | Row/icon spacing |
| `ReadyOSpacing.md` | `12.dp` | Row padding |
| `ReadyOSpacing.lg` | `16.dp` | Card padding |
| `ReadyOSpacing.xl` | `20.dp` | Horizontal screen padding |
| `ReadyOSpacing.xxl` | `24.dp` | Section spacing |
| `ReadyORadius.sm` | `10.dp` | Small controls |
| `ReadyORadius.md` | `16.dp` | Inputs / list rows |
| `ReadyORadius.lg` | `22.dp` | Cards / hero panels |
| `ReadyORadius.xl` | `30.dp` | Large artwork panels |
| `ReadyODimensions.minTouchTarget` | `48.dp` | Accessibility minimum |
| `ReadyODimensions.bottomNavHeight` | `66.dp` | Bottom bar |
| `ReadyODimensions.miniPlayerHeight` | `66.dp` | Mini-player |

### Typography guidance

Use two families if practical:
- **Primary UI font:** system sans or a clean humanist sans
- **Accent / label font:** optional condensed or mono-style treatment **only** for micro labels such as `LIVE`, `SIDE A`, `QUEUE`, `REC`, `LOCAL`

Do not build the whole app with a novelty retro font.

Suggested hierarchy:
- App title / large player title: bold, slightly condensed feel, strong letterform
- Section headers: semibold
- List titles: medium/semibold
- Metadata: regular
- Micro labels: uppercase, tracked slightly wider

## 5. Shared component names

These names remain the same and should map directly to the CMP app.

| Stable Compose component | Responsibility | Used by |
|---|---|---|
| `ReadyOAppScaffold` | Shared screen container, navigation, mini-player, system insets | All |
| `ReadyOBottomNavigation` | Radio + My Music destinations | R1/R2/R3/M1/M2/M3 |
| `MiniPlayer` | Current artwork, title, source, play/pause, expand | All root/detail screens |
| `NowPlayingScreen` | Shared fullscreen player; source-specific controls | Both sources |
| `StationRow` | Logo, name, metadata, favorite, play | R1/R2/R3 |
| `TrackRow` | Embedded art, title, artist, overflow | M1/M2/M3 |
| `StationCategoryCard` | Browse station location/genre | R1 |
| `MusicCategoryCard` | Songs/Albums/Artists/Folders | M1 |
| `SectionHeader` | Title and optional See all action | R1/M1 |
| `RadioFilterChip` | Search/browse filters with selected state | R2 |
| `ReadyOEmptyState` | Empty stations/music lists with recovery CTA | R2/R3/M1/M2 |
| `ReadyOErrorState` | Error text and retry/permission action | All |
| `ArtworkTile` | Real art with cassette-frame fallback | Lists/NowPlaying |
| `PlaybackControls` | Play/pause/skip; source-specific actions | NowPlaying |
| `QueueTrackRow` | Track and reorder/remove affordance | M3 |

## 6. Component styling rules

### `ReadyOAppScaffold`
- Background should feel like the main body of a cassette player.
- Use subtle panel separation between content area, mini-player, and bottom nav.
- Avoid floating everything on glassy translucent layers.

### `ReadyOBottomNavigation`
- Two large hardware-like tabs.
- Active tab can use `labelSurface` or a stronger accent border.
- Icons should feel simple and slightly chunky.

### `MiniPlayer`
- Styled like a compact deck strip.
- Use a cassette-window-style artwork frame: dark outer frame + light inner label area if needed.
- Place main play/pause as the most tactile control.
- Metadata can show a tiny badge like `LIVE` or `LOCAL`.

### `NowPlayingScreen`
**Aesthetic showcase / full-throttle cassette.** The cassette should be instantly recognizable before the user reads any text.

Visual structure:
1. small top bar: minimize, ReadyO wordmark, overflow
2. **large cassette deck module**, featuring 4 visible screws, cream shell, segmented multicolor printed label, translucent brown window, two reels with magnetic tape ribbon, TYPE I mark, model markings
3. paper track-metadata label below the cassette (real text, never baked into an image)
4. small warm amber VU bars, subtly animated only during playback
5. raised analog transport-key layout with a rust-red primary play/pause
6. tiny bottom status / queue actions

**Animation contract:** `CassetteReels` rotate continuously only in `Playing` and turn off for `Paused`, `Idle`, `Buffering`, `Stopped`, and `Error`. Avoid restart snaps during state recomposition. Reels rotate at subtly different speeds; no looping flashing lights. `AnalogVUMeter` sways when playing as a visual simulation; use actual output-level data only if available. `ON AIR` LED may breathe softly. Respect Android reduced-motion / animator duration scale and iOS Reduce Motion, and freeze nonessential effects when backgrounded.

**Implementation:** CSS keyframes in the HTML prototype are only visual reference. Prefer Compose Canvas / `rememberInfiniteTransition` for mechanically precise reels, or Lottie with a cached vector `.json` under shared resources for complex textures. Lottie is **optional**, not a requirement, and must not own playback state. Provide static fallbacks, avoid audio-reactive claims, and budget animation work in M4 quality pass.

For radio:
- show `LIVE`, station location/genre, and current metadata if available
- no fake progress bar

For music:
- seek bar appears
- `QUEUE`, `SHUFFLE`, `REPEAT` are available

### `StationRow` and `TrackRow`
- Use slightly denser rows than the previous design.
- Artwork or station icon should sit in a framed tile.
- Title line should be bold enough to feel like a cassette label.
- Metadata should feel like printed sub-label information.
- Secondary actions should look like hardware buttons or soft deck keys.

### `StationCategoryCard` and `MusicCategoryCard`
- Give them a label-card aesthetic: cream/sand top surface or inset stripe.
- Category icon can be a simple glyph rather than emoji in production.
- Keep categories clear and tactile, not playful or childish.

### `RadioFilterChip`
- Use pill or ticket-like chips.
- Active chip can use olive or amber depending on emphasis.
- Keep readable contrast; don't make them too decorative.

## 7. Screen visual contracts and layout guidance

The flow and placement stay aligned with the previous design. These notes only change the visual treatment.

### R1 `RadioHomeScreen`
- Header can include a small tape-mark style brand lockup.
- Hero card should resemble a featured deck label or cassette insert card.
- Continue listening CTA should be visually prominent in amber.
- Popular stations list should feel like a curated rack of presets.

### R2 `RadioSearchScreen`
- Search field can resemble a labeled input strip.
- Filter chips should feel like radio preset selectors.
- Keep result list efficient and scannable.

### R3 `SavedStationsScreen`
- Segmented control for `Favorites` / `Recently played` can look like a two-position deck switch.
- Favorites may use warm red-orange heart state.

### M1 `MusicLibraryScreen`
- `Songs`, `Albums`, `Artists`, `Folders` cards should look like categorized tape boxes or sleeves.
- Recently played list should feel consistent with TrackRow styling.

### M2 `MusicCollectionScreen`
- `Play all` is the main amber action.
- `Shuffle` is secondary, possibly olive.
- The header should feel like reading the label of a selected collection.

### M3 `PlayQueueScreen`
- Queue screen can resemble stacked cassette order cards.
- Reorder affordance should be clear and tactile.

## 8. Motion and interaction tone

- Use **short, physical-feeling transitions** and continuous reel rotation only while playing.
- Buttons should have slight press feedback, not bouncy playful animations.
- Mini-player to full player expansion should feel like opening a deck view.
- Keep motion subtle; the retro feeling should come mostly from form, color, and composition.

## 9. Accessibility and practicality rules

Retro inspiration must not damage usability:
- maintain WCAG AA contrast
- minimum 48dp touch targets
- support dynamic type / larger text
- never rely on texture alone for affordance
- avoid tiny low-contrast mono labels for critical content
- decorative retro effects must remain optional and lightweight

## 10. Agent implementation mapping

1. Read `ReadyO_Phase1_Requirements.md` first. It remains the source of truth for behavior.
2. Replace the previous visual direction with this **SIDE A — Cassette Edition** theme while keeping the same route IDs and component names.
3. Implement `ReadyOTheme` in `commonMain` using the same token classes (`ReadyOColors`, `ReadyOSpacing`, `ReadyORadius`, `ReadyODimensions`) but the updated token values in this file.
4. Preserve these stable names exactly: `R1`, `R2`, `R3`, `M1`, `M2`, `M3`, `NowPlayingScreen`, `MiniPlayer`, `StationRow`, `TrackRow`, `PlaybackControls`, `ReadyOBottomNavigation`.
5. Use `ReadyO_UI_Prototype.html` as a visual reference for color balance, hierarchy, and retro motifs — not as production code.
6. Build CMP previews using fake preview data and wire runtime behavior to real state.
7. Do not add a Settings tab or widen Phase 1 scope.

## 11. Short prompt for a coding agent

Read `ReadyO_Phase1_Requirements.md` and `ReadyO_UI_Design_Spec.md` as the source of truth. Use `ReadyO_UI_Prototype.html` only as the visual reference. Implement the updated **SIDE A — Cassette Edition** theme in Compose Multiplatform while preserving the exact screen IDs, route names, and component names. Start with `ReadyOTheme`, `ReadyOAppScaffold`, `ReadyOBottomNavigation`, `MiniPlayer`, and `NowPlayingScreen`, then apply the same theme system to R1–R3 and M1–M3.
