# FitTrack implementation reference

Design: https://www.figma.com/design/5fIyFMT8EHFrex6OCg8BZK/FitTrack?node-id=1110-16468

The application is native Compose Multiplatform. Figma screenshots are visual references, not runtime UI assets. Existing local data, navigation, draft confirmation, and saveable state are retained.

## Screen references

| Native route | Figma frames |
| --- | --- |
| Trainer home | `1107:13367` |
| Client list | `1106:12531`, `1110:18296` |
| Invitation, incoming request, chat, acceptance | `1110:17125`, `1110:17294`, `1110:17490`, `1110:17790`, `1110:17868` |
| Client schedule | `1069:11931` |
| Program selection | `1069:12165`, `1069:12334`, `1070:12864` |
| Program structure | `1126:20006`, `1126:20659`, `1110:19345` |
| Exercise selection and parameters | `1070:17367` |
| Program scheduling | `1126:21418`, `1126:21906`, `1126:22155` |

## Shared implementation

- `FitControls.kt`: text fields, icon and text actions, outlined actions, circular selectors, switches, menus, dialogs, and pinned footer. Layout, shape, and color are defined here rather than inherited from standard Material controls.
- `FitTheme.kt`: local Inter 400/500/600/700 for all application text, toolbar titles, editable values, and counters. The user's Inter-only policy overrides other font families in Figma; sizes, line heights, and weights come from the corresponding design nodes. Default Material letter spacing is removed. There is no remote font dependency; the SIL Open Font License is bundled alongside the fonts.
- `WorkoutComponents.kt`: screen shell, 64 dp toolbar, 18 dp content corners, calendar, exercise rows and chips. Primary actions are 52 dp high; compact actions are 34 dp high with 10 dp corners.
- `ClientScheduleScreen.kt`: dedicated layout for frame `1069:11931`, with a 32 dp tab strip, a 110 dp program informer, a 305 dp five-row calendar, and a 161 dp empty-workouts card at the reference width. Its actions use the frame's 34/44 dp heights and 12/14 dp corners. Calendar dates remain functional and use the actual selected year.
- `typography-reference.json` and `typography-subpanels.json`: text sizes, weights, and line heights read from 22 implemented design frames and subpanels. Original font-family metadata is retained for provenance; the application always renders Inter.
- Dashboard sections have separate gray surfaces with white separators. Client cards, program cards, and exercise parameters use the corresponding white and pale-purple surfaces.
- Android status and navigation bars use system insets. The iPhone status bar and home-indicator artwork from Figma are not drawn into the application.

## Scope of local behavior

The local model supports sets, repetitions or timed sets, a shared duration or individual durations, optional rest, shared or per-set weights, MAX weight modes, supersets, weekly schedules, copying programs, and assignment. Exercise parameters can be edited directly in the picker and after adding to a program. Saveable records retain all these modes and remain compatible with previous records. Remote media still shows the design's empty state until content is supplied.

Closing the personal QR-link sheet (close button, outside dismissal, swipe, or Back) shows the unread new-request dialog over the current screen. "Посмотреть позже" dismisses it without changing the screen; "Перейти к запросу" opens the request conversation. A dismissed/read request is not shown again merely by reopening and closing the QR sheet.

Screens for which this Figma section has no matching frame, such as the existing program-library overview and generic information dialogs, reuse the same controls and typography. Their arrangement is not claimed as an exact Figma reproduction.

## Verification

- Android debug build succeeds with `.\kotlin.bat build -m androidApp`.
- Native emulator review at 375 dp: clients → program selection → structure → exercise parameters → schedule → assignment → reopen editor.
- Edited sets, timed mode, MAX weight, and both exercises in a superset survive assignment and Android Activity recreation.
- Legacy exercise records and 200 exercise/program/library serialization cases pass round-trip checks, including Unicode and delimiter characters in names.
- Referenced image resources exist and are nonempty; VectorDrawable XML parses; `git diff --check` passes. The packaged APK contains only the four Inter font files.
- QR dismissal by close button and Android Back shows the request dialog with native window dimming over the client list. Both request actions and non-repetition of an already-read request were checked in the emulator.
- Final APK: `build/artifacts/FitTrack-figma.apk`; review screenshots: `build/ui-review/`.
- iOS execution was not tested on this Windows host.
