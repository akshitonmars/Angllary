# Angllary

**Your photos. Your space.**

Angllary is a native, local-first Android gallery focused on a calm visual experience, fast media browsing, thoughtful organisation, and privacy.

> This repository is in its foundation stage. The first milestone establishes the Android application, design system, gallery shell, and reproducible cloud debug builds. Features are added incrementally; unfinished features are not represented as production-ready.

## Product principles

- **Local-first:** the gallery works without an account or a cloud service.
- **Private by default:** media stays on the device unless a future feature explicitly asks for permission.
- **Fast:** browsing must not wait for background indexing or AI.
- **Quiet design:** media is the focus; controls stay compact and predictable.
- **Open source:** the project is developed in public.

## Technology

- Kotlin
- Native Android + Jetpack Compose
- Material 3, with a custom Angllary design system
- Android MediaStore for device media
- Coil for efficient media thumbnails
- GitHub Actions for debug APK builds

## Build

The repository includes a GitHub Actions workflow that builds a debug APK on pushes to `main`.

The generated APK is published as a workflow artifact named `angllary-debug-apk`.

For a local Android Studio build, open the repository as a Gradle project and run the `assembleDebug` task.

## Project status

- [x] Initial Android project and design-system foundation
- [x] Gallery shell and All / Albums navigation
- [ ] Full MediaStore permission and library indexing
- [ ] Photo viewer and video playback
- [ ] Search and album management
- [ ] Locked Folder and Recently Deleted
- [ ] Editors and on-device AI

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Product and implementation decisions are documented in [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md).

## License

Apache-2.0. See [LICENSE](LICENSE).
