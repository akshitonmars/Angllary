# Angllary design system

## Product feel

Angllary is a quiet, media-first gallery. Images and video are the content; navigation and actions should feel like a light layer over the library rather than a set of heavy app chrome bars.

- No ad surfaces, account prompts, social feed, or AI upsell.
- No saturated accent colour, coloured tints, or decorative gradients.
- Light and dark themes use warm neutral values rather than pure white and pure black.
- Controls are compact, consistently aligned, and remain easy to reach.

## Brand and typography

- Name: Angllary.
- Tagline: Your photos. Your space.
- Initial UI language: English; strings are resource-backed for future localization.
- Google Sans Flex is requested through Android's downloadable-font provider. Android sans-serif remains the fallback when the font provider is unavailable.

## Colour tokens

| Token | Light | Dark |
|---|---|---|
| Canvas | #F2F1ED | #151514 |
| Surface | #FAF9F6 | #20201E |
| Secondary surface | #E8E7E2 | #2B2B29 |
| Primary text | #242320 | #F0EFEA |
| Secondary text | #77766F | #A6A59F |
| Divider | #D9D8D2 | #393936 |

Viewer media is presented on a dark neutral canvas in either theme to keep the image itself visually prominent. Monochrome top and bottom vignettes are used only where controls need contrast.

## Layout and spacing

- Main horizontal inset: 20dp.
- Gallery grid inset: 5dp.
- Thumbnail gap: 4dp, both horizontally and vertically.
- Thumbnail corners: approximately 12dp.
- Album cards and sheets use 18–28dp corner radii.
- Header: brand at left, date label optically centred, overflow at right.
- Footer: separate Search control at lower-left, All/Albums pill aligned to lower-right.
- No solid, full-width bottom navigation shelf.

## Motion direction

Motion should clarify navigation and state changes, never become decoration.

- Tab selection uses a short spring between neutral surfaces.
- Screen changes use restrained fade transitions.
- Viewer controls and details use short fade/vertical-slide transitions.
- Media paging remains gesture-led and direct.
- Avoid long autoplay loops, large parallax movement, and heavy overshoot.

The interaction vocabulary was informed by the component and transition patterns explored in Skiper UI and Figma prototyping, then adapted to native Compose rather than copying web code or motion values.

## Main gallery

- Adaptive staggered grid respects media aspect ratios and uses cached video-frame thumbnails.
- Scroll position updates the display-only date label.
- Sort and grid controls are in the overflow menu.
- Search opens as a local overlay and filters filenames, albums, folder paths, dates, MIME types, media type and favourites.
- The bottom capsule contains only All and Albums.

## Albums

- Device albums are derived from MediaStore folders.
- Custom albums are virtual collections of MediaStore URIs; adding or removing an item does not duplicate the original file.
- Album cover and item count are derived from current library content.
- Smart/AI albums are not part of this release.

## Viewer and editing

- Full-screen horizontal media pager.
- Photos support pinch zoom and pan.
- Videos use Media3 playback controls.
- Viewer actions include share, favourite, details, edit, trash, add/remove from custom album, and copy to Locked Folder.
- Details only show metadata available from Android.
- Photo and video edits are non-destructive exports; the source stays unchanged.

## Privacy surfaces

- Recently Deleted uses Android system trash and its consent/confirmation flow where supported.
- Locked Folder uses Android system authentication and an AES-GCM key held by Android Keystore.
- Locked media is stored as encrypted app-private copies. Decrypted preview cache is cleared on app start and when the viewer closes.
- Backup archives contain only the encrypted media and metadata; the Keystore key is not exported. Restore is limited to the original installation that still has that key.

## Accessibility

- Touch targets remain at least 48dp where practical.
- Icons have content descriptions.
- Text respects system font scaling.
- Animations are short and used to communicate state changes.
