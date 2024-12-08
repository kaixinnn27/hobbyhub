package com.example.hobbyhub.activityfeed.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.activityfeed.model.Comment
import com.example.hobbyhub.activityfeed.model.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("posts")
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    fun fetchPosts() {
        col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("PostViewModel", "Error fetching posts: $error")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val postsList = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                _posts.postValue(postsList)
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

    suspend fun addComment(postId: String, comment: Comment): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val commentRef = col.document(postId).collection("comments").document()
                commentRef.set(comment).await()

                val postRef = col.document(postId)
                postRef.update("commentCount", FieldValue.increment(1)).await()
                true
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding comment: $e")
                false
            }
        }
    }

    // Fetch the like count for a post
    suspend fun getLikeCount(postId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val likeRef = col.document(postId).collection("likes")
                val likeCount = likeRef.get().await().size()
                likeCount
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error fetching like count: $e")
                0
            }
        }
    }

    // Fetch the comment count for a post
    suspend fun getCommentCount(postId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val commentRef = col.document(postId).collection("comments")
                val commentCount = commentRef.get().await().size()
                commentCount
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error fetching comment count: $e")
                0
            }
        }
    }

    // Fetch the comments for a post
    suspend fun getComments(postId: String): List<Comment> {
        return withContext(Dispatchers.IO) {
            try {
                val commentRef = col.document(postId).collection("comments")
                val snapshot = commentRef.get().await()
                snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error fetching comments: $e")
                emptyList()
            }
        }
    }

    // Like a post
    suspend fun likePost(postId: String, userId: String) {
        try {
            val postRef = col.document(postId)

            // Add the user to the likedBy list and update likeCount
            postRef.update("likedBy", FieldValue.arrayUnion(userId))
            postRef.update("likeCount", FieldValue.increment(1))
        } catch (e: Exception) {
            Log.e("PostViewModel", "Error liking post: $e")
        }
    }

    // Unlike a post
    suspend fun unlikePost(postId: String, userId: String) {
        try {
            val postRef = col.document(postId)

            // Remove the user from the likedBy list and update likeCount
            postRef.update("likedBy", FieldValue.arrayRemove(userId))
            postRef.update("likeCount", FieldValue.increment(-1))
        } catch (e: Exception) {
            Log.e("PostViewModel", "Error unliking post: $e")
        }
    }
}