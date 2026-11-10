# Contributing to Angllary

Thanks for your interest in Angllary.

## Before contributing

- Keep the app local-first. Do not add analytics, ad SDKs, or network permissions without a documented product decision.
- Never upload users' photos or videos without a clear, contextual consent flow.
- Preserve originals by default when editing or moving media.
- Keep gallery browsing responsive while background work runs.
- Prefer native Android and Jetpack Compose APIs.

## Development

1. Open the repository in a recent Android Studio release.
2. Use JDK 17 or newer.
3. Build the debug app with the Gradle assembleDebug task.
4. Run relevant unit and instrumentation tests before opening a pull request.

## Pull requests

- Describe the user-visible change.
- Include screenshots for UI changes.
- Mention permissions, storage, privacy, and background-work implications.
- Keep pull requests focused and reasonably small.

## Code style

- Kotlin official style.
- Compose UI should be decomposed into focused composables.
- Keep user-facing text in Android string resources.
- Add comments when explaining non-obvious platform behaviour, not for obvious code.
