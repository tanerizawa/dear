package com.psy.dear.data.repository

import com.psy.dear.data.network.api.ContentApiService
import com.psy.dear.data.network.dto.AudioTrackResponse
import com.psy.dear.domain.model.AudioTrack
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ContentRepositoryImplTest {

    private lateinit var api: ContentApiService
    private lateinit var repository: ContentRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        repository = ContentRepositoryImpl(api)
    }

    @Test
    fun `getMoodMusic emits tracks from api as domain models`() = runTest {
        val mood = "happy"
        val responses = listOf(
            AudioTrackResponse("1", "Track1", "url1"),
            AudioTrackResponse("2", "Track2", "url2")
        )
        coEvery { api.getMoodMusic(mood) } returns responses

        val result = repository.getMoodMusic(mood).first()

        val expected = listOf(
            AudioTrack("1", "Track1", "url1"),
            AudioTrack("2", "Track2", "url2")
        )

        assertEquals(expected, result)
    }
}
