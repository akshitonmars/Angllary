# Angllary

**Your photos. Your space.**

Angllary is a native Android gallery designed around a calm, media-first interface, private local storage, and fast everyday organisation.

## Design principles

- **Local-first:** the library is read from Android MediaStore. No account or cloud library is required.
- **Private by default:** personal organisation stays on-device; Angllary does not upload your gallery.
- **Media-first:** photos and videos occupy the screen, while controls stay compact and consistent.
- **Quiet monochrome:** warm off-whites in light mode and charcoal neutrals in dark mode. No coloured accent palette, pure-black canvas, or pure-white surfaces.
- **Originals protected:** edits export as new files. Locked Folder imports are encrypted copies.

## Features in v0.4.0

### Gallery and organisation
- [x] Adaptive staggered photo and video grid with small, consistent thumbnail gaps.
- [x] Local MediaStore indexing with file name, folder, album, dimensions, size, date and duration metadata where available.
- [x] Device-folder albums and custom virtual albums.
- [x] Add and remove items from custom albums; rename and delete collections.
- [x] Local favourites.
- [x] Newest/oldest sorting and compact/comfortable/large grid settings.
- [x] Separate Search control and on-device search for names, folders, albums, MIME types, dates, photos, videos and favourites.

### Photo and video viewing
- [x] Full-screen viewer with swipe navigation between media.
- [x] Pinch zoom and pan for photos.
- [x] In-app video playback with Media3 playback controls.
- [x] Share sheet, favourites, media details, album actions and move-to-trash action.
- [x] Metadata panel for file name, date, dimensions, file size, duration, folder and MIME type.
- [x] Cached local video-frame thumbnails in the gallery and lists.

### Editing and privacy
- [x] Photo editor: crop ratios, rotate, brightness, contrast and saturation.
- [x] Export edited images as new copies; originals are left untouched.
- [x] Basic video editor: trim start/end and export a new MP4 copy.
- [x] Recently Deleted via Android's system trash confirmation on supported Android versions.
- [x] Locked Folder secured by Android device authentication.
- [x] Locked Folder media is encrypted with AES-GCM using a key held in Android Keystore.
- [x] Encrypted Locked Folder archive export and restore.

### Personalisation
- [x] System, light and dark appearance settings.
- [x] Grid density and sorting preferences persist locally.
- [x] Google Sans Flex through Android's downloadable-font provider, with a system font fallback.
- [x] Soft edge vignettes, compact floating navigation, restrained transitions and consistent spacing.

## Deliberately out of scope for this release

AI categorisation, object recognition, OCR, semantic search and cloud AI are not included. Search is traditional and local-only. No automatic upload or remote photo analysis is used.

## Important platform and privacy notes

- On Android 11 and later, moving media to Recently Deleted uses Android's system trash request and confirmation UI. Actual retention is controlled by Android and the device media provider.
- On older Android versions, system trash may not be available; Angllary explains this rather than silently deleting media.
- Locked Folder creates a private encrypted copy; it does not remove the source item from the normal gallery.
- Locked Folder backups contain encrypted files, not the Android Keystore key. Restore therefore requires the original app installation and its existing Keystore key. The archive cannot be used to transfer private media to a different device or a fresh installation.
- The current build is a debug APK for testing. It has passed the automated Android build, but still needs hands-on testing on physical devices.

## Technology

- Kotlin and native Android
- Jetpack Compose and Material 3
- Android MediaStore and Storage Access Framework
- Coil for image thumbnails
- AndroidX Biometric
- Android Keystore AES-GCM encryption
- AndroidX Media3 ExoPlayer and Transformer
- GitHub Actions for build validation and GitHub Releases distribution

## Build and releases

Open the repository in Android Studio and run assembleDebug.

Every push to main builds the APK. The workflow reads versionName from app/build.gradle.kts, creates or updates the matching v<versionName> GitHub Release, and attaches app-debug.apk.

## License

Apache-2.0. See LICENSE.
