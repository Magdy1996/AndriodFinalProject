YumYum (MagdyDiner)
=====================

One-line description
--------------------
YumYum is an Android meal browsing and ordering app built with Kotlin and Jetpack Compose that lets users browse categories and meals from TheMealDB API, manage local orders (Room), and store user accounts locally. It follows Clean Architecture (data/domain/presentation), uses Hilt for DI and Retrofit for remote API calls.

Table of Contents
-----------------
- Project Title
- Problem Description
- Features Overview
- System Architecture
- Use Case / UI Flow
- Technology Stack
- Package Hierarchy
- Screenshots / Screen Flow
- Setup Instructions
- API Reference
- Data Models
- Testing
- Generative AI Integration
- Future Enhancements / Limitations
- Wholeness / SCI Connection
- Author / Contributors

1. Project Title
----------------
YumYum (MagdyDiner)

2. Problem Description
----------------------
Many casual users want a lightweight app to explore recipes and place simple local orders without a heavy e-commerce backend. YumYum provides:
- A searchable way to browse meal categories and meals (sourced from TheMealDB)
- Persistent local order management with Room for offline-first behavior
- Lightweight local authentication and user account storage
This is ideal for learners, demo use, and small local ordering workflows.

3. Features Overview
--------------------
Major functionalities implemented in the app:
- Remote API integration with Retrofit to fetch categories, meals by category, and meal details
- Local persistence with Room for orders and users (CRUD operations: create/update/delete/list)
- Dependency injection with Hilt
- Asynchronous flows with Kotlin Coroutines and Flow
- Navigation with Navigation-Compose
- Jetpack Compose UI
- Image loading with Coil and Lottie animations for UI polish
- Local authentication repository (simple email/username storage)
- Repository and use-case separation following Clean Architecture

4. System Architecture
----------------------
This project follows a Clean Architecture layering:
- Data layer: Retrofit API interfaces (`app/src/main/java/com/example/yumyum/data/remote/MealApiService.kt`), Room database (`AppDatabase`), DAOs (`OrderDao`, `UserDao`), concrete repositories (`data/repository/*`).
- Domain layer: repository interfaces and use-cases (`domain/repository/*`, `domain/use_case/*`) which encapsulate business rules.
- Presentation layer: Jetpack Compose screens and ViewModels (`presentation/*`) that expose UI state via StateFlow/SharedFlow and call use-cases.

Diagram (textual)
- UI (Compose) -> ViewModel -> UseCases -> Repositories -> (Retrofit API | Room DB)

5. Use Case / UI Flow
---------------------
Primary user flows:
- Browse categories -> select category -> list meals -> view meal details
- Add meals to local order (cart) -> update quantity / delete -> submit order (mark submitted)
- Create a local account (email/username), sign-in (local auth repository)

6. Technology Stack
-------------------
- Language: Kotlin
- UI: Jetpack Compose
- DI: Dagger Hilt
- Networking: Retrofit + Gson converter + OkHttp
- Persistence: Room (RoomDatabase, DAOs, Entities)
- Concurrency: Kotlin Coroutines + Flow
- Navigation: Navigation-Compose
- Image Loading: Coil (coil-compose, coil-svg)
- Animations: Lottie (lottie-compose)
- Testing: JUnit, Mockito (mockito-kotlin), coroutines-test

7. Package Hierarchy (high-level)
---------------------------------
Project packages (reflecting Clean Architecture):
- `com.example.yumyum.data` - remote API, local DB, repository implementations
  - `data/local` - `AppDatabase`, `OrderDao`, `UserDao`, Room entities
  - `data/remote` - `MealApiService` (Retrofit)
  - `data/repository` - `MealRepositoryImpl`, `OrderRepositoryImpl`
- `com.example.yumyum.domain` - models, repository interfaces, use-cases
  - `domain/repository` - `MealRepository`, `OrderRepository`
  - `domain/use_case` - `GetCategoriesUseCase`, `GetMealsUseCase`, `GetMealUseCase`, etc.
- `com.example.yumyum.presentation` - compose screens, ViewModels (e.g., `MealsListViewModel`, `MealDetailViewModel`, `OrderViewModel`, `LoginViewModel`)
- `com.example.yumyum.auth` - `AuthRepository`, `LocalAuthRepository`
- `com.example.yumyum.di` - `AppModule` (Hilt providers)
- `com.example.yumyum.common` - `Constants` (BASE_URL and other constants)

8. Screenshots / Screen Flow
---------------------------
Add 3–5 screenshots to the `presentation/` folder or the repository `docs/` folder and link them here. Example screen flow to capture:
1. Categories list
2. Meals list for a category
3. Meal detail screen
4. Orders/Cart screen
5. Login / Signup screen

(Placeholders — add actual screenshots when available.)

9. Setup Instructions
---------------------
Prerequisites:
- Android Studio (Arctic Fox or later recommended; project targets SDK 33)
- JDK 17 (the project config uses Java/Kotlin 17)

Steps to run locally:
1. Clone the repository:

```bash
git clone <REPO_URL> && cd MagdyDiner
```

2. Open the project in Android Studio: choose "Open" and select the project root (the folder containing `settings.gradle`).
3. Let Android Studio sync Gradle and download dependencies. Gradle wrapper is included.
4. Run on an emulator or physical device (API 24+ supported). The app requires INTERNET permission (already in manifest).

Notes:
- The app uses TheMealDB public API. No API key required for the default endpoints used.
- If you modify `minSdk` or tooling versions, update Gradle accordingly.

10. API Reference
-----------------
Base URL (defined in `com.example.yumyum.common.Constants`):
- https://www.themealdb.com/api/json/v1/1/

Endpoints used (as defined in `app/src/main/java/com/example/yumyum/data/remote/MealApiService.kt`):
- GET categories
  - Path: `categories.php`
  - Full example: https://www.themealdb.com/api/json/v1/1/categories.php
- GET meals by category
  - Path: `filter.php?c={categoryName}`
  - Example: https://www.themealdb.com/api/json/v1/1/filter.php?c=Seafood
- GET meal detail by id
  - Path: `lookup.php?i={idMeal}`
  - Example: https://www.themealdb.com/api/json/v1/1/lookup.php?i=52772

11. Data Models
---------------
Room entities (source files):
- `app/src/main/java/com/example/yumyum/data/local/entities/OrderEntity.kt`
  - Fields:
    - `id: Long` (PrimaryKey, autoGenerate)
    - `mealId: String`
    - `mealName: String`
    - `quantity: Int`
    - `timestamp: Long`
    - `isSubmitted: Boolean` (default false)
    - `userId: Long` (associate order with local user)

- `app/src/main/java/com/example/yumyum/data/local/entities/UserEntity.kt`
  - Fields:
    - `id: Long` (PrimaryKey, autoGenerate)
    - `email: String`
    - `username: String?`
    - `passwordHash: String?`
    - `displayName: String?`
    - `phoneNumber: String?`
    - `address: String?`
    - `createdAt: Long` (timestamp)

Remote DTOs / Domain models (examples in `domain.model.meals` — used by `MealApiService`):
- `CategoryResponse` — response wrapper returned by `categories.php` (contains categories list)
- `MealsResponse` — returned by `filter.php` (contains meal list)
- `MealDetailResponse` — returned by `lookup.php` (contains detailed meal info)

(See `domain/model/meals` packages for exact field definitions; include JSON examples from TheMealDB if desired.)

12. Testing (mandatory)
-----------------------
The project includes unit tests for utility and ViewModel logic. Example test file:
- `app/src/test/java/com/example/yumyum/util/PaymentValidatorsTest.kt` — contains unit tests validating payment field logic.

Repository and ViewModel tests are present in the `test/` tree (see `TEST_DOCUMENTATION.md` and `TEST_SUITE_SUMMARY.md` for the full test catalog). Notable tests:
- `MealRepositoryImplTest.kt` — repository behavior tests
- `MealRepositoryImpl` and other repository tests ensure correct Resource states are emitted when API calls succeed/fail.

How to run tests from terminal (optional):

```bash
# Run unit tests
./gradlew test
# Run android instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

13. Generative AI Integration (Mandatory)
----------------------------------------
This project does not integrate external Generative AI services (OpenAI, etc.) out of the box. If you plan to add GenAI features (e.g., recipe suggestions, generated meal names, or images), recommended approach:
- Add a server-side component or call a GenAI API from a secure backend (avoid embedding API keys in the app)
- Example prompt for recipe-suggestion use-case:
  "Suggest 5 side dishes that pair well with <meal name> and explain why."
- Example architecture: Compose app -> secure backend -> OpenAI API -> app

If you want, I can add an example GenAI integration using a small bounded interface and sample prompt + code snippet.

14. Future Enhancements / Limitations
------------------------------------
- Add remote order submission / backend to persist user orders across devices
- Replace local auth with secure OAuth or Firebase Authentication
- Add DataStore preferences for user settings
- Add WorkManager jobs for background sync of submitted orders
- Improve accessibility and internationalization (i18n)

15. Wholeness / SCI Connection (MIU Context)
-------------------------------------------
This project illustrates the Science of Creative Intelligence (SCI) principle of integral design: combining creative UI (Compose), disciplined architecture (Clean Architecture), and technological integration (Room + Retrofit + DI) to produce a practical, user-centered learning application that balances structure and creativity.

16. Author / Contributors
-------------------------
- Student: (Your Name)
- Project: MagdyDiner / YumYum
- Course: (e.g., CS473 - Oct 2025)

Appendix: Useful file locations
-------------------------------
- `app/build.gradle` — project dependencies and Compose setup
- `app/src/main/AndroidManifest.xml` — permissions and `MealApplication` / `MainActivity`
- `app/src/main/java/com/example/yumyum/di/AppModule.kt` — Hilt providers (Retrofit, Room, Repositories)
- `app/src/main/java/com/example/yumyum/data/remote/MealApiService.kt` — Retrofit interface and endpoints
- `app/src/main/java/com/example/yumyum/data/local` — Room `AppDatabase`, DAOs, Entities
- `TEST_DOCUMENTATION.md`, `TEST_SUITE_SUMMARY.md` — test inventory and details

If you'd like, I can:
- Add in-repo screenshots under `docs/` and link them in this README
- Generate a small architecture diagram PNG and add it to the repo
- Add a sample GenAI integration snippet and a ViewModel test for it

---

Requirements coverage (quick mapping):
- Project Title: Done
- Problem Description: Done
- Features Overview: Done
- System Architecture: Done (textual + mapping)
- Use Case Diagram: Placeholder (can add image)
- Technology Stack: Done
- Package Hierarchy: Done
- Screenshots: Placeholder
- Setup Instructions: Done
- API Reference: Done (BASE_URL + endpoints)
- Data Models: Done (Room entities + DTO mentions)
- Testing: Done (references and how to run)
- Generative AI Integration: Not implemented — described how to add
- Future Enhancements: Done
- Wholeness/SCI: Done
- Author/Contributors: Placeholder (please provide your name/ID)

