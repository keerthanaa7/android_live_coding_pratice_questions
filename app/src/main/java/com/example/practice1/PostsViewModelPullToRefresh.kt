package com.example.practice1

import PostItem
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface PostUIStatePullToRefresh{
    data object Loading: PostUIStatePullToRefresh
    data class Success(val posts: List<PostItem>, val isRefreshing: Boolean = false): PostUIStatePullToRefresh
    data class Error(val message: String): PostUIStatePullToRefresh
}

class PostsViewModelPullToRefresh(private val postRepository: PostRepository) : ViewModel(){

    companion object {
        private const val TAG = "PullToRefreshVM"
        private const val MIN_REFRESH_DURATION_MS = 1000L
    }

    private val uiStateMutable = MutableStateFlow<PostUIStatePullToRefresh>(PostUIStatePullToRefresh.Loading)
    val uistate = uiStateMutable.asStateFlow()

    init {
        fetchPosts()
    }
     fun fetchPosts(){
        viewModelScope.launch {
            uiStateMutable.value = PostUIStatePullToRefresh.Loading
            try {
                val postslist = postRepository.getPosts()
                Log.d("PostsViewModel", "post list ${postslist.size}")
                if(postslist.isEmpty()){
                    uiStateMutable.value = PostUIStatePullToRefresh.Error("post list is empty")
                }else{
                    uiStateMutable.value = PostUIStatePullToRefresh.Success(postslist)
                }
            }catch (e: Exception){
                uiStateMutable.value = PostUIStatePullToRefresh.Error("${e}")
            }
        }
    }

    fun refersh(){
        Log.d(TAG, "refersh() called")
        val currentstate = (uiStateMutable.value as? PostUIStatePullToRefresh.Success)?:return
       if(currentstate.isRefreshing){
           return
       }
        viewModelScope.launch {
            uiStateMutable.update {
               currentstate ->
                if(currentstate is PostUIStatePullToRefresh.Success){
                    currentstate.copy(isRefreshing =  true)
                }else{
                    currentstate
                }
            }

            // keep the spinner visible for at least 1s so it's noticeable during testing
            val refreshStart = System.currentTimeMillis()

            try{
                val newposts = postRepository.getPosts()
                val elapsed = System.currentTimeMillis() - refreshStart
                if(elapsed < MIN_REFRESH_DURATION_MS){
                    delay(MIN_REFRESH_DURATION_MS - elapsed)
                }
                if(newposts.isNullOrEmpty()){
                    uiStateMutable.update {
                            currentstate ->
                        if(currentstate is PostUIStatePullToRefresh.Success){
                            currentstate.copy(isRefreshing =  false)
                        }else{
                            currentstate
                        }
                    }
                }else{
                    uiStateMutable.value = PostUIStatePullToRefresh.Success(posts = newposts, isRefreshing = false)
                }
            }catch (e: Exception){
                uiStateMutable.update {
                    currentstate ->
                    if(currentstate is PostUIStatePullToRefresh.Success){
                        currentstate.copy(isRefreshing =  false)
                    }else{
                        currentstate
                    }
                }
            }
        }
    }

}