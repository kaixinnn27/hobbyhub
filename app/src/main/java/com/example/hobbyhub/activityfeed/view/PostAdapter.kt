package com.example.hobbyhub.activityfeed.view

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.activityfeed.model.Post
import com.example.hobbyhub.activityfeed.viewmodel.PostViewModel
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ItemPostBinding
import com.example.hobbyhub.utility.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PostAdapter(
    private val posts: List<Post>,
    private val postViewModel: PostViewModel,
    private val userId: String,
    private val coroutineScope: CoroutineScope,
    private val authViewModel: AuthViewModel,
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(post: Post) {
            binding.username.text = post.username
            binding.descriptionTv.text = post.description
            binding.likeCountTextView.text = post.likeCount.toString()
            binding.commentCountTextView.text = post.commentCount.toString()

            coroutineScope.launch {
                val user = authViewModel.get(post.userId)
                if (user != null && user.photo.toBitmap() != null) {
                    binding.headerProfile.setImageBitmap(user.photo.toBitmap())
                    binding.letterOverlayTv.visibility = View.GONE
                }

                if (user != null && user.photo.toBitmap() == null) {
                    binding.headerProfile.setImageResource(R.drawable.profile_bg)
                    binding.letterOverlayTv.visibility = View.VISIBLE

                    val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                    binding.letterOverlayTv.text = firstLetter
                }
            }

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
                        post.likedBy.add(userId)
                        post.likeCount++
                        postViewModel.likePost(post.id, userId)
                        binding.likeButton.setImageResource(R.drawable.ic_favourite)
                    } else {
                        post.likedBy.remove(userId)
                        post.likeCount--
                        postViewModel.unlikePost(post.id, userId)
                        binding.likeButton.setImageResource(R.drawable.ic_not_favourite)
                    }
                    binding.likeCountTextView.text = post.likeCount.toString()
                }
            }

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
        val post = getItem(position)
        holder.bind(post)
    }

    // DiffUtil callback to efficiently update the list
    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
