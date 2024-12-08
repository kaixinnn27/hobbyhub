package com.example.hobbyhub.activityfeed.view

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.activityfeed.model.Post
import com.example.hobbyhub.activityfeed.viewmodel.PostViewModel
import com.example.hobbyhub.databinding.ItemPostBinding
import com.example.hobbyhub.utility.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PostAdapter(
    private val posts: List<Post>,
    private val postViewModel: PostViewModel,
    private val userId: String,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(post: Post) {
            // Set basic post data
            binding.username.text = post.username
            binding.descriptionTv.text = post.description
            binding.likeCountTextView.text = post.likeCount.toString()
            binding.commentCountTextView.text = post.commentCount.toString()

            // Set post image
            post.photo.let {
                binding.postImageView.setImageBitmap(it.toBitmap())
            }

            if (post.likedBy.contains(userId)) {
                binding.likeButton.setImageResource(R.drawable.ic_favourite)
            } else {
                binding.likeButton.setImageResource(R.drawable.ic_not_favourite)
            }

            binding.likeButton.setOnClickListener {
                coroutineScope.launch {
                    if (!post.likedBy.contains(userId)) {
                        // Like the post
                        post.likedBy.add(userId)
                        post.likeCount++
                        postViewModel.likePost(post.id, userId)

                        binding.likeButton.setImageResource(R.drawable.ic_favourite)
                        binding.likeCountTextView.text = post.likeCount.toString()
                    } else {
                        // Unlike the post
                        post.likedBy.remove(userId)
                        post.likeCount--
                        postViewModel.unlikePost(post.id, userId)

                        binding.likeButton.setImageResource(R.drawable.ic_not_favourite)
                        binding.likeCountTextView.text = post.likeCount.toString()
                    }
                }
            }

            // Handle Comment Button click
            binding.commentButton.setOnClickListener {
                val intent = Intent(binding.root.context, CommentActivity::class.java)
                intent.putExtra("postId", post.id)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size
}
