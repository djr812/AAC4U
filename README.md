# AAC4U — Augmentative and Alternative Communication

**Free, open-source AAC app for Android tablets.**

AAC4U helps people who have difficulty with spoken language to communicate using symbol-based grids, core vocabulary, and text-to-speech.

## Features

- **Symbol-based communication** with customisable picture grids
- **Core + fringe vocabulary** model (high-frequency words always visible)
- **Text-to-speech** with offline voices, adjustable rate and pitch
- **Configurable grid layouts** from 3×3 to 6×10
- **High contrast themes** and accessibility-first design
- **Multiple user profiles** with individual settings
- **Backup/restore** for complete configuration portability
- **Phrase prediction** based on usage patterns
- **Sentence building** with visual sentence bar
- **Quick phrases** for common needs ("I need help", "I'm hungry")

## For Users With

- Motor impairments (large touch targets, dwell selection, switch access)
- Cognitive differences (consistent layout, minimal navigation depth)
- Vision issues (high contrast, customisable colours)
- Language delays (symbol-based communication, core vocabulary)

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture
- **Database:** Room (SQLite) — fully offline
- **TTS:** Android TextToSpeech API
- **Symbols:** ARASAAC (CC BY-NC-SA)
- **DI:** Hilt

## Building

```bash
# Clone the repository
git clone https://github.com/djr812/aac4u.git
cd aac4u

# Build debug APK
./gradlew assembleDebug

# Run on connected device
./gradlew installDebug
```

Requires Android Studio Ladybug (2024.2.1) or later with SDK 35.

## Licence

This project is open source. See [LICENSE](LICENSE) for details.

ARASAAC symbols are used under the Creative Commons BY-NC-SA licence.
Original ARASAAC symbols © Gobierno de Aragón.

## Contributing

See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.
