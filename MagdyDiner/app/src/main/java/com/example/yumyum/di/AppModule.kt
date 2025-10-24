package com.example.yumyum.di


import android.content.Context
import androidx.room.Room
import com.example.yumyum.common.Constants.BASE_URL
import com.example.yumyum.data.remote.MealApiService
import com.example.yumyum.data.repository.MealRepositoryImpl
import com.example.yumyum.data.local.AppDatabase
import com.example.yumyum.data.local.OrderDao
import com.example.yumyum.data.repository.OrderRepositoryImpl
import com.example.yumyum.domain.repository.MealRepository
import com.example.yumyum.domain.repository.OrderRepository
import com.example.yumyum.domain.use_case.ApiUseCases
import com.example.yumyum.domain.use_case.GetCategoriesUseCase
import com.example.yumyum.domain.use_case.GetMealUseCase
import com.example.yumyum.domain.use_case.GetMealsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import com.example.yumyum.data.local.UserDao
import com.example.yumyum.auth.AuthRepository
import com.example.yumyum.auth.LocalAuthRepository
import com.example.yumyum.data.preferences.UserPreferences


/**
 * AppModule is a Dagger Hilt module that provides singleton instances of key dependencies.
 *
 * The @Module annotation tells Hilt that this class contains dependency definitions.
 * The @InstallIn(SingletonComponent::class) annotation means all provided objects exist
 * for the entire lifetime of the application (not recreated each time they're needed).
 *
 * This follows the Dependency Injection (DI) pattern which makes the code:
 * - Easier to test (you can provide mock objects for testing)
 * - More modular (dependencies are defined in one place)
 * - More flexible (easy to swap implementations)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Creates and provides a singleton Retrofit instance for making HTTP API requests.
     * Retrofit is a library that converts HTTP requests into Kotlin function calls.
     *
     * @return A configured Retrofit instance that can make API calls to the meal database
     */
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        // Create a Retrofit builder and configure it with the API's base URL
        return Retrofit.Builder()
            // Set the base URL that all API requests will be relative to
            .baseUrl(BASE_URL)
            // Add a JSON converter so Retrofit can automatically convert JSON responses
            // to Kotlin objects (serialization/deserialization)
            .addConverterFactory(GsonConverterFactory.create())
            // Build and return the configured Retrofit instance
            .build()
    }

    /**
     * Creates and provides a singleton MealApiService interface instance.
     * This is the actual service that makes the HTTP calls to fetch meal data.
     *
     * @param retrofit The Retrofit instance needed to create the service
     * @return A MealApiService instance that can make API calls
     */
    @Provides
    @Singleton
    fun providesMealService(retrofit: Retrofit): MealApiService {
        // Use Retrofit to create an implementation of the MealApiService interface
        // Hilt automatically injects the retrofit parameter from provideRetrofit()
        return retrofit.create(MealApiService::class.java)
    }

    /**
     * Creates and provides a singleton MealRepository instance.
     * A repository acts as a middle layer between the UI and data sources,
     * handling all data fetching and caching logic.
     *
     * @param api The MealApiService needed to fetch data from the API
     * @return A MealRepository instance that encapsulates data operations
     */
    @Provides
    @Singleton
    fun provideMealRepository(api: MealApiService): MealRepository {
        // Create a concrete implementation of the MealRepository interface
        // The api parameter is automatically injected from providesMealService()
        return MealRepositoryImpl(api)
    }

    /**
     * Creates and provides a singleton ApiUseCases collection.
     * Use cases contain business logic and coordinate between the repository and UI.
     * Grouping multiple use cases in one object makes them easy to inject together.
     *
     * @param repository The MealRepository needed to fetch data for each use case
     * @return An ApiUseCases object containing three use cases for fetching meal data
     */
    @Provides
    @Singleton
    fun provideApiUseCases(repository: MealRepository): ApiUseCases {
        // Create three separate use cases, each responsible for a specific API operation
        return ApiUseCases(
            // Use case for fetching all available meal categories
            getCategoriesUseCase = GetCategoriesUseCase(repository),
            // Use case for fetching meals that belong to a specific category
            getMealsUseCase = GetMealsUseCase(repository),
            // Use case for fetching detailed information about a specific meal
            getMealUseCase = GetMealUseCase(repository)
        )
    }

    // --- Room database providers for Orders feature ---
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        Log.d("AppModule", "Creating AppDatabase 'app_db'")
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context, userDao: UserDao): AuthRepository {
        return LocalAuthRepository(context, userDao)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(dao: OrderDao, @ApplicationContext context: Context, authRepository: AuthRepository): OrderRepository = OrderRepositoryImpl(dao, context, authRepository)

    // Provide UserPreferences (DataStore wrapper) as a singleton so ViewModels can inject it
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences.getInstance(context)
    }

}