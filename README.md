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

## Features in v0.5.1

### Gallery and organisation
- [x] Adaptive staggered photo and video grid with consistent spacing and dimensions.
- [x] Local MediaStore metadata: file name, folder, album, dimensions, size, date and duration where available.
- [x] Device-folder albums and custom virtual albums.
- [x] Add/remove media from custom albums; rename and delete collections.
- [x] Persistent local favourites.
- [x] Newest/oldest sorting and compact/comfortable/large grid settings.
- [x] Dedicated local Search destination for filenames, folders, albums, MIME types, dates, photos, videos and favourites.
- [x] All/Albums native horizontal pager with preserved gallery and album scroll positions.
- [x] All and Albums scroll positions preserved when switching tabs and returning from Search.

### Photo and video viewing
- [x] Opaque activity-window media viewer (not a Dialog window), with next/previous swipes across photos and videos.
- [x] Floating pill header and action dock remain visible, and the viewer canvas covers both system-bar edges so the gallery underneath cannot peek through.
- [x] Photo swipes work at fit-to-screen scale; image panning takes over only after pinch zoom. Video swipes use a direction and distance threshold so normal playback taps remain usable.
- [x] Pinch zoom and pan for photos.
- [x] In-app video playback with Media3 controls.
- [x] Cached video-frame thumbnails with a MediaMetadataRetriever fallback for devices whose MediaStore thumbnail provider does not return a frame.
- [x] Share sheet, favourites, metadata, album actions and move-to-trash action. The viewer closes only after Android confirms the trash request.

### Editing and privacy
- [x] Photo editor: crop ratios, rotate, brightness, contrast and saturation.
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
