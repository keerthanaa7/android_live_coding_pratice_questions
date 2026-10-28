package com.example.practice1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.practice1.ui.theme.Practice1Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private val postRepository: PostRepository by lazy {
        PostRepository()
    }

    private val postviewmodel: PostsViewModel by viewModels {
        viewModelFactory {
            initializer {
                PostsViewModel(postRepository)
            }
        }
    }

    private val postsViewModelNav: PostsViewModelNav by viewModels {
        viewModelFactory {
            initializer {
                PostsViewModelNav(postRepository)
            }
        }
    }

    private val postTimerRepository: PostTimerRepository by lazy {
        PostTimerRepository()
    }

    private val postsViewModelTimer: PostsViewModelTimer by viewModels {
        viewModelFactory {
            initializer {
                PostsViewModelTimer(postTimerRepository)
            }
        }
    }

    private val formViewmodel: FormViewModel by viewModels {
        viewModelFactory {
            initializer {
                FormViewModel()
            }
        }
    }

    private val postsViewModelPullToRefresh: PostsViewModelPullToRefresh by viewModels {
        viewModelFactory {
            initializer {
                PostsViewModelPullToRefresh(postRepository)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //SearchDebounce(postviewmodel)
            // Navigation(postsViewModelNav)
            // Timer(postsViewModelTimer)
            //  AutoScrolling(postsViewModelNav)
            //  ValidateForm(formViewmodel)
            PullToRefresh(postsViewModelPullToRefresh)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefresh(postsViewModelPullToRefresh: PostsViewModelPullToRefresh) {
    val uistate by postsViewModelPullToRefresh.uistate.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (val state = uistate) {
            is PostUIStatePullToRefresh.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(
                    Alignment.Center
                )
            )

            is PostUIStatePullToRefresh.Success ->
                PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = {postsViewModelPullToRefresh.refersh()}) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.posts) { post ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "post title ${post.title}")
                            }
                        }
                    }
                }

                    is PostUIStatePullToRefresh.Error -> Text(
                "error ${state.message}",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ValidateForm(formViewModel: FormViewModel) {
    val uistate by formViewModel.uistate.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("user registration")
        TextField(
            value = uistate.email,
            onValueChange = { formViewModel.onEmailChanged(it) },
            label = { Text("enter email") },
            isError = uistate.emailError != null
        )
        uistate.emailError?.let { emailerror ->
            Text(text = emailerror, color = Color.Red)
        }

        TextField(
            value = uistate.password,
            onValueChange = { formViewModel.onPasswordUpdated(it) },
            label = { Text("enter password") },
            isError = uistate.passworderror != null
        )
        uistate.passworderror?.let { passworderror ->
            Text(text = passworderror, color = Color.Red)
        }

        Button(onClick = { formViewModel.submitForm() }) {
            Text("submit form")
        }
        if (uistate.isSubmitted) {
            Text("form submitted successfully")
        }
    }
}

@Composable
fun AutoScrolling(postsViewModelNav: PostsViewModelNav) {
    val uistate by postsViewModelNav.UIstate.collectAsStateWithLifecycle()
    val liststate = rememberLazyListState()
    var isPlaying by remember { mutableStateOf(true) }

    val posts = (uistate as? PostUIStateNav.Success)?.posts
    LaunchedEffect(isPlaying, posts) {
        if (isPlaying && !posts.isNullOrEmpty()) {
            while (true) {
                delay(2000L)
                val nextindex = (liststate.firstVisibleItemIndex + 1) % posts.size
                liststate.animateScrollToItem(nextindex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uistate) {
            is PostUIStateNav.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(
                    Alignment.Center
                )
            )

            is PostUIStateNav.Error -> Text(
                text = "${state.message}",
                modifier = Modifier.align(Alignment.Center)
            )

            is PostUIStateNav.Success ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = liststate
                ) {
                    items(items = state.posts, key = { it.id }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("post id: ${item.id}")
                            Text("post title: ${item.title}")
                        }
                    }
                }
        }

        FloatingActionButton(
            onClick = { isPlaying = !isPlaying },
            containerColor = if (isPlaying) Color.Red else Color.Green,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "paused" else "playing"
            )
        }
    }
}

@Composable
fun Timer(postsViewModelTimer: PostsViewModelTimer) {
    val uistate by postsViewModelTimer.uistate.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uistate) {
            is PostUIStateTimer.Loading -> CircularProgressIndicator()
            is PostUIStateTimer.Success -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.posts, key = { it.id }) { post ->
                    DisplayEachRow(post)
                }
            }

            is PostUIStateTimer.Error -> Text("error is ${state.message}")
        }
    }

}

@Composable
fun DisplayEachRow(post: PostItemTimer) {
    var remainingMs by remember(post.id, post.timeStamp) {
        mutableStateOf(post.timeStamp - System.currentTimeMillis())
    }

    LaunchedEffect(post.id, post.timeStamp) {
        while (remainingMs > 0) {
            delay(1000)
            remainingMs = post.timeStamp - System.currentTimeMillis()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("post title: ${post.title}")
        Text("post id: ${post.id}")
        val secondsLeft = (remainingMs / 1000).coerceAtLeast(0)
        val timerText = if (secondsLeft > 0) {
            "Time left: ${secondsLeft}s"
        } else {
            "Timer expired"
        }

        Text(timerText)
    }
}

@Composable
fun Navigation(postsViewModelNav: PostsViewModelNav) {
    val navcontroller = rememberNavController()
    NavHost(navController = navcontroller, startDestination = "post home screen") {
        composable(route = "post home screen") {
            PostHomeScreen(postsViewModelNav) { post ->
                navcontroller.navigate("postdetailscreen/${post}")
            }
        }

        composable(route = "postdetailscreen/{post}", arguments = listOf(navArgument("post") {
            type =
                NavType.StringType
        })) { backstackentry ->
            val post = backstackentry.arguments?.get("post")
            PostDetaiilScreen(post as String)
        }
    }

}

@Composable
fun SearchDebounce(postsViewModel: PostsViewModel) {
    val search by postsViewModel.searchQuery.collectAsStateWithLifecycle()
    val uistate by postsViewModel.uistate.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = search, onValueChange = { postsViewModel.onQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uistate) {
                is PostUIState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )

                is PostUIState.Success ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.posts) { post ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "post title ${post.title}")
                            }
                        }
                    }

                is PostUIState.Error -> Text(
                    "error ${state.message}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Practice1Theme {
        Greeting("Android")
    }
}