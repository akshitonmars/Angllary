# Angllary design system

## Product feel

Version 0.6.0 completes the interface around true black/white foundations, floating pill controls and native paging.

Angllary is a quiet, media-first gallery. Content is the focal point; navigation and actions are a light, consistent layer rather than heavy app chrome.

- No advertisements, account prompts, social feed or AI upsell.
- No hue-based accents, coloured tints or decorative gradients.
- Light and dark themes are neutral greys with near-white and near-black anchors.
- Controls share a defined height, corner radius, spacing and alignment system.
- Motion communicates navigation, feedback and loading; it is never decorative for its own sake.

## Brand and typography

- Name: Angllary.
- Tagline: Your photos. Your space.
- Initial UI language: English; strings are resource-backed for future localization.
- Google Sans Flex is requested through Android's downloadable-font provider. Android sans-serif remains the fallback.

## Neutral colour system

All colour tokens use equal red, green and blue channel values. This is intentional: it prevents teal, green or warm casts in surfaces, text and controls.

| Token | Light | Dark |
|---|---|---|
| Canvas | #FFFFFF | #000000 |
| Surface | #FFFFFF | #111111 |
| Secondary surface | #F4F4F4 | #191919 |
| Raised surface | #EAEAEA | #242424 |
| Primary text | #000000 | #FFFFFF |
| Secondary text | #666666 | #B3B3B3 |
| Divider | #D6D6D6 | #363636 |

Viewer content uses a consistent dark-neutral viewing canvas in both themes. Media overlays use monochrome gradients only to preserve text and control contrast.

## Shape and component system

- Floating dock: a unified 58dp-tall pill, 50dp outer radius, raised above the gallery with a clear outline and shadow; it overlays content instead of occupying a solid footer row.
- Dock segments: equal 48dp hit area and 18dp inner radius.
- Primary compact actions: 42–48dp height, 12–14dp corner radius.
- Search field: 16dp corner radius.
- Media tiles: 13dp corner radius and 4dp gaps.
- Album covers: 15dp corner radius.
- Settings groups: 16dp corner radius.
- Main horizontal inset: 20dp; gallery inset: 5dp.

The brand and date in the top bar are typographic elements, not separate competing pills. Search and All/Albums belong to one dock with a shared height, surface, border and alignment.

## Navigation and interaction

- The home dock floats above the content area; gallery and album lists reserve enough trailing scroll space to avoid hiding the last items.
- Media viewer is drawn as an opaque activity-level Compose overlay rather than an Android Dialog window. It covers the complete edge-to-edge canvas, including system-bar regions, and prevents the underlying gallery from showing through.
- Viewer header and action dock are persistent floating pills; the trash action remains available while Android's confirmation UI is shown.

- Main header: app mark and brand at left, a standalone date pill without a calendar glyph, and a separate overflow pill at right. The status-bar region uses the same real clipped surface; scrolling morphs the header into a smaller floating pill.
- Bottom dock: Search action, divider, All and Albums.
- A native horizontal pager switches All/Albums, leaving vertical scrolling to each page. The selected destination and scroll positions survive switching and opening Search.
- Search is a dedicated destination, not a modal dialog.
- System predictive back moves the destination surface with the back gesture.
- The viewer pages horizontally through photos and videos. Video playback uses custom Compose controls and a dedicated seek bar placed above the action dock; Media3's built-in controller is disabled to prevent overlapping UI.
- Long-press on a library tile opens a focused, enlarged media preview over a blurred grid and blurred navigation chrome, with a compact action menu.
- Media details open as a distinct full-screen information destination.

## Motion direction

Motion is short, calm and spring-led where physical feedback helps.

- Tab selection uses a restrained spring between neutral surfaces.
- Press feedback scales media tiles and dock actions by only a few percent.
- Page navigation uses directional slide and fade transitions.
- Header and dock transition with the destination instead of appearing/disappearing abruptly.
- Viewer controls use short fade/vertical-slide transitions; viewer entry/exit uses a restrained fade/scale.
- Loading uses a soft skeleton grid and a low-contrast video-thumbnail pulse.
- Reduced and disabled motion preferences should be respected as accessibility work is expanded.

Motion references were explored through Figma Smart Animate concepts and Skiper UI interaction patterns, then adapted to native Compose APIs rather than copying web implementations. For platform-specific behaviour, Compose transitions, gestures and predictive-back APIs are the implementation reference.

## Main gallery

- Adaptive staggered grid preserves media aspect ratios.
- Video thumbnails first try MediaStore's thumbnail provider, then fall back to MediaMetadataRetriever.
- Video frames are held in a bounded memory cache.
- Scroll position updates the display-only date label.
- Sort and grid density remain in the overflow menu.
- Search filters filenames, albums, folder paths, dates, MIME types, media types and favourites locally.
- The bottom dock is shared and geometrically consistent.

## Albums and Search

- Device albums are derived from MediaStore folders and use compact thumbnail rows; user-created collections retain cover-card tiles.
- Custom albums are virtual collections of MediaStore URIs; they do not duplicate original files.
- Album cards share one aspect ratio, corner shape and pressed-state animation.
- All/Albums scroll states are held above the animated destination so they can be restored.
- Search is a dedicated local page with media filters and results that reuse the same image/video thumbnail component.

## Viewer and editing

- Full-screen horizontal media pager.
- Photos support pinch zoom and pan.
- Videos use Media3 playback controls and allow horizontal page swipes from the playback canvas.
- Viewer actions include share, favourite, details, edit, trash, add/remove from custom album and copy to Locked Folder.
- Metadata only shows fields available from Android.
- Photo/video edits are non-destructive exports; source media remains unchanged.

## Privacy surfaces

- Recently Deleted uses Android system trash and its confirmation flow where supported.
- Locked Folder uses Android system authentication and an AES-GCM key held by Android Keystore.
- Locked media is stored as encrypted app-private copies. Decrypted preview cache is cleared on app start and when the viewer closes.
- Backup archives contain encrypted media and metadata, never the Keystore key. Restore is limited to the original installation that still has that key.

## Accessibility

- Touch targets remain at least 48dp where practical.
- Icons have content descriptions.
- Text respects system font scaling.
- Haptics are limited to clear interaction confirmations.
- Animations are short and tied to state changes.
