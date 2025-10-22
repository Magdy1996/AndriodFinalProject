YumYumApp — Developer Onboarding Guide

Last updated: 2025-10-21

Purpose
-------
This document helps new contributors understand the project structure, the
primary responsibilities of each module, and how to make common changes.
It also gives step-by-step commands to build, run and test the app locally.

What this project is
--------------------
- Android mobile app built with Jetpack Compose, Hilt for DI, Retrofit for
  network calls, and Kotlin coroutines + Flow for async streams.
- The app fetches meal categories and meal details from TheMealDB and
  renders lists and detail screens.

Quick architecture summary
--------------------------
- Presentation (UI): Jetpack Compose. Screens are in `app/src/main/java/com/example/yumyum/presentation`.
- Domain: Use-cases and repository interfaces live under `domain/`.
- Data: Retrofit API definitions and repository implementations under `data/`.
- DI: `di/AppModule.kt` configures Retrofit, repository and use-case bindings with Hilt.
- Models/DTOs: `domain/model/meals/` contains response DTOs and domain models.
- Theme: `ui/theme/` contains color tokens, typography and shapes.
- App entry points: `MainActivity.kt` and `MealApplication.kt`.

Project layout (quick map)
-------------------------
app/
  src/main/java/com/example/yumyum/
    common/
      - `Constants.kt`       — API base URL and other global constants
      - `Resource.kt`        — Result wrapper (Loading/Success/Error)

    data/
      - `remote/MealApiService.kt`  — Retrofit endpoints
      - `repository/MealRepositoryImpl.kt` — Implementation of repo

    domain/
      - `repository/MealRepository.kt`   — Repository contract (Flow<Resource<...>>)
      - `use_case/`                       — Domain use-cases aggregated in `ApiUseCases.kt`
      - `model/meals/`                    — DTOs (CategoryResponse, MealsResponse, MealDetailResponse) and models

    presentation/
      - `screens/`                        — Compose screen composables
      - `navigation/`                     — `Screen.kt` routes and `YumYumNavigation.kt` NavHost
      - `meals_list/`, `category_list/`, `meal_detail/` — ViewModels, State classes, components
      - `CustomComponents.kt`             — small reusable composables (headings, progress, logo)

    di/
      - `AppModule.kt`                    — Hilt providers for Retrofit, repo and use-cases

    ui/theme/
      - `Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`

Key files and responsibilities
------------------------------
- `MealApiService.kt` — Defines endpoints used by the app (getCategories(), getMealsByCategory(), getMealById()).
- `MealRepository.kt` — Interface: methods return `Flow<Resource<T>>`.
- `MealRepositoryImpl.kt` — Calls the Retrofit API and emits `Resource` values (Loading/Success/Error) using `flow {}`.
- `ApiUseCases.kt` + `Get*UseCase.kt` — Thin domain layer wrappers around repository calls. Injected into ViewModels.
- `AppModule.kt` — Hilt bindings: Retrofit instance, MealApiService, MealRepositoryImpl and ApiUseCases.
- `YumYumNavigation.kt` / `Screen.kt` — Central navigation graph and route definitions. Add routes here when adding screens.
- `*ViewModel.kt` files — Convert `Resource` emissions into UI `State` and expose `StateFlow` that screens collect.

Navigation tips and examples
----------------------------
Where to add a screen
1. Create a composable in `presentation/screens/` (or a subpackage if it fits a feature).
2. Add a route in `presentation/navigation/Screen.kt` as a new `object`.
3. Register the route in `YumYumNavigation.kt` inside the `NavHost`.
4. If the screen needs an argument (e.g. meal id), use a placeholder in the route string and add a corresponding `navArgument` with type: e.g.
   - Route definition: `"${Screen.MealDetailScreen.route}/{idMeal}"`
   - navArgument: `navArgument("idMeal") { type = NavType.StringType }`
5. Create a ViewModel (if needed) and expose a `StateFlow` for the UI. Inject the use-cases into the ViewModel via Hilt.

Example: adding a detail screen route
- In `Screen.kt`:
  - object MealDetailScreen: Screen("meal_detail_screen")
- In `YumYumNavigation.kt`:
  - composable(route = "${Screen.MealDetailScreen.route}/{idMeal}", arguments = listOf(navArgument("idMeal") { type = NavType.StringType })) { backStackEntry -> ... }
- In the composable: read the arg via `navBackStackEntry.arguments?.getString("idMeal")` and pass it to the ViewModel or call the use-case.

How ViewModels and Use-cases connect
-----------------------------------
- ViewModel receives `ApiUseCases` via Hilt injection (see `AppModule` which provides `ApiUseCases`).
- On init, ViewModel will typically call a suspend operator function on a use-case and collect a Flow with `.onEach { ... }.launchIn(viewModelScope)`.
- Convert `Resource.Loading/Success/Error` into a UI `State` data class and expose it via an immutable `StateFlow`.

Common tasks — step-by-step
---------------------------
1) Debugging a Retrofit API call
- Confirm `BASE_URL` in `common/Constants.kt`.
- Check `MealApiService.kt` endpoint paths match API docs.
- Add logging interceptor (if needed) in a debug-only network module.

2) Adding a dependency
- Add the library to `build.gradle` (app-level or root as appropriate).
- Sync Gradle in Android Studio.
- If the dependency requires ProGuard or R8 rules, update `proguard-rules.pro`.

3) Fixing a Hilt injection issue
- Ensure the ViewModel is annotated `@HiltViewModel`.
- Ensure `MealApplication` has `@HiltAndroidApp`.
- Check the provided binding is annotated with `@Provides` and included in a Hilt `@Module` installed in `SingletonComponent` (see `AppModule.kt`).

Build, run and test locally
--------------------------
Prerequisites
- Android Studio (recommended) or JDK + SDK + Gradle installed
- An emulator or physical device connected

Assemble a debug APK from project root:
```bash
./gradlew :app:assembleDebug --console=plain
```

Install to a connected device/emulator:
```bash
./gradlew :app:installDebug
```

Run lint checks:
```bash
./gradlew :app:lint --console=plain
```

Run the full build (assemble + unit tests):
```bash
./gradlew build --console=plain
```

Open in Android Studio
- File -> Open -> select the project folder.
- Let Gradle sync and download dependencies.
- Run using the Run toolbar or the app configuration.

Testing strategies
------------------
- Unit tests: put them under `app/src/test/java` and use standard JUnit + MockK or Mockito.
- UI tests: use `app/src/androidTest/java` with Espresso or Compose UI tests.
- For ViewModel testing, mock `ApiUseCases` and test state transitions for `Loading/Success/Error`.

Code style and conventions
--------------------------
- Keep composables small and focused; prefer small reusable components in `presentation/`.
- Use `StateFlow` + immutable state data classes for UI state.
- Keep repository methods as cold Flows that emit `Resource<T>`.
- Prefer constructor injection with Hilt for ViewModels and repositories.

Searching and navigating the codebase
------------------------------------
- Common entry points:
  - UI navigation: `presentation/navigation/YumYumNavigation.kt`
  - Network: `data/remote/MealApiService.kt`
  - DI bindings: `di/AppModule.kt`
- If you need to find a symbol quickly in Android Studio use "Navigate -> Class/File/Symbol".
- From the terminal you can use ripgrep (if installed):
```bash
rg "getMealById|MealRepository|ApiUseCases" -n
```

Troubleshooting checklist (common gotchas)
-----------------------------------------
- Missing resources / fonts: run Gradle sync, ensure `res/` and `font/` resources are present.
- Network calls fail on device: check device network, emulator proxy, or API availability.
- Hilt errors at build time: confirm annotation processing is enabled and the project has the right hilt-gradle-plugin configuration.
- Retrofit JSON mapping errors: check DTO fields names match the API JSON keys.

Contributing guide (brief)
--------------------------
1. Fork the repo and create a feature branch from `main`.
2. Make small, focused commits with clear messages.
3. Run `./gradlew build` and `./gradlew :app:lint` locally before opening a PR.
4. Add tests for new functionality where practical.
5. Open a PR describing the change, include screenshots for UI changes and mention any migration or API impacts.

Maintainer notes
----------------
- Primary domain logic lives in `domain/use_case` and `data/repository` — keep business rules there, not in UI code.
- Small components are grouped in `presentation/*/components` so they can be reused across screens.

If you want next
----------------
- I can add a CONTRIBUTING.md with a PR template and checklist.
- I can add example unit tests for one ViewModel as a template for testing patterns.
- I can add a short dev script (Makefile or bash script) to automate common tasks (assemble, lint, install).

Contact
-------
If you have questions about a specific area of the codebase, tell me which file or feature and I will give a targeted guide or make a small change to demonstrate.

