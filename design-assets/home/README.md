Source: [FitTrack, section 1110:16468](https://www.figma.com/design/5fIyFMT8EHFrex6OCg8BZK/FitTrack?node-id=1110-16468). Current dashboard: `1107:13367`; original asset import: `747:17786`.

These SVG files are the original icons exported by Figma. Their VectorDrawable versions in `app/shared/composeResources/drawable` were converted with the Android SDK's `Svg2Vector`, preserving their intrinsic sizes. The five PNG resources in that directory are the original profile photos and client-status illustrations from the same screen.

The navigation SVGs were refreshed from dashboard `1107:13367`, including the layers icon for Materials. `field_clear.svg` comes from `1126:20006`; `workout_disclosure.svg` comes from `1069:12165`. Their VectorDrawable versions preserve the source viewports and intrinsic dimensions.

The screen is implemented with Compose layouts; no screenshot is used as UI content. System status and navigation bars are supplied by the device.

The `client_profile_prev`, `client_profile_next`, and `client_profile_all` SVGs are from client detail frame `1069:11931`; their intrinsic sizes are 20, 20, and 16 pixels. `client_profile_chat.svg` is the complete original 24 × 24 icon instance `I1069:11943;58665:37349`. The runtime uses its original transparent 3× PNG export `client_profile_chat_icon.png` at 24 dp so Android preserves the tiny dot details, centered in the matching 48 dp button slot. The SVG sources and VectorDrawable counterparts remain local.
