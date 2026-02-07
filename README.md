
# Notes Clean Architecture App

A Notes application built with **Clean Architecture** principles for Android, demonstrating proper separation of concerns and layered architecture.

## 🏗️ Architecture Overview

This project follows **Clean Architecture** with three distinct layers:

### 1. **Domain Layer** (Business Logic)
- **Pure Kotlin** - No Android dependencies
- Contains business models, repository interfaces, and use cases
- **Location**: `domain/`

#### Components:
- **`model/Note.kt`**: Domain entity representing a note
- **`repo/NotesRepository.kt`**: Repository interface (abstraction)
- **`usecase/`**: Use cases implementing business logic
  - `ObserveNotesUseCase`: Get all notes as a Flow
  - `UpsertNoteUseCase`: Create or update a note
  - `DeleteNoteUseCase`: Delete a note
  - `RefreshNotesUseCase`: Sync with remote server

### 2. **Data Layer** (Data Management)
- Implements repository interfaces from domain layer
- Manages data sources (local & remote)
- **Location**: `data/`

#### Components:
- **`local/`**: Room database for offline-first architecture
  - `NoteDto.kt`: Room entity for local storage
  - `NotesDao.kt`: Data Access Object
  - `AppDatabase.kt`: Room database configuration
  
- **`remote/`**: API service for server communication
  - `NotesApi.kt`: Retrofit API interface
  - `dto/NoteRemoteDto.kt`: Network data transfer object
  
- **`mapper/`**: Conversion between DTOs and domain models
  - `NoteMappers.kt`: Extension functions for data mapping
  
- **`repo/`**: Repository implementation
  - `NotesRepositoryImpl.kt`: Implements NotesRepository with offline-first approach

### 3. **Presentation Layer** (UI)
- Android-specific UI components using Jetpack Compose
- **Location**: `presentation/`

#### Components:
- **`notes/`**: Notes screen feature
  - `NotesScreen.kt`: Composable UI
  - `NotesViewModel.kt`: ViewModel managing UI state

## 🔄 Data Flow

```
UI (Compose) → ViewModel → Use Case → Repository Interface
                                            ↓
                                   Repository Implementation
                                            ↓
                                    ┌──────┴──────┐
                                    ↓             ↓
                              Local (Room)   Remote (API)
```

## 📦 Key Technologies

- **Kotlin** - Programming language
- **Jetpack Compose** - Modern Android UI
- **Hilt** - Dependency Injection
- **Room** - Local database (offline-first)
- **Retrofit** - REST API client
- **Kotlinx Serialization** - JSON serialization
- **Coroutines & Flow** - Asynchronous programming
- **Clean Architecture** - Separation of concerns

## 🎯 Clean Architecture Benefits

1. **Testability**: Each layer can be tested independently
2. **Maintainability**: Changes in one layer don't affect others
3. **Scalability**: Easy to add new features
4. **Separation of Concerns**: Clear responsibilities for each component
5. **Independence**: Domain layer is framework-agnostic

## 🔧 Dependency Rules

- **Domain** ← Data ← Presentation
- Inner layers know nothing about outer layers
- Dependencies point inward (Dependency Inversion Principle)

## 🚀 Features

- ✅ Offline-first architecture (local database as single source of truth)
- ✅ Remote API synchronization
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Reactive UI with Flow
- ✅ Error handling with graceful degradation
- ✅ Dependency injection with Hilt

## 📱 How It Works

1. **Local First**: All operations save to Room database first
2. **Background Sync**: Changes attempt to sync with remote API
3. **Graceful Degradation**: App works offline if API is unavailable
4. **Refresh**: Manual sync pulls latest data from server

## 🏛️ Project Structure

```
app/src/main/java/com/example/notes/
├── domain/                    # Business logic layer
│   ├── model/
│   │   └── Note.kt           # Domain entity
│   ├── repo/
│   │   └── NotesRepository.kt # Repository interface
│   └── usecase/              # Business use cases
│       ├── ObserveNotesUseCase.kt
│       ├── UpsertNoteUseCase.kt
│       ├── DeleteNoteUseCase.kt
│       ├── GetNoteByIdUseCase.kt
│       └── RefreshNotesUseCase.kt
├── data/                      # Data management layer
│   ├── local/                # Local data source (Room)
│   │   ├── NoteDto.kt        # Local entity
│   │   ├── NotesDao.kt       # DAO
│   │   └── AppDatabase.kt    # Database
│   ├── remote/               # Remote data source (API)
│   │   ├── NotesApi.kt       # Retrofit interface
│   │   └── dto/
│   │       └── NoteRemoteDto.kt # Network DTO
│   ├── mapper/               # Data mapping
│   │   └── NoteMappers.kt
│   └── repo/                 # Repository implementation
│       └── NotesRepositoryImpl.kt
├── presentation/              # UI layer (MVI Pattern)
│   ├── NotesScreen.kt        # Notes list Compose UI
│   ├── NotesViewModel.kt     # Notes list ViewModel (MVI)
│   ├── NotesIntent.kt        # User intents for notes list
│   ├── NotesEffect.kt        # Side effects for notes list
│   ├── NoteDetailScreen.kt   # Note detail Compose UI
│   ├── NoteDetailViewModel.kt # Note detail ViewModel (MVI)
│   ├── NoteDetailIntent.kt   # User intents for note detail
│   ├── NoteDetailEffect.kt   # Side effects for note detail
│   ├── NotesAppRoot.kt       # Navigation root
│   └── navigation/
│       └── Screen.kt         # Navigation routes
└── di/                        # Dependency Injection
    ├── AppModule.kt          # App-level dependencies
    └── UseCaseModule.kt      # Use case dependencies
```

## 🧪 Testing Strategy

- **Domain Layer**: Unit tests with pure Kotlin (no Android framework)
- **Data Layer**: Integration tests with fake repositories
- **Presentation Layer**: UI tests with Compose testing

---

**Built with Clean Architecture principles for maintainable, testable, and scalable Android development.** (Compose + Hilt + Flow/Coroutines + Room + Retrofit)

## How to run
1. Unzip.
2. Open the folder in **Android Studio** (File > Open).
3. Let Gradle sync (Android Studio will manage Gradle setup).
4. Click Run.

## What this project demonstrates
- Clean-ish layering inside a single module:
  - `domain` (models, repository contract, use cases)
  - `data` (Room + Retrofit + repository impl)
  - `presentation` (ViewModel + Compose UI)
- Offline-first display: UI observes Room via Flow.
- A manual "Refresh" action calls Retrofit and replaces local DB.

## Backend
The Retrofit base URL is set to `https://example.com/`.
Replace it with your API (or mock) in `AppModule.provideRetrofit()`.