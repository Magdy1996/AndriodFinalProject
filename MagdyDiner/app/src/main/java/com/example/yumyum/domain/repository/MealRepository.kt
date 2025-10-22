package com.example.yumyum.domain.repository

import com.example.yumyum.common.Resource
import com.example.yumyum.domain.model.meals.CategoryResponse
import com.example.yumyum.domain.model.meals.MealDetailResponse
import com.example.yumyum.domain.model.meals.MealsResponse
import kotlinx.coroutines.flow.Flow

/**
 * MealRepository is an interface that defines the contract for accessing meal data.
 *
 * An interface is a contract that says: "Any class implementing me must have these methods."
 * Using an interface allows us to:
 * - Hide implementation details (whether data comes from API, database, cache, etc.)
 * - Easily test by creating mock implementations
 * - Switch implementations without changing the code that uses this interface
 *
 * This follows the Repository Pattern, which is a best practice in Android development.
 * The Repository Pattern separates data access logic from business logic.
 *
 * All methods are suspend functions because they perform network operations,
 * which should not block the main thread.
 * They return Flow<Resource<T>> to handle three states: Success, Error, and Loading.
 */
interface MealRepository {

    /**
     * Fetches all available meal categories from the data source.
     *
     * Example categories: "Seafood", "Vegetarian", "Dessert", etc.
     *
     * @return A Flow that emits Resource states containing CategoryResponse
     *         - Resource.Loading when data is being fetched
     *         - Resource.Success when categories are successfully retrieved
     *         - Resource.Error if something goes wrong
     */
    suspend fun getCategories(): Flow<Resource<CategoryResponse>>

    /**
     * Fetches all meals that belong to a specific category.
     *
     * Example: If strCategory is "Seafood", returns all seafood meals
     *
     * @param strCategory The name of the category to fetch meals for
     * @return A Flow that emits Resource states containing MealsResponse
     *         - Resource.Loading when data is being fetched
     *         - Resource.Success when meals are successfully retrieved
     *         - Resource.Error if something goes wrong
     */
    suspend fun getMealsByCategory(strCategory: String): Flow<Resource<MealsResponse>>

    /**
     * Fetches detailed information about a specific meal by its ID.
     *
     * The detailed response includes ingredients, measurements, and cooking instructions.
     *
     * @param idMeal The unique identifier of the meal to fetch
     * @return A Flow that emits Resource states containing MealDetailResponse
     *         - Resource.Loading when data is being fetched
     *         - Resource.Success when meal details are successfully retrieved
     *         - Resource.Error if something goes wrong
     */
    suspend fun getMealById(idMeal: String): Flow<Resource<MealDetailResponse>>

}