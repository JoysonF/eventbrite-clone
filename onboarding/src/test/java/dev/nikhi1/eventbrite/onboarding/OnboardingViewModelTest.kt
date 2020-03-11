package dev.nikhi1.eventbrite.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dev.nikhi1.eventbrite.core.Result
import dev.nikhi1.eventbrite.core.UIState
import dev.nikhi1.eventbrite.onboarding.data.DataRepository
import dev.nikhi1.eventbrite.test_shared.utils.MainCoroutineRule
import dev.nikhi1.eventbrite.test_shared.utils.getOrAwaitValue
import dev.nikhi1.eventbrite.test_shared.utils.runBlocking
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class OnboardingViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesRule = MainCoroutineRule()

    lateinit var viewModel: OnboardingViewModel

    lateinit var dataRepository: DataRepository

    @Before
    fun setup() {
        dataRepository = mockk()
        viewModel = OnboardingViewModel(dataRepository)
    }

    @Test
    fun `fetch topics or interests`() {
        coroutinesRule.runBlocking {
            val uiState = OnboardingViewState(uiState = UIState.Content, categories = listOf(TestData.subcategory))
            coEvery {  dataRepository.getTopics(any()) } returns Result.Success(TestData.subCategoryResponse)
            viewModel.fetchTopics()
            assertEquals(uiState, viewModel.viewState.getOrAwaitValue())
        }
    }

    @Test
    fun `empty topics or interests`() {
        coroutinesRule.runBlocking {
            val uiState = OnboardingViewState(uiState = UIState.Empty)
            coEvery {  dataRepository.getTopics(any()) } returns Result.Success(TestData.emptySubCategoryResponse)
            viewModel.fetchTopics()
            assertEquals(uiState, viewModel.viewState.getOrAwaitValue())
        }
    }

    @Test
    fun `failed fetch topics or interests`() {
        coroutinesRule.runBlocking {
            val uiState = OnboardingViewState(uiState = UIState.Error)
            coEvery {  dataRepository.getTopics(any()) } returns Result.Failure(IOException())
            viewModel.fetchTopics()
            assertEquals(uiState, viewModel.viewState.getOrAwaitValue())
        }
    }
}