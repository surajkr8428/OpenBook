# OpenBooks - Open Source E-Book Reader

A modern, feature-rich e-book reader for Android built with Kotlin and Jetpack Compose, inspired by PocketBook Reader.

## Features

- **Multi-format support**: EPUB, PDF, MOBI, FB2, DJVU, TXT, CBZ/CBR (comics), audiobooks
- **Cloud sync**: Reading positions, bookmarks, notes, collections across devices
- **Text-to-speech**: Built-in TTS with customizable voice, speed, pitch
- **Highly customizable**: Fonts, themes (light/dark/sepia), margins, line height, animations
- **Library management**: Collections, shelves, search, auto-scan, favorites
- **Comics/manga mode**: Dedicated reading mode with page view and auto-rotation
- **Offline-first**: Works perfectly without internet
- **No ads, no tracking**: Privacy-focused

## Architecture

```
OpenBooks/
├── app/                    # Main application module
├── core/
│   ├── domain/             # Domain models, repositories interfaces, use cases
│   ├── data/               # Room database, repository implementations
│   ├── parser-epub/        # EPUB parsing (epublib)
│   ├── parser-pdf/         # PDF parsing (PDFBox + PdfRenderer)
│   ├── parser-comics/      # CBZ/CBR parsing (Apache Commons Compress + Junrar)
│   ├── tts/                # Text-to-speech engine
│   └── sync/               # Cloud sync (Firebase)
├── feature/
│   ├── library/            # Library UI (grid/list, collections, search)
│   ├── reader/             # Reader UI (EPUB, PDF, Comics renderers)
│   ├── settings/           # Settings UI
│   └── store/              # Book store UI
```

## Tech Stack

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room
- **Async**: Kotlin Coroutines + Flow
- **Image loading**: Coil
- **Networking**: Retrofit + OkHttp
- **Serialization**: Kotlinx Serialization
- **Sync**: Firebase Auth + Firestore + Storage
- **Parsers**: 
  - EPUB: epublib
  - PDF: PDFBox + Android PdfRenderer
  - Comics: Apache Commons Compress + Junrar

## Requirements

- Android Studio Ladybug (2024.2) or later
- JDK 17
- Android SDK 35
- Minimum SDK: 24 (Android 7.0)

## Building

```bash
# Clone the repository
git clone <repository-url>
cd OpenBooks

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

## Project Structure Details

### Core Domain (`core/domain`)
- **Models**: Book, Collection, Shelf, ReadingPosition, Bookmark, Note, ReadingSession, ReaderSettings, TTSConfig, SyncConfig
- **Repository Interfaces**: BookRepository, CollectionRepository, ShelfRepository, ReadingPositionRepository, BookmarkRepository, NoteRepository, ReadingSessionRepository, ReaderSettingsRepository
- **Parser Interfaces**: BookParser, EpubParser, PdfParser, ComicsParser, BookRenderer
- **TTS Interfaces**: TTSEngine, TTSRepository
- **Sync Interfaces**: SyncService, SyncRepository
- **Use Cases**: LibraryUseCases, ReaderUseCases, SettingsUseCases, SyncUseCases

### Core Data (`core/data`)
- Room database with DAOs for all entities
- Repository implementations using Room
- TypeConverters for complex types

### Parsers
- **EPUB**: Uses epublib for parsing EPUB 2/3, extracts metadata, chapters, TOC, cover
- **PDF**: Uses PDFBox for text extraction and metadata, Android PdfRenderer for page rendering
- **Comics**: Handles CBZ (ZIP) and CBR (RAR) archives, extracts images in natural sort order

### Features
- **Library**: Grid/list views, search, filtering, collections, shelves, recent books
- **Reader**: 
  - EPUB: HTML/CSS rendering with customizable styles
  - PDF: Page-based rendering with zoom/pan
  - Comics: Image-based with double-page spread support
- **Settings**: Reading preferences, TTS config, sync settings, storage management
- **Store**: Featured free books, categories, download management

## Sync Architecture

Uses Firebase for cloud synchronization:
- **Auth**: Email/password + Google Sign-In
- **Firestore**: User data (positions, bookmarks, notes, collections, shelves)
- **Storage**: Book files (optional backup)
- **Conflict resolution**: Last-write-wins, local-wins, remote-wins, manual

## Customization

### Themes
- Light, Dark, Sepia, System, Custom
- Customizable background/text colors
- Font family, size, weight, line height, letter spacing

### Reading Experience
- Page turn animations: None, Slide, Curl, Fade
- Scroll direction: Vertical, Horizontal
- Margins, paragraph spacing, justification
- Hyphenation, keep screen on

### TTS
- Voice selection (system voices)
- Speech rate: 0.5x - 2.0x
- Pitch: 0.5x - 2.0x
- Auto-scroll, text highlighting
- Skip footnotes, pause between paragraphs

## License

MIT License - See LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and lint
5. Submit a pull request

## Acknowledgments

- [epublib](https://github.com/psiegman/epublib) for EPUB parsing
- [PDFBox](https://pdfbox.apache.org/) for PDF processing
- [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) for archive handling
- [Junrar](https://github.com/junrar/junrar) for RAR extraction
- [Coil](https://coil-kt.github.io/coil/) for image loading
- [Firebase](https://firebase.google.com/) for cloud sync