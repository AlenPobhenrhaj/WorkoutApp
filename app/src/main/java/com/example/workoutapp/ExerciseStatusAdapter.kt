package com.example.workoutapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.workoutapp.databinding.ItemExerciseStatusBinding

class ExerciseStatusAdapter(val items: ArrayList<ExerciseModel>) :
    RecyclerView.Adapter<ExerciseStatusAdapter.ViewHolder>() {

    // Inner class to hold the views for each item
    class ViewHolder (binding: ItemExerciseStatusBinding) : RecyclerView.ViewHolder(binding.root) {
        // Holds the TextView that will add each item to
        val tvItem = binding.tvItem
    }

    // Create a new ViewHolder object for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for the item
            return ViewHolder(
            ItemExerciseStatusBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    // Bind a ViewHolder to a particular item in the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the corresponding ExerciseModel object from the ArrayList
        val model: ExerciseModel = items[position]

        // Set the text of the TextView to the ID of the ExerciseModel object
        holder.tvItem.text = model.getId().toString()

        when {
            model.getIsSelected() -> {
                holder.tvItem.background =
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.item_circular_thin_color_accent_border
                    )
                holder.tvItem.setTextColor(Color.parseColor("#212121")) // Parse the color string, and return the corresponding color-int.
            }
            model.getIsCompleted() -> {
                holder.tvItem.background =
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.item_circular_color_accent_background)
                holder.tvItem.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else -> {
                holder.tvItem.background =
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.item_circular_color_gray_background)
                holder.tvItem.setTextColor(Color.parseColor("#212121"))
            }
        }
    }

    // Return the number of items in the ArrayList
    override fun getItemCount(): Int {
        return items.size
    }
}
