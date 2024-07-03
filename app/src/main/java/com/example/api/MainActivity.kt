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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
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

        // Create an instance of the UserService using Retrofit
        val service = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(UserService::class.java)

        // Use the lifecycle scope to trigger the coroutine
        lifecycleScope.launch {
            try {
                val users = service.getUsers()
                // Print the result of the network call to the Logcat
                Log.d("TAG_", users.toString())
                // Update UI with the fetched data
                val composeView = findViewById<ComposeView>(R.id.compose_view)
                composeView.setContent {
                    APITheme {
                        UserList(users)
                    }
                }
            } catch (e: Exception) {
                // Log the exception
                Log.e("TAG_", "Error fetching users", e)
            }
        }
    }
}

data class User(
    val login: String,
    val id: Int,
    val avatar_url: String,
    val html_url: String,
    val followers: Int,
    val following: Int
)

interface UserService {
    @GET("users")
    suspend fun getUsers(): List<User>
}

@Composable
fun UserList(users: List<User>) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(users) { user ->
                UserItem(user)
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Login: ${user.login}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodySmall)
        Text(text = "URL: ${user.html_url}", style = MaterialTheme.typography.bodySmall)
        Text(text = "Followers: ${user.followers}", style = MaterialTheme.typography.bodySmall)
        Text(text = "Following: ${user.following}", style = MaterialTheme.typography.bodySmall)
        Image(
            painter = rememberAsyncImagePainter(user.avatar_url),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserItemPreview() {
    UserItem(User("octocat", 1, "https://avatars.githubusercontent.com/u/583231?v=4", "https://github.com/octocat", 100, 100))
}
