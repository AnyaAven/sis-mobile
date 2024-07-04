package com.example.api

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Empty.painter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.api.ui.theme.APITheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {

    private lateinit var serviceEvents: EventService
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Retrofit service for events
        serviceEvents = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(EventService::class.java)

        // Observe ViewModel or handle login logic here
        handleLogin()
    }

    private fun handleLogin() {
        val viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            APITheme {
                LoginScreen(viewModel)
            }
        }

        viewModel.loginState.observe(this, Observer { loginState ->
            when (loginState) {
                is LoginViewModel.LoginState.Loading -> {
                    // Show loading indicator or handle UI state
                }
                is LoginViewModel.LoginState.Success -> {
                    val token = loginState.token
                    fetchEvents(token)
                }
                is LoginViewModel.LoginState.Error -> {

                    Log.e("TAG_HANDLE_LOGIN_ERROR", "Login failed: ${loginState.error.message}")
                }
            }
        })
    }

    private fun fetchEvents(token: String) {
        lifecycleScope.launch {
            try {
                val eventsResponse = serviceEvents.getEvents("Token $token")
                val events = eventsResponse.results
                Log.d("TAG_", "Events: $events")
                // Update UI with the fetched data
                val composeView = findViewById<ComposeView>(R.id.compose_view)
                composeView.setContent {
                    APITheme {
                        MainContent(events)
                    }
                }
            } catch (e: Exception) {

                Log.e("TAG_fetchEvents", "Error fetching events", e)
            }
        }
    }
}

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun performLogin(username: String, password: String) {
        _loginState.value = LoginState.Loading
        Log.d("TAG_USER", "Username $username pwd $password")

        viewModelScope.launch {
            try {
                val authService = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8000/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(AuthService::class.java)
                Log.d("TAG_API_", "Went to /api ")
                val authResponse = authService.loginUser(LoginRequest(username, password))
                Log.d("TAG_AUTH_RESP", authResponse.toString())
                _loginState.value = LoginState.Success(authResponse.token)
                Log.d("TAG_LOGIN_STATE", "Val ${_loginState.value}")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e)
                Log.e("TAG_LOGIN_ERROR", "Login failed", e)
            }
        }
    }

    sealed class LoginState {
        data object Loading : LoginState()
        data class Success(val token: String) : LoginState()
        data class Error(val error: Exception) : LoginState()
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

data class AuthResponse(
    val token: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

interface AuthService {
    @POST("-token/")
    suspend fun loginUser(@Body loginRequest: LoginRequest): AuthResponse
}

interface EventService {
    @GET("events/")
    suspend fun getEvents(@Header("Authorization") token: String): EventResponse
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
fun LoginScreen(viewModel: LoginViewModel) {


    val overlayImage =
        "https://www.rithmschool.com/wp-content/uploads/2023/03/Square-rithm-logo.png"

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(overlayImage)
            .size(coil.size.Size.ORIGINAL) // Set the target size to load the image at.
            .build()
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current

        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val passwordVisualTransformation = remember { PasswordVisualTransformation() }

        if (painter.state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator()
        } else {
            Image(
                painter = painter,
                contentScale = ContentScale.Crop,
                contentDescription = "photo"
            )
        }

        TextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username") },
            modifier = Modifier.padding(16.dp),
            singleLine = true
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            visualTransformation = passwordVisualTransformation,
            modifier = Modifier.padding(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                // Trigger login when "Done" button is pressed on keyboard
                viewModel.performLogin(username, password)
            })
        )

        Button(
            onClick = {
                viewModel.performLogin(username, password)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventItemPreview() {
    EventItem(Event("Event title", "published"))
}
