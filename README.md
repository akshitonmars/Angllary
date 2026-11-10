# Angllary

**Your photos. Your space.**

Angllary is a native, local-first Android gallery focused on a calm visual experience, fast media browsing, thoughtful organisation, and privacy.

## Product principles

- **Local-first:** the gallery works without an account or cloud service.
- **Private by default:** media stays on the device unless a future feature explicitly asks for permission.
- **Fast:** browsing does not wait for background indexing or AI.
- **Quiet design:** near-black and off-white surfaces, compact controls, and no decorative colour.
- **Open source:** the project is developed in public.

## Technology

- Kotlin
- Native Android + Jetpack Compose
- Material 3 with a monochrome Angllary design system
- Android MediaStore for device media
- Coil for efficient image thumbnails
- GitHub Actions for reproducible APK builds and GitHub Releases distribution

## Milestones

### Milestone 1 — App foundation
- [x] Android project, navigation shell, design system, and adaptive launcher icon
- [x] Media permission onboarding and local MediaStore query
- [x] All / Albums gallery shell, sorting, and grid density controls
- [x] GitHub Actions build pipeline

### Milestone 2 — Media viewing
- [x] Full-screen image viewer with pinch-to-zoom
- [x] In-app video playback with Android playback controls
- [x] Previous / next media navigation
- [x] Share media through Android Sharesheet
- [x] Media details panel with file, date, resolution, album, and MIME type
- [x] Monochrome UI refresh and updated app icon

### Later milestones
- [ ] Search and album management
- [ ] Locked Folder and Recently Deleted
- [ ] Photo and video editing
- [ ] Optional on-device AI organisation and search

## Build and releases

Every push to main runs the Android debug build. To publish an APK, create and push a version tag such as v0.2.0; the release workflow builds the APK and attaches it to the matching GitHub Release.

For a local Android Studio build, open the repository as a Gradle project and run the assembleDebug task.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Product and implementation decisions are documented in [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md).

## License

Apache-2.0. See [LICENSE](LICENSE).
