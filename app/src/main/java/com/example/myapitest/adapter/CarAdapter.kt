package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.Car
import com.example.myapitest.ui.CircleTransform
import com.squareup.picasso.Picasso

class CarAdapter(
    private val cars: List<Car>,
    private val carClickListener: (Car) -> Unit,
) : RecyclerView.Adapter<CarAdapter.CarHolder>() {

    class CarHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val tvNameCar: TextView = view.findViewById(R.id.name)
        val tvYearCar: TextView = view.findViewById(R.id.year)
        val tvLicenseCar: TextView = view.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return CarHolder(view)
    }

    override fun getItemCount(): Int = cars.size

    override fun onBindViewHolder(holder: CarHolder, position: Int) {
        val car = cars[position]
        holder.itemView.setOnClickListener {
            carClickListener.invoke(car)
        }

        holder.tvNameCar.text = car.name
        holder.tvYearCar.text = car.year
        holder.tvLicenseCar.text = car.licence

        Picasso.get()
            .load(car.imageUrl)
            .placeholder(R.drawable.ic_download)
            .error(R.drawable.ic_error)
            .transform(CircleTransform())
            .into(holder.imageView)
    }
}