package com.example.hobbyhub.activityfeed.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhub.activityfeed.model.Comment
import com.example.hobbyhub.activityfeed.model.Like
import com.example.hobbyhub.activityfeed.model.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("posts")
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    fun fetchPosts() {
        viewModelScope.launch {
            try {
                val snapshot = col.get().await()
                val postsList = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                _posts.postValue(postsList)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error fetching posts: $e")
            }
        }
    }

    suspend fun addPost(post: Post): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val postId = col.document().id
                val newPost = post.copy(id = postId)
                col.document(postId).set(newPost).await()
                true
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding post: $e")
                false
            }
        }
    }

    suspend fun likePost(postId: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val likeRef = col.document(postId)
                    .collection("likes").document(userId)
                likeRef.set(Like(postId, userId)).await()
                true
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error liking post: $e")
                false
            }
        }
    }

    suspend fun addComment(postId: String, comment: Comment): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val commentRef = col.document(postId)
                    .collection("comments").document()
                commentRef.set(comment).await()
                true
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding comment: $e")
                false
            }
        }
    }
}