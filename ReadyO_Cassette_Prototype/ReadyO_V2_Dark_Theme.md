# ReadyO — Archive Edition V2 / Dark Theme

**Design variant:** `Archive Dark`  
**Status:** visual theme extension; **no product or navigation changes**  
**Reference light theme:** `ReadyO_V2_UI_Design_Spec.md`  
**Requirements:** `ReadyO_Phase1_Requirements.md`  
**Dark screen art direction:** `ReadyO_V2_Dark_NowPlaying.png`  
**Platform:** Compose Multiplatform (CMP), `commonMain`

> **Agent contract:** This document defines **dark-mode color values for the existing V2 semantic tokens**. It does not rename components, routes, or IDs. Keep `R1`, `R2`, `R3`, `M1`, `M2`, `M3`, `NowPlayingScreen`, `MiniPlayer`, and the original two-tab `Radio` / `My Music` navigation. The reference image is **art direction only**: actual fullscreen `NowPlayingScreen` behavior and navigation remain governed by the product requirements and current V2 spec.

## 1. Visual intent

**Premium analog tape deck, late at night.** An almost-black warm-graphite interface, a smoked translucent cassette shell, brushed-metal reel hubs, a physically printed cream cassette label, and subdued copper transport keys. Preserve Archive Edition's editorial typography, generous whitespace, and precise mechanical geometry.

- **Keep:** real cassette proportions, brushed hardware, glass/smoke window, engraved screws, tactile transport buttons, elegant labels.
- **Use sparingly:** copper/orange on active playback, live indicators, and tiny VU highlights; sage/olive on low-emphasis supporting details.
- **Avoid:** electric/neon colors, futuristic HUD elements, heavy gradients, fake colored glows on all cards, bright cyberpunk accents, cartoon reels.
- **Do not change:** size, spacing, typography roles, view hierarchy, screen routes or interaction behaviors when toggling themes.

## 2. Exact mapping — V2 current light → new dark

Retain semantic names in `ReadyOColors` and CSS variable identifiers exactly as specified in V2. Only the values differ by theme.

| Kotlin semantic token | CSS token | V2 Light (existing) | Archive Dark (new) | Purpose |
|---|---|---|---|---|
| `ReadyOColors.background` | `--color-bg` | `#F0EEE8` | **`#0E0F10`** | Screen background, dark graphite |
| `ReadyOColors.surface` | `--color-surface` | `#FAF8F2` | **`#1A1C1D`** | Cards, track rows, search fields |
| `ReadyOColors.surfaceRaised` | `--color-surface-raised` | `#E6E2D7` | **`#252729`** | Elevated mini-player, sheets, deck panel |
| `ReadyOColors.surfaceHover` | `--color-surface-hover` | `#EAE6DB` | **`#303335`** | Pressed/focused states |
| `ReadyOColors.textPrimary` | `--color-text-primary` | `#252C28` | **`#F0E7D7`** | Primary headings and title text |
| `ReadyOColors.textSecondary` | `--color-text-secondary` | `#657069` | **`#B8AA96`** | Artist, station description, metadata |
| `ReadyOColors.textMuted` | `--color-text-muted` | `#89918A` | **`#8E867A`** | Micro-labels and less prominent copy |
| `ReadyOColors.accentPrimary` | `--color-accent-primary` | `#A64F36` | **`#D57B49`** | Active playback, LIVE, links, selected states |
| `ReadyOColors.accentSecondary` | `--color-accent-secondary` | `#65735F` | **`#9CAA88`** | Sage hardware/secondary active states |
| `ReadyOColors.danger` | `--color-danger` | `#B6533C` | **`#DE816C`** | Error and destructive action |
| `ReadyOColors.border` | `--color-border` | `#D7D3C9` | **`#434541`** | Panel borders, dividers, control outlines |
| `ReadyOColors.onAccent` | `--color-on-accent` | `#FFF9EE` | **`#1C1510`** | Dark icon/text on copper button |
| `ReadyOColors.labelSurface` | `--color-label-surface` | `#E9E4D7` | **`#DDC8AB`** | Cream paper label on cassette, special highlights |
| `ReadyOColors.labelText` | `--color-label-text` | `#242A26` | **`#30251C`** | Ink printed on cassette paper label |

**Mapping rule:** Treat `labelSurface` / `labelText` as a deliberate light-on-dark **physical cassette-label island**, not as the default card surface. Most dark-theme UI panels remain dark.

## 3. Cassette-specific material palette

V2 already separates cassette illustration colors from general UI semantics. Keep these in a dedicated `ReadyOCassettePalette`, rather than introducing screen-wide colors that make app controls look like hardware.

| Physical element | Current V2 light direction | Archive Dark (new) | Intended treatment |
|---|---|---|---|
| `deckFrame` | Brushed aluminum `#CFCCC2` | `#272725` | Satin blackened aluminum chassis |
| `cassetteShell` | Ivory / pale grey case | `#191A19` | Smoked transparent plastic shell |
| `cassetteWindow` | Dark acrylic `#222621` | `#0A0D0D` | Deep smoked viewing window |
| `reelAluminum` | Pale silver reel hubs | `#A5A59A` | Satin brushed-metal hubs |
| `labelPaper` | Off-white `#F6F3E9` | `#DDC8AB` | Warm aged paper, remains visibly light |
| `labelInk` | Dark forest / ink | `#30251C` | Printed cassette title and track number |
| `oxideTape` | Dark brown tape | `#473024` | Realistic magnetic tape ribbon |
| `screwMetal` | Brushed silver screws | `#857F74` | Subtle mechanically grounded screws |
| `vuHighlight` | Muted olive accent | `#C88345` | Amber analog meter illumination, local to meter |

**Important:** Metal highlights and VU glow are **illustrative component materials**, not new app-wide accent colors.

## 4. Compose Multiplatform implementation example

Use this as a mapping reference; adapt to the exact types already in the repo. **Do not create a duplicate `ReadyOColors` API** if it exists.

```kotlin
// commonMain — values assigned to the EXISTING ReadyOColors semantic contract.
internal object ReadyOArchiveDarkPalette {
    val background = Color(0xFF0E0F10)
    val surface = Color(0xFF1A1C1D)
    val surfaceRaised = Color(0xFF252729)
    val surfaceHover = Color(0xFF303335)
    val textPrimary = Color(0xFFF0E7D7)
    val textSecondary = Color(0xFFB8AA96)
    val textMuted = Color(0xFF8E867A)
    val accentPrimary = Color(0xFFD57B49)
    val accentSecondary = Color(0xFF9CAA88)
    val danger = Color(0xFFDE816C)
    val border = Color(0xFF434541)
    val onAccent = Color(0xFF1C1510)
    val labelSurface = Color(0xFFDDC8AB)
    val labelText = Color(0xFF30251C)
}
```

```kotlin
@Composable
fun ReadyOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) readyOArchiveDarkColors() else readyOArchiveLightColors()
    CompositionLocalProvider(LocalReadyOColors provides colors) {
        // Keep existing Material3 color scheme / typography integration.
        content()
    }
}
```

The second snippet is **illustrative**: `readyOArchiveDarkColors`, `readyOArchiveLightColors`, and `LocalReadyOColors` should be matched to the actual project's theme implementation rather than copied as unresolved symbols.

## 5. Component mapping

| Existing component | Light theme | Archive Dark behavior |
|---|---|---|
| `ReadyOAppScaffold` | Pale ivory background | Nearly black graphite; no global orange gradient |
| `ReadyOBottomNavigation` | Pale tab bar | Raised carbon rail, selected tab copper/sage; still exactly two destinations |
| `MiniPlayer` | Light raised module | Smoked graphite module, light artwork, one copper active control |
| `NowPlayingScreen` | Brushed silver deck | Blackened metal deck + cream cassette label; maintain real control positions |
| `ArtworkTile` | Bright sleeve / light fallback | Preserve real embedded art, charcoal fallback frame |
| `StationRow`, `TrackRow` | Neutral light list | Tight dark card/row hierarchy; cream title, taupe metadata |
| `StationCategoryCard`, `MusicCategoryCard` | Pale surfaces | Dark raised surface with restrained cassette-label cues |
| `RadioFilterChip` | Light outlined pills | Graphite pills; copper or sage selected state only |
| `PlaybackControls` | Green transport control | Copper active key with dark icon; inactive keys dark brushed metal |
| `QueueTrackRow` | Light queue list | Charcoal list with subtle divider and accessible reorder controls |
| `ReadyOEmptyState`, `ReadyOErrorState` | Dark text on light | Cream text on dark; clear functional CTA and error colors |

## 6. Now Playing: the signature dark screen

Retain the existing V2 vertical structure:

1. **Upper fascia:** dismiss arrow + `SIDE A / RADIO` or `SIDE B / MY MUSIC`; tiny monochrome control marks.
2. **Title:** editorial `Now playing.` in warm cream, quiet monospaced descriptor.
3. **Cassette object:** blackened metal shell, low-sheen smoked acrylic window, warm printed label, realistic aluminum hubs and small machine screws. This object should visually dominate.
4. **Metadata:** station / song name and secondary location / artist line; large enough to be legible.
5. **Source state:** Radio shows `ON AIR` and **no fake seek bar**. Music shows real scrubber, elapsed/duration, queue controls.
6. **Transport keys:** tactile matte controls with one copper active control; avoid oversized neon circular buttons.
7. **Footer:** restrained source labels / small meter treatment only where space allows.

### Animation

- Rotate inner cassette hubs while `PlaybackState.Playing`. Pause for `Paused`, `Idle`, and `Error`. Buffering can show a subtle nonreel indicator.
- Slightly different reel speeds or winding/tape amount are aesthetic enhancements; they do **not** have to mirror actual magnetic tape timing.
- Optional VU needle animation should be explicitly **illustrative** unless driven by real audio-level analysis; do not claim it reflects actual signal amplitude.
- Prefer lightweight native Compose animation for reels (`rememberInfiniteTransition`) over a compulsory Lottie dependency. Allow Lottie as replaceable art behind `CassetteReelAnimation`.
- Honor reduced motion; static cassette must still look good.

## 7. Accessibility and contrast

The primary dark palette was checked at these example pairings:

| Combination | Approx. contrast |
|---|---:|
| `textPrimary` / `background` | 15.6:1 |
| `textSecondary` / `surface` | 7.5:1 |
| `textMuted` / `surface` | 4.8:1 |
| `accentPrimary` / `surface` | 5.5:1 |
| `onAccent` / `accentPrimary` | 5.8:1 |
| `labelText` / `labelSurface` | 9.2:1 |

Continue to test component-level combinations, opacity, and disabled states in the actual app. Keep the V2 minimum touch targets and font scaling behavior.

## 8. Implementation instructions for the coding agent

1. Read `ReadyO_Phase1_Requirements.md`, then `ReadyO_V2_UI_Design_Spec.md`, then **this dark-theme mapping**.
2. Reuse every existing screen/route/component identifier and every semantic color-token property.
3. Implement dark values alongside the current V2 light values in `ReadyOTheme` (not as a separate UI tree).
4. Use device/system dark-theme selection by default; **do not add a Settings screen** in Phase 1.
5. Scope cassette materials to `NowPlayingScreen` and related artwork composables. Real content and player state always take priority over decorative UI.
6. Build visual previews for `NowPlayingScreen` Radio + Music and at least `R1` and `M1` under both palettes.
7. Compare the final CMP dark preview with `ReadyO_V2_Dark_NowPlaying.png` for **mood and materials**, not exact markup or any prototype-only controls. Preserve the current V2 layout and requirements where the illustration differs.

**Implementation prompt:**

> Extend the existing ReadyO Archive Edition V2 CMP theme with the token values in `ReadyO_V2_Dark_Theme.md`. Preserve the entire Phase 1 screen/route/component contract. Implement system light/dark switching through the existing `ReadyOTheme`, then update the shared cassette visuals and all screen surfaces with semantic colors. The supplied PNG is an aesthetic reference; the V2 requirements and navigation remain authoritative. Do not create a separate SwiftUI UI, add new tabs, or introduce a Settings screen.
