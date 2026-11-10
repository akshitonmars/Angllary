# Angllary design system

This document records the first implementation direction. Colours and spacing are foundational defaults and can be refined as the visual identity matures.

## Product feel

- Minimal, calm, media-first.
- Photos occupy the majority of the screen.
- Controls are compact, rounded and predictable.
- No ads, account prompts, social feed, or decorative clutter.
- Light and dark themes follow the Android system setting.

## Brand and language

- Name: Angllary.
- Working tagline: Your photos. Your space.
- Initial interface language: English.
- User-facing text is kept in Android string resources so translations can be added without changing UI code.

## Colour tokens

| Token | Light | Dark |
|---|---|---|
| Canvas | #F6F5F2 | #121311 |
| Surface | #FFFFFF | #1C1E1B |
| Primary text | #20211F | #F3F1EC |
| Secondary text | #85847F | #A5A39C |
| Accent | #C86F4A | #E59A78 |
| Soft accent | #F3E3DA | #3B2A23 |

## Shapes and spacing

- Pills: fully rounded.
- Media thumbnails: approximately 18–20dp corner radius.
- Cards: 20–22dp corner radius.
- Main horizontal inset: 20dp.
- Gallery grid gap: 9dp.
- Avoid persistent borders; use tonal surfaces and subtle outlines only where needed.

## Main gallery

- Brand pill at top-left.
- Scroll-aware date indicator in the top centre; display-only and not tappable.
- Overflow menu at top-right.
- Adaptive masonry-inspired media grid with varied thumbnail aspect ratios.
- Separate circular Search control at bottom-left.
- Two-destination pill navigation: All and Albums.

## Albums

- Mixed album presentation.
- Larger cover cards for featured folders.
- Compact rows for the rest.
- Smart Albums will be populated when on-device analysis is implemented.

## Accessibility

- Touch targets should be at least 48dp.
- Content descriptions for meaningful icons and media thumbnails.
- Text remains readable with system font scaling.
- Use system light/dark mode and respect reduced-motion preferences as animation is introduced.
