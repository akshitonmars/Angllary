# Angllary

**Your photos. Your space.**

Angllary is a native Android gallery designed around a quiet monochrome interface, private local storage, smooth media browsing and practical organisation.

## Design principles

- **Floating controls:** navigation and viewer actions use independent pill surfaces that sit above media rather than occupying a solid footer bar.

- **Local-first:** the library is read from Android MediaStore. No account or cloud library is required.
- **Private by default:** favourites and collections are local; Angllary does not upload your gallery.
- **Media-first:** photos and videos occupy the screen, while controls stay compact and consistent.
- **True neutral palette:** light mode uses greys and near-white; dark mode uses greys and near-black. Every design token is grayscale—no green, teal or warm tint.
- **Originals protected:** photo and video edits export as new files. Locked Folder imports are encrypted copies.

## Features in v0.5.2

### Gallery and organisation
- [x] Adaptive staggered photo and video grid with consistent spacing and dimensions.
- [x] Local MediaStore metadata: file name, folder, album, dimensions, size, date and duration where available.
- [x] Device-folder albums and custom virtual albums with smooth lazy-recycled rows.
- [x] Add/remove media from custom albums; rename and delete collections.
- [x] Persistent local favourites.
- [x] Newest/oldest sorting and compact/comfortable/large grid settings.
- [x] Dedicated local Search destination for filenames, folders, albums, MIME types, dates, photos, videos and favourites.
- [x] Search result viewer navigation bounded to search matches.
- [x] All/Albums native horizontal pager with preserved gallery and album scroll positions.
- [x] Library statistics and version overview in Settings.

### Photo and video viewing
- [x] Opaque activity-window media viewer with immersive chrome toggle on tap.
- [x] Double-tap to zoom (toggle 1x <-> 2.5x) and boundary-clamped pan gestures for photos.
- [x] Pinch zoom and pan for photos with pager swipe lock when zoomed.
- [x] Next/previous swipes across photos and videos.
- [x] In-app video playback with Media3 ExoPlayer and lifecycle-aware pause.
- [x] Cached video-frame thumbnails with a MediaMetadataRetriever fallback.
- [x] Share sheet, favourites, metadata, album actions and move-to-trash action with system confirmation.

### Editing and privacy
- [x] Photo editor: crop ratios, animated 90° rotation, brightness, contrast and saturation.
- [x] Live frame scrubbing when adjusting video trim start and end sliders.
- [x] Edited photos export as new copies; originals are never overwritten.
- [x] Basic video editor: trim start/end and export a new MP4 copy.
- [x] Edited photos export as new copies; originals are never overwritten.
- [x] Basic video editor: trim start/end and export a new MP4 copy.
- [x] Recently Deleted through Android's system trash confirmation where supported.
- [x] Locked Folder with Android device authentication and AES-GCM encryption backed by Android Keystore.
- [x] Encrypted Locked Folder archive export and restore.

### Interaction and visual polish
- [x] Strict grayscale light/dark colour system.
- [x] Unified floating navigation dock with a consistent shape and control geometry.
- [x] Spring-based tab and tile press feedback; animated overflow icon and viewer actions.
- [x] Page transitions for app destinations and All/Albums.
- [x] Predictive back progress, animated viewer entry/exit and animated viewer chrome.
- [x] Haptic feedback for navigation and major actions.
- [x] Skeleton loading grid and subtle video-thumbnail loading pulse.
- [x] Google Sans Flex with a system font fallback.

## Deliberately out of scope

AI categorisation, object recognition, OCR, semantic search and cloud AI are not included. Search is traditional and local-only. There is no automatic upload or remote photo analysis.

## Platform and privacy notes

- On Android 11 and later, Recently Deleted uses Android's system trash request and confirmation UI. Retention is controlled by Android and the device media provider.
- On older Android versions, system trash may not be available; Angllary explains this rather than silently deleting media.
- Locked Folder creates a private encrypted copy; it does not remove the source item from the normal gallery.
- Locked Folder backups contain encrypted files, not the Android Keystore key. Restore therefore requires the original app installation and its existing Keystore key. Backups cannot migrate private media to a different device or fresh installation.
- Automated Android build validation is not a substitute for physical-device testing. A tester should verify thumbnail generation, video paging, editor export, haptics and predictive back on the target device.

## Technology

- Kotlin and native Android
- Jetpack Compose and Material 3
- Android MediaStore and Storage Access Framework
- Coil for image thumbnails
- AndroidX Biometric and Android Keystore AES-GCM
- AndroidX Media3 ExoPlayer and Transformer
- GitHub Actions for build validation and GitHub Releases distribution

## Build and releases

Open the repository in Android Studio and run `assembleDebug`.

Every push to `main` builds the APK. The workflow reads `versionName` from `app/build.gradle.kts`, creates or updates the matching `v<versionName>` GitHub Release, and attaches `app-debug.apk`.

## License

Apache-2.0. See [LICENSE](LICENSE).
