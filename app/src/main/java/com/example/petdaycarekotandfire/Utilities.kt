package com.example.petdaycarekotandfire

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.xml.KonfettiView

class Utilities {

    companion object{
        var fromwhere = false

        fun showalert(error : String,context: Context){
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Error")
            builder.setMessage("Se ha producido un error  autenticando el usuario: $error")
            builder.setPositiveButton("Aceptar", null)
            val dialog : AlertDialog = builder.create()
            dialog.show()
        }



        fun showPets(email : String ,context: Context){
            val i = Intent(context,pets::class.java)
            i.putExtra("email", email)
            startActivity(context, i, null)
        }

        fun makeToast(context: Context,message : String){
            Toast.makeText(context,message,Toast.LENGTH_LONG).show()
        }

        fun EditCreateshowAlert(party: Party,seeButton : Boolean,pet_save2 : String , pet_save1 : String , sumbitButtonText : String, cancelButtonText:String?, layoutInflater: LayoutInflater , window: Window,context: Context,email: String){
            val dialogbinding = layoutInflater.inflate(R.layout.custom_dialog, null)

            val title = dialogbinding.findViewById<TextView>(R.id.pet_save_1)

                title.text = pet_save1
                dialogbinding.findViewById<TextView>(R.id.pet_save_2).text = pet_save2
            val dialog = Dialog(context)
            dialog.setContentView(dialogbinding)

            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val secondButton = dialogbinding.findViewById<Button>(R.id.cancelbutton)
            secondButton.isVisible = seeButton


            window.decorView.rootView.findViewById<KonfettiView>(R.id.konfettiView).start(party)

            // boton para quitar el dialog
            val sumbitbutton = dialogbinding.findViewById<Button>(R.id.submitbutton)
            sumbitbutton.text = sumbitButtonText
            sumbitbutton.setOnClickListener {
                if(!fromwhere){
                    val i  = Intent(context,pets::class.java)
                    i.putExtra("email", email)
                    startActivity(context,i,null)
                }
                dialog.dismiss()
            }
        }

        fun areYouSureAlert(funcion: () -> Unit, pet_save1: String, pet_save2: String, layoutInflater: LayoutInflater,context: Context) {
            val dialogbinding = layoutInflater.inflate(R.layout.custom_dialog, null)

            val title = dialogbinding.findViewById<TextView>(R.id.pet_save_1)

            title.text = pet_save1
            dialogbinding.findViewById<TextView>(R.id.pet_save_2).text =pet_save2
            val dialog = Dialog(context)
            dialog.setContentView(dialogbinding)

            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val secondButton = dialogbinding.findViewById<Button>(R.id.cancelbutton)
            secondButton.isVisible = true
            secondButton.text = "Cancelar"
            secondButton.setOnClickListener {
                dialog.dismiss()
            }


            val sumbitbutton = dialogbinding.findViewById<Button>(R.id.submitbutton)
            sumbitbutton.text = "Si, estoy seguro"
            if (context is LifecycleOwner) {
                sumbitbutton.setOnClickListener {
                    context.lifecycleScope.launch {
                        dialog.dismiss()
                        funcion()
                    }
                }
            }

        }

    }

}