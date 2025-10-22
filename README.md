# MagdyDiner (YumYumApp)

Last updated: 2025-10-21

# Short description

MagdyDiner (a.k.a. YumYumApp) is an Android app built with Kotlin and Jetpack Compose that displays meal categories and meal details using a clean-architecture structure. The app demonstrates local persistence using Room, user preferences via DataStore, network integration with Retrofit, background tasks with WorkManager, and a small example of Generative AI usage.

# Project snapshot

- Platform: Android (Kotlin + Jetpack Compose)
- Architecture: Clean Architecture (data / domain / presentation)
- Key libraries: Hilt (DI), Retrofit (HTTP), Room (local DB), Kotlin Coroutines & Flow, WorkManager, DataStore

# 1) Project Title

MagdyDiner — an example restaurant/meal browsing app built for learning and demonstration.

# 2) Problem description

Many meal/recipe apps surface aggregated content but mix UI, network, and persistence logic. MagdyDiner separates concerns following Clean Architecture so features are easier to test, maintain, and extend. Target users: students, Android developers, and instructors evaluating best practices for modern Android apps.

# 3) Features overview

- Browse meal categories (GET categories via Retrofit)
- View meals in a category and meal details
- Local caching with Room and DTO → Entity mappings
- User preferences persisted with DataStore (e.g., theme, last-selected category)
- Background tasks using WorkManager (periodic sync placeholder)
- Example integration with a GenAI model (prompt → generated recommendation or description)
- Navigation with Navigation 3 (single-Activity architecture)
- Unit-tested ViewModel example

# 4) System architecture

The project follows Clean Architecture boundaries:
- Presentation: Compose UI, ViewModels, navigation
- Domain: Use-cases and business rules (pure Kotlin, no Android framework)
- Data: Retrofit services, Room DAOs, repository implementations

### Diagram (simple):

Presentation -> Domain -> Data
                         ↑
                  Remote (Retrofit) / Local (Room)

# 5) Use case examples

- GetCategories: Fetch meal categories from remote API; store in local cache
- GetMealsByCategory: Fetch meals list for a category, fall back to cache when offline
- GetMealDetails: Fetch and map remote DTO to domain model

(Include a UML/use-case diagram screenshot in `docs/` or the repo root if desired.)

# 6) Technology stack

- Kotlin
- Jetpack Compose
- AndroidX Navigation (Navigation 3)
- Hilt (Dependency Injection)
- Retrofit + OkHttp
- Room
- DataStore (Preferences)
- WorkManager
- Kotlin Coroutines & Flow
- JUnit + MockK for unit tests

# 7) Package hierarchy (recommended)

Root package: `com.example.magdy.diner` (adjust to real package)
- core/ (common utilities, constants)
- data/
  - local/ (Room entities, DAOs, database)
  - remote/ (Retrofit services, network models)
  - repository/ (implementations)
- domain/
  - model/ (domain models)
  - repository/ (interfaces)
  - usecase/ (use case classes)
- presentation/
  - components/ (reusable Composables)
  - screens/ (feature screens)
  - navigation/
  - viewmodel/
- di/ (Hilt modules)

(Ensure the repo folders mirror the package structure.)

# 8) Screenshots / screen flow

Add 3–5 screenshots to `docs/screenshots/` or `assets/docs/` showing the home/category list, meal list, and meal detail screens. Example paths:
- `docs/screenshots/home.png`
- `docs/screenshots/category.png`
- `docs/screenshots/detail.png`

# 9) Setup & run instructions

### Prerequisites
- Android Studio (2022.3+ recommended) with Kotlin plugin
- JDK 11 or newer
- Connected device or emulator

### Clone, build and run
```bash
# from project root
git clone <repo-ssh-or-https>
cd MagdyDiner
./gradlew clean assembleDebug --console=plain
./gradlew :app:installDebug
```
Open in Android Studio: File → Open → select the project folder, allow Gradle sync.

Run the app from Android Studio or use `adb install` with the generated APK in `app/build/outputs/apk/debug`.

# 10) API reference

Base URL (example): https://www.themealdb.com/api/json/v1/1/
Key endpoints used:
- GET /categories.php — list meal categories
- GET /filter.php?c={category} — list meals in category
- GET /lookup.php?i={id} — meal details by id

(Verify paths with `data/remote/MealApiService.kt`.)

Authentication: Not required for TheMealDB example API. For other APIs, set API keys via `local.properties` or environment variables and never commit secrets.

# 11) Data models

- Remote DTOs: defined under `data/remote/model` (CategoryResponse, MealsResponse, MealDetailResponse)
- Room entities: under `data/local/entity` (CategoryEntity, MealEntity)
- Domain models: `domain/model` — mapped from DTOs or entities using extension mappers

### Example Kotlin data class (domain):
```kotlin
// domain/model/Meal.kt
data class Meal(
  val id: String,
  val name: String,
  val thumbnailUrl: String?,
  val instructions: String?
)
```

# 12) Testing (mandatory — ViewModel example)

There is at least one ViewModel unit test located under `app/src/test/java/...` demonstrating state transitions using JUnit and MockK. To run tests:
```bash
./gradlew test --console=plain
```
For ViewModel tests, mock `ApiUseCases` and verify `StateFlow` updates for Loading → Success → Error states.

# 13) Generative AI integration (mandatory)

Purpose: provide a generated meal description or suggestion based on ingredients or mood.
Sample prompt and usage (high level):
- Prompt: "Suggest a 3-course meal for a cozy dinner using chicken and rice."
- Implementation: small service module that calls a GenAI API (e.g., OpenAI/Gemini/Firebase AI). Keep keys in secure storage and do not commit them.

### Example (pseudocode):
```kotlin
// domain/usecase/GenerateMealDescriptionUseCase.kt
suspend operator fun invoke(prompt: String): Result<String>
```
Include the request/response code snippet and a screenshot of the generated output in `docs/genai/`.

# 14) Future enhancements / limitations

- Add offline synchronization (diffing + background sync)
- Add pagination for large category lists
- Improve error reporting and analytics
- Add more unit and integration tests

# 15) Wholeness / SCI connection (MIU context)

This project demonstrates the Science of Creative Intelligence (SCI) principle by structuring components to work harmoniously: separation of concerns encourages clarity and maintainability, fostering a learning environment aligned with creative problem solving.

# 16) Author / contributors

- Primary: Student Name (replace with your name)
- Repo: MagdyDiner (local path)
- Course: CS473 — Oct 2025 (example)

# Contributing

1. Fork the repo and create a feature branch from `main`.
2. Run `./gradlew build` and `./gradlew :app:lint` locally.
3. Include tests for new behaviors and open a PR with screenshots if UI changes.

# License

This repository is released under the MIT License. See `LICENSE` for details (or add a license file if missing).

# Contact

For questions about structure or tests, open an issue or contact the maintainer in the repo metadata.

-------------------------------
**Requirements coverage (summary)**
- Project Title: Done
- Problem Description: Done
- Features Overview: Done
- System Architecture: Done (textual diagram)
- Use Case Diagram: Placeholder described (add image in `docs/`)
- Technology Stack: Done
- Package Hierarchy: Done
- Screenshots / Screen Flow: Placeholders and paths suggested
- Setup Instructions: Done with commands
- API Reference: Done
- Data Models: Done with example
- Testing: Done (instructions)
- Generative AI Integration: Done (description and example)
- Future Enhancements: Done
- Wholeness / SCI Reflection: Done
- Author / Contributors: Done (placeholder)

If you want, I can:
- Add example screenshots into `docs/screenshots/` if you provide images.
- Add a small ViewModel unit test template under `app/src/test`.
- Rename package references to the actual app package if you confirm it.
