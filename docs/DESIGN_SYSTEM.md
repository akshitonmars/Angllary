# Angllary design system

## Product feel

- Minimal, quiet, media-first.
- Photos occupy the majority of the screen.
- Controls are compact, consistently aligned, and predictable.
- No ads, account prompts, social feed, decorative tints, or accent colours.
- Light and dark themes follow the Android system setting.

## Brand and typography

- Name: Angllary.
- Working tagline: Your photos. Your space.
- Initial interface language: English.
- Google Sans is requested as the UI typeface and is used when the Android system exposes it; Android sans-serif is the runtime fallback.
- User-facing strings live in Android resources to support future localization.

## Monochrome colour tokens

| Token | Light | Dark |
|---|---|---|
| Canvas | #F2F2F0 | #111111 |
| Surface | #FAFAF8 | #1A1A1A |
| Secondary surface | #E8E8E6 | #252525 |
| Primary text | #191919 | #F1F1EF |
| Secondary text | #747474 | #A5A5A5 |
| Divider | #D9D9D6 | #353535 |

There are no hue-based accent colours. The top and bottom edge fades are monochrome and intentionally subtle.

## Shapes and spacing

- Navigation: one compact capsule aligned to the lower-right, with a separate inactive Search control on the lower-left.
- No full-width solid navigation shelf.
- Main horizontal inset: 20dp.
- Gallery grid inset: 5dp.
- Gallery thumbnail gap: 4dp.
- Media thumbnails use a medium rounded shape; no persistent outline.
- Keep all header controls vertically centred and the date indicator centred to the screen.

## Main gallery

- Brand at top-left, non-interactive date indicator centred, overflow menu at top-right.
- Adaptive staggered media grid with image dimensions respected.
- Search control is visible but intentionally inactive until its milestone.
- Bottom navigation contains only All and Albums.

## Milestone 2 viewer

- Full-screen image and video presentation.
- Pinch-to-zoom for still images.
- Android video playback controls for videos.
- Previous / next media navigation, sharing, and a details panel.
- Details show only fields available from MediaStore.

## Accessibility

- Touch targets remain at least 48dp where practical.
- Meaningful icons have content descriptions.
- Text follows system font scaling.
- Reduced-motion preferences will be respected as animation is introduced.
