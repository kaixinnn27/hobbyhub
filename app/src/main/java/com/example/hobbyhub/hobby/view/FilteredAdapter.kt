package com.example.hobbyhub.hobby.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.ItemFilteredBinding
import com.example.hobbyhub.hobby.model.Hobby

class FilteredAdapter(
    private val items: List<Hobby>,
    private val onItemClicked: ((Hobby) -> Unit)? = null
) : RecyclerView.Adapter<FilteredAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemFilteredBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Hobby) {

            val imageUrl = item.imageUrl.firstOrNull()

            Glide.with(binding.itemImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .into(binding.itemImage)

            // Set other text fields
            binding.itemTitle.text = item.name
            binding.itemDescription.text = item.description
            binding.itemTag.text = "\u2022 ${item.category}"

            // Handle item click, if listener is provided
            binding.root.setOnClickListener {
                // navigate to PlacesDetailsActivity
                val context = binding.root.context
                val intent = Intent(context, HobbyDetailsActivity::class.java).apply {
                    // Pass the selected TouristPlace to the details activity
                    putExtra("hobby", item)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFilteredBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}