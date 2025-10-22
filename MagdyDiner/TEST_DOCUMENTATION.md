# YumYumApp Test Suite Documentation

## Overview

This document provides a comprehensive guide to the professional test cases created for the YumYumApp project. The test suite covers unit tests, integration tests, and UI tests across all layers of the application.

## Test Structure

```
app/src/
├── test/                                    # Unit Tests (JVM)
│   └── java/com/example/yumyum/
│       ├── common/
│       │   ├── ConstantsTest.kt           # Tests for app constants
│       │   └── ResourceTest.kt            # Tests for Resource wrapper
│       ├── data/repository/
│       │   └── MealRepositoryImplTest.kt  # Repository layer tests
│       ├── domain/use_case/
│       │   └── UseCasesTest.kt            # Use case tests
│       ├── presentation/
│       │   ├── category_list/
│       │   │   ├── CategoryListViewModelTest.kt
│       │   │   └── CategoryListStateTest.kt
│       │   ├── meals_list/
│       │   │   ├── MealsListViewModelTest.kt
│       │   │   └── MealsListStateTest.kt
│       │   ├── meal_detail/
│       │   │   ├── MealDetailViewModelTest.kt
│       │   │   └── MealDetailStateTest.kt
│       │   └── integration/
│       │       └── MealDataFlowIntegrationTest.kt
│
└── androidTest/                            # Instrumented Tests (Android)
    └── java/com/example/yumyum/
        ├── presentation/
        │   ├── navigation/
        │   │   └── NavigationTest.kt
        │   ├── category_list/components/
        │   │   └── CategoryItemTest.kt
        │   ├── meals_list/components/
        │   │   └── MealItemTest.kt
        │   └── custom_components/
        │       └── CustomComponentsTest.kt
```

## Test Categories

### 1. Unit Tests (test/ folder)

Run with: `./gradlew test`

#### Common Tests
- **ConstantsTest.kt** - Validates all constant values and their format
- **ResourceTest.kt** - Tests the Resource sealed class (Loading, Success, Error states)

#### Data Layer Tests
- **MealRepositoryImplTest.kt** - Tests repository delegates to API correctly, handles errors, emits proper Resource states

#### Domain Layer Tests
- **UseCasesTest.kt** - Tests use case delegation and data flow through domain layer

#### Presentation Layer Tests

**CategoryListViewModel & State:**
- Tests initial state
- Tests successful data loading
- Tests error handling
- Tests empty list handling
- Tests state transitions

**MealsListViewModel & State:**
- Tests category parameter retrieval
- Tests meals fetching
- Tests SavedStateHandle integration
- Tests null safety

**MealDetailViewModel & State:**
- Tests meal ID parameter retrieval
- Tests detailed meal fetching
- Tests ingredient data preservation
- Tests instruction formatting

### 2. Instrumented Tests (androidTest/ folder)

Run with: `./gradlew connectedAndroidTest`

Requires: Android emulator or connected device

#### Navigation Tests
- Tests screen route definitions
- Tests route uniqueness
- Tests parameter passing in routes

#### UI Component Tests
- **CategoryItemTest.kt** - Tests category item rendering and click handling
- **MealItemTest.kt** - Tests meal item display and interaction
- **CustomComponentsTest.kt** - Tests reusable custom components

## Running Tests

### Run All Tests
```bash
./gradlew test                    # Unit tests only
./gradlew connectedAndroidTest    # Instrumented tests only
./gradlew test connectedAndroidTest # All tests
```

### Run Specific Test Class
```bash
./gradlew test --tests CategoryListViewModelTest
./gradlew test --tests "*.MealRepository*"
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTest --coverage
```

### Run Tests with Verbose Output
```bash
./gradlew test --info
```

## Test Coverage Summary

### Unit Tests (13 test files)

1. **ConstantsTest.kt** (7 tests)
   - API constants validation
   - Navigation constants
   - Firebase constants
   - UI strings
   - URL format validation

2. **ResourceTest.kt** (6 tests)
   - Loading state creation
   - Success data wrapping
   - Error message storage
   - Resource type differentiation

3. **MealRepositoryImplTest.kt** (7 tests)
   - Categories fetching
   - API exception handling
   - Meals by category
   - Meal details fetching
   - Multiple independent calls

4. **UseCasesTest.kt** (9 tests)
   - GetCategoriesUseCase (2 tests)
   - GetMealsUseCase (2 tests)
   - GetMealUseCase (3 tests)
   - Error propagation
   - Data preservation

5. **CategoryListViewModelTest.kt** (5 tests)
   - Success state
   - Error handling
   - Loading state
   - Empty list handling
   - Initial state

6. **CategoryListStateTest.kt** (6 tests)
   - Default values
   - Custom values
   - State copying
   - State transitions
   - Multiple transitions

7. **MealsListViewModelTest.kt** (6 tests)
   - Meals fetching
   - Error handling
   - Empty responses
   - Null safety
   - Loading states

8. **MealsListStateTest.kt** (4 tests)
   - Default values
   - Custom meals
   - Loading transitions
   - Error states

9. **MealDetailViewModelTest.kt** (7 tests)
   - Meal details fetching
   - Error handling
   - Ingredient data
   - Null safety
   - Instructions preservation

10. **MealDetailStateTest.kt** (4 tests)
    - Default values
    - Meal detail storage
    - Loading transitions
    - Error states

11. **MealDataFlowIntegrationTest.kt** (3 tests)
    - Complete flow from categories to meals
    - Error handling in flow
    - Sequential operations consistency

### Instrumented Tests (4 test files)

1. **NavigationTest.kt** (5 tests)
   - Route definitions
   - Route uniqueness
   - Category parameters
   - Meal ID parameters
   - Special characters handling

2. **CategoryItemTest.kt** (4 tests)
   - Category name display
   - Item clickability
   - Callback parameters
   - Category name reception

3. **MealItemTest.kt** (4 tests)
   - Meal name display
   - Item clickability
   - Meal ID callback
   - Long name handling

4. **CustomComponentsTest.kt** (4 tests)
   - HeadingTextComponent
   - TextTitleMealInfo
   - ProgressBar
   - TextTitleComponent

**Total: 77 Professional Test Cases**

## Test Dependencies

The tests use the following libraries:

```gradle
// Testing Libraries
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito.kotlin:mockito-kotlin:4.0.0'
testImplementation 'org.mockito:mockito-core:4.8.1'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1'

androidTestImplementation 'androidx.test.ext:junit:1.1.5'
androidTestImplementation 'androidx.compose.ui:ui-test-junit4:1.4.3'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
```

## Test Best Practices Used

### 1. Arrange-Act-Assert Pattern
All tests follow AAA pattern for clarity:
```kotlin
@Test
fun `test_name_describes_behavior`() {
    // Arrange - Setup test data
    val data = setupData()
    
    // Act - Perform action
    val result = performAction(data)
    
    // Assert - Verify result
    assert(result.isCorrect())
}
```

### 2. Mocking Strategy
- Mock only external dependencies (API, database)
- Use real implementations for internal layers
- Use MockitoAnnotations for consistent setup

### 3. Test Naming Convention
- Descriptive names using backticks
- Format: `test_what_is_being_tested_expected_outcome`
- Easily readable by non-developers

### 4. State Management Testing
- Test state initialization
- Test all state transitions
- Verify state immutability

### 5. Error Handling
- Test success paths
- Test error paths
- Test edge cases (empty data, nulls)

## Common Test Scenarios

### Testing ViewModel State Changes
```kotlin
@Test
fun `test_state_transitions`() = runTest {
    // Mock repository
    whenever(mockRepository.getData()).thenReturn(
        flowOf(
            Resource.Loading(),
            Resource.Success(data)
        )
    )
    
    // Create ViewModel
    viewModel = TestedViewModel(mockRepository)
    advanceUntilIdle()
    
    // Verify final state
    assert(viewModel.state.value.data == data)
}
```

### Testing Error Propagation
```kotlin
@Test
fun `test_error_handling`() = runTest {
    // Setup mock to throw exception
    whenever(mockApi.getData())
        .thenThrow(Exception("API Error"))
    
    // Verify error propagation
    val results = repository.getData().toList()
    assert(results.last() is Resource.Error)
}
```

### Testing UI Components
```kotlin
@Test
fun `test_ui_component_behavior`() {
    composeTestRule.setContent {
        TestComponent(callback = { clickedData = it })
    }
    
    composeTestRule.onNodeWithText("Label").performClick()
    
    assert(clickedData == expectedValue)
}
```

## Continuous Integration

For CI/CD pipelines, add to your build script:

```bash
# Run all tests
./gradlew testDebugUnitTest

# Generate test report
./gradlew testDebugUnitTest --info

# Tests with coverage report
./gradlew testDebugUnitTestCoverage
```

## Troubleshooting

### Tests Fail on CI but Pass Locally
- Ensure test data is independent
- Don't rely on test execution order
- Use `runTest` for coroutine tests

### Mock Objects Not Working
- Initialize with `MockitoAnnotations.openMocks(this)`
- Use `whenever` instead of `when`
- Verify mock setup before test

### UI Tests Failing
- Ensure device/emulator is running
- Check screen resolution compatibility
- Use `waitForIdle()` for async operations

## Future Test Additions

Recommended additional tests:
1. API service mock tests
2. Database/Cache layer tests
3. E2E user journey tests
4. Performance tests
5. Accessibility tests

## Resources

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/)
- [Compose Testing Guide](https://developer.android.com/jetpack/compose/testing)
- [Coroutines Testing](https://kotlinlang.org/docs/coroutine-testing.html)

