package com.example.petdaycarekotandfire

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class petAdapter(var pList: List<pet>) :RecyclerView.Adapter<petAdapter.PetViewHolder>(){

    lateinit var mListener : onItemClickListener


// RecyclerView
    inner class PetViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val pet_image : ImageView = itemView.findViewById(R.id.profile_image)
        val nombre_pet = itemView.findViewById<TextView>(R.id.name)
        val raza_pet = itemView.findViewById<TextView>(R.id.raza)

        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.pet_list,parent,false)

        return PetViewHolder(view,mListener)
    }

    override fun getItemCount(): Int {
        return pList.size
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pList[position]
        val imageUrl = pet.imagen

        Glide.with(holder.itemView)
            .load(imageUrl)
            .into(holder.pet_image)

        holder.nombre_pet.text = pet.nombre
        holder.raza_pet.text = pet.raza

    }


    //BUSCADOR
    fun setfileteredList(mList: List<pet>){
        this.pList = mList
        notifyDataSetChanged()
    }

    //ItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }
}

