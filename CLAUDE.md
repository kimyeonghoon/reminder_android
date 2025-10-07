# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Reminder is a native Android TODO application built with Kotlin and Jetpack Compose. The app uses MVVM architecture with Room Database for local persistence.

## Build and Run Commands

### Build the project
```bash
./gradlew build
```

### Run tests
```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest
```

### Install on device/emulator
```bash
./gradlew installDebug
```

### Clean build
```bash
./gradlew clean
```

## Architecture

### MVVM Pattern
- **Model**: `data/entity/ReminderEntity.kt` - Room entity with Priority enum
- **View**: `ui/screen/*` - Compose screens (HomeScreen, AddEditReminderScreen)
- **ViewModel**: `viewmodel/ReminderViewModel.kt` - State management with Kotlin Flow

### Data Layer
- **Room Database**: Singleton pattern in `ReminderDatabase.kt`
- **DAO**: `ReminderDao.kt` provides Flow-based queries for reactive updates
- **Repository**: `ReminderRepository.kt` abstracts data access
- **Type Converters**: `Converters.kt` handles LocalDateTime and Priority enum conversion

### Dependency Injection
Manual DI via `ReminderApplication.kt`:
- Database and Repository are lazy-initialized at application level
- ViewModel receives Repository through `ReminderViewModelFactory.kt`

### Navigation
Navigation Compose with two routes:
- `"home"` - Main list screen
- `"add_edit"` - Create/edit reminder screen

State for editing is managed via `selectedReminder` in MainActivity's ReminderApp composable.

### UI Components
- **ReminderCard**: Reusable card with checkbox, priority indicator, and delete button
- **Theme**: Material 3 with dynamic color support (Android 12+)
- Priority colors: High (Red), Medium (Orange), Low (Green)

## Key Technical Details

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin Version**: 1.9.20
- **Compose Compiler**: 1.5.4
- **KSP**: Used for Room annotation processing

## Database Schema

ReminderEntity fields:
- `id` (Long, auto-generated)
- `title` (String, required)
- `description` (String)
- `dueDateTime` (LocalDateTime?, nullable)
- `priority` (Priority enum: LOW, MEDIUM, HIGH)
- `category` (String)
- `isCompleted` (Boolean)
- `createdAt`, `updatedAt` (LocalDateTime)

## Development Notes

- Room queries return `Flow<List<ReminderEntity>>` for reactive UI updates
- StateFlow is used in ViewModel for Compose state management
- Search filtering is performed in-memory via ViewModel's `getFilteredReminders()`
- Reminder completion toggle updates `isCompleted` and `updatedAt` fields atomically

## Coding Conventions

### Naming Conventions

**Classes and Objects**
- Classes: `PascalCase` (e.g., `ReminderEntity`, `ReminderViewModel`)
- Interfaces: `PascalCase` with descriptive names (e.g., `ReminderDao`)
- Objects: `PascalCase` (e.g., `ReminderDatabase.Companion`)

**Functions and Variables**
- Functions: `camelCase` with verb prefix (e.g., `getReminderById`, `toggleReminderCompletion`)
- Variables: `camelCase` (e.g., `selectedReminder`, `isCompleted`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `DATABASE_NAME`)
- Private properties: prefix with underscore for backing properties (e.g., `_selectedReminder` for MutableStateFlow)

**Compose Functions**
- Composable functions: `PascalCase` (e.g., `HomeScreen`, `ReminderCard`)
- Composable parameters: `camelCase` with `on` prefix for callbacks (e.g., `onAddClick`, `onReminderClick`)

**Files**
- Match the primary class name (e.g., `ReminderEntity.kt`, `HomeScreen.kt`)
- Group related small classes in one file when appropriate (e.g., Priority enum in `ReminderEntity.kt`)

### Code Organization

**Package Structure**
```
com.reminder/
├── data/
│   ├── entity/      # Data models and enums
│   ├── dao/         # Room DAOs
│   ├── database/    # Database and converters
│   └── repository/  # Data repositories
├── ui/
│   ├── screen/      # Full screens
│   ├── components/  # Reusable UI components
│   └── theme/       # Theme-related files
└── viewmodel/       # ViewModels and factories
```

**File Order**
1. Package declaration
2. Imports (Android → Third-party → Java/Kotlin → Internal)
3. Class/Interface declaration
4. Companion object
5. Properties (public → private)
6. Init blocks
7. Constructors
8. Override functions
9. Public functions
10. Private functions

### Kotlin Style

**Nullability**
- Use nullable types (`?`) explicitly when needed
- Prefer safe calls (`?.`) over `!!` (avoid `!!` unless absolutely certain)
- Use `lateinit` for dependency injection, nullable types for optional data

**Immutability**
- Prefer `val` over `var`
- Use `data class` copy for updates (e.g., `reminder.copy(isCompleted = true)`)
- Use immutable collections when possible

**Flow and Coroutines**
- All database operations are suspend functions in DAO
- Use `viewModelScope` for ViewModel coroutines
- Expose Flow as StateFlow in ViewModel for Compose state management
- Use `stateIn()` with `SharingStarted.WhileSubscribed(5000)` for Flow to StateFlow conversion

### Compose Guidelines

**State Management**
- State hoisting: pass state and callbacks down from parent
- Use `remember` for UI state (e.g., text field values)
- Use ViewModel StateFlow for business logic state
- Collect StateFlow with `collectAsState()` in composables

**Composable Structure**
```kotlin
@Composable
fun ComponentName(
    data: DataType,              // Data parameters first
    modifier: Modifier = Modifier,  // Modifier with default
    onAction: () -> Unit         // Callbacks last
) {
    // Implementation
}
```

**Modifiers**
- Always accept `modifier: Modifier = Modifier` parameter
- Apply received modifier first: `modifier.then(localModifiers)`
- Use semantic modifiers before layout modifiers

### Room Database

**Entity Design**
- Always include `createdAt` and `updatedAt` timestamps
- Use `@PrimaryKey(autoGenerate = true)` for ID fields
- Provide default values for optional fields

**DAO Queries**
- Return `Flow<T>` for queries that need reactive updates
- Return `suspend fun` for single-shot operations (insert/update/delete)
- Use clear, descriptive query function names

### Comments and Documentation

**When to Comment**
- Complex business logic requiring explanation
- Non-obvious Room queries or Compose logic
- TODOs with context: `// TODO: Add notification scheduling`

**When NOT to Comment**
- Self-explanatory code
- Restating what the code does
- Commented-out code (remove instead)

### Architecture Rules

**ViewModel**
- Never pass Context to ViewModel
- No Android framework dependencies (except lifecycle)
- Expose immutable state (StateFlow) to UI
- All business logic lives here

**Repository**
- Single source of truth for data access
- Abstracts data sources (Room, network, etc.)
- Simple passthrough for this app, but prepared for expansion

**UI Layer**
- Screens should be stateless except for local UI state
- No direct database or repository access
- All business logic delegated to ViewModel

**Dependency Flow**
- UI → ViewModel → Repository → DAO → Database
- Never skip layers or create circular dependencies
