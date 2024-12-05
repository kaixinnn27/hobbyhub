package com.example.hobbyhub.hobby.view

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.HorizontalHobbyItemBinding
import com.example.hobbyhub.hobby.model.Hobby

class HorizontalHobbyAdapter(private var hobbies: List<Hobby>) :
    RecyclerView.Adapter<HorizontalHobbyAdapter.TouristViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristViewHolder {
        val binding = HorizontalHobbyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TouristViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TouristViewHolder, position: Int) {
        holder.bind(hobbies[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Hobby>) {
        hobbies = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = hobbies.size

    class TouristViewHolder(private val binding: HorizontalHobbyItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hobby: Hobby) {
            binding.hobbyTitle.text = hobby.name.replace(" ", "\n")

            val imageUrl = hobby.imageUrl.firstOrNull()

            Glide.with(binding.touristImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_miscellaneous)
                .into(binding.touristImage)

            binding.root.setOnClickListener {
                // navigate to PlacesDetailsActivity
                val context = binding.root.context
                // TODO: navigate to hobby details
//                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
//                    // Pass the selected TouristPlace to the details activity
//                    putExtra("hobby", hobby)
//                }
//                context.startActivity(intent)
            }
        }
    }
}