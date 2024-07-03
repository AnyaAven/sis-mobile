package com.example.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.example.api.ui.theme.APITheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceEvents = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(EventService::class.java)

        // Use the lifecycle scope to trigger the coroutine
        lifecycleScope.launch {
            try {
                val eventsResponse = serviceEvents.getEvents()
                Log.d("TAG_EVENTS_RESPONSE", eventsResponse.toString())
                val events = eventsResponse.results
                // Print the result of the network call to the Logcat

                Log.d("TAG_EVENTS", events.toString())
                // Update UI with the fetched data
                val composeView = findViewById<ComposeView>(R.id.compose_view)
                composeView.setContent {
                    APITheme {
                        MainContent(events)
                    }
                }
            } catch (e: Exception) {
                // Log the exception
                Log.e("TAG_", "Error fetching", e)
            }
        }
    }
}

data class Event(
    val title: String,
    val status: String
)

data class EventResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Event>
)

interface EventService {
    @Headers("Authorization: Token 1876a0415265139fc3cddca821049590f6bf9544")
    @GET("events")
    suspend fun getEvents(): EventResponse
}

@Composable
fun MainContent(events: List<Event>) {
    Column {
        EventList(events)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun EventList(events: List<Event>) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(events) { event ->
                EventItem(event)
            }
        }
    }
}


@Composable
fun EventItem(event: Event) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Title ${event.title}", style = MaterialTheme.typography.bodyLarge)
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Cyan) {
        Text(
            text = "Hi, my name is slim shady",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventItemPreview() {
    EventItem(Event("Event title", "published"))
}
