package com.example.yumyum.presentation.orders

import com.example.yumyum.domain.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    @After
    fun tearDown() {
        // Reset Main dispatcher after each test
        Dispatchers.resetMain()
    }

    @Test
    fun addOrUpdateOrder_emitsOrderPlaced_whenRepositoryReturnsNonNegative() = runTest {
        // Arrange: create a mock repository and stub the suspend upsertOrder to return a positive id
        val repository: OrderRepository = mock()
        whenever(repository.upsertOrder(any())).thenReturn(1L)

        // Use the test scheduler's dispatcher as Main so ViewModel coroutines run on the test dispatcher
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        // Create the ViewModel (it uses viewModelScope which will use Dispatchers.Main)
        val viewModel = OrderViewModel(repository)

        // Start collecting the SharedFlow before invoking the action (SharedFlow replay=0)
        val deferred = async { viewModel.orderPlaced.first() }

        // Act: call the function under test
        viewModel.addOrUpdateOrder("meal1", "Meal 1", 2)

        // Advance the scheduler to allow launched coroutines to execute
        // Use the testScheduler provided by runTest to advance virtual time until all tasks complete
        testScheduler.advanceUntilIdle()

        // Assert: verify an event was emitted and it is true
        val emitted = deferred.await()
        assertTrue(emitted)
    }
}
