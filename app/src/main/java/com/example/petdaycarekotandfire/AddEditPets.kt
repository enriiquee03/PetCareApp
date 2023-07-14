package com.example.petdaycarekotandfire

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class AddEditPets : AppCompatActivity(),BDController {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY = 2


    lateinit var first_button: Button
    lateinit var second_button: Button
    lateinit var name_text : TextInputEditText
    lateinit var raza_text : TextInputEditText
    lateinit var gender_spinner : AutoCompleteTextView
    lateinit var constitucion_text : TextInputEditText
    lateinit var email : String
    lateinit var pet_image : CircleImageView
    lateinit var backIcon : ImageView
    lateinit var loading : ProgressBar
    var selectedImg : Uri? = null
    var changeImage = false

    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_pets)


        val titulo = findViewById<TextView>(R.id.add_edit_text)
        first_button = findViewById(R.id.create_edit_button)
        second_button = findViewById(R.id.delete_button)
        name_text = findViewById(R.id.name_text)
        raza_text = findViewById(R.id.raza_text)
        gender_spinner = findViewById(R.id.gender_spinner)
        constitucion_text = findViewById(R.id.constitucion_text)
        pet_image = findViewById(R.id.pet_image)
        backIcon = findViewById(R.id.backArrow)
        loading = findViewById(R.id.loading)
        val bundle = intent.extras
        email = bundle?.getString("email")!!


        if(Utilities.fromwhere){
            setViewForCreatePet(titulo)

        }else{
            setViewForEditPet(titulo)
        }



        // spinner
        val genderOptions = listOf("Masculino", "Femenino")
        //spinner adapter
        val genderAdapter = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            genderOptions
        )
        gender_spinner.setAdapter(genderAdapter)
        // mostramos el spinner cuando el usuario hace click y escondemos el keyboard
        gender_spinner.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                gender_spinner.showDropDown()
            }
            hideSoftKeyboard(window.decorView.rootView)
            false
        }

            first_button.setOnClickListener {
                //comprobamos si algun campo esta vacio
                if (!validatePet(name_text, raza_text, gender_spinner, constitucion_text)) {
                    disableButton(first_button)
                        lifecycleScope.launch {
                            // Si quiere crear una mascota:

                            if(Utilities.fromwhere) {
                                if(changeImage) {
                                    if (getUserIdByEmail(email) != null) {
                                        loading.visibility = View.VISIBLE
                                        createpet(email, name_text.text.toString().trim(), raza_text.text.toString().trim(), gender_spinner.text.toString(), constitucion_text.text.toString().toDouble(), this, selectedImg)
                                    } else {
                                        Utilities.makeToast(applicationContext, "El email del usuario no  ha sido encontrado")
                                        enableButton(first_button)
                                    }
                                }else{
                                    Utilities.makeToast(applicationContext,"Debes añadir una imagen")
                                    enableButton(first_button)
                                }
                            // Si quiere editar una mascota
                            }else{
                                val mascota = intent.extras?.get("EXTRA_PET") as pet
                                //comprobamos si ha cambiado algo
                                if(!hasEditSomething(mascota) || changeImage) {
                                    loading.visibility = View.VISIBLE
                                    EditPetOfUserByEmail(email, mascota.id, name_text.editableText.toString(), raza_text.editableText.toString(), gender_spinner.editableText.toString(), constitucion_text.editableText.toString().toDouble(),selectedImg,mascota.imagen)
                                }else{
                                    Utilities.makeToast(applicationContext,"No se ha editado ninguno de los campos o la imagen")
                                    enableButton(first_button)
                                }
                            }
                        }

                } else {
                    Utilities.makeToast(applicationContext,"Alguno de los campos esta vacio o no has añadido la imagen")
                }
            }
        //eliminar la mascota
            second_button.setOnClickListener {
                val mascota = intent.extras?.get("EXTRA_PET") as pet
                    Utilities.areYouSureAlert({ lifecycleScope.launch {deletePetById(mascota.id, email,mascota.imagen)} },"Tu mascota sera eliminada","¿Estas seguro que deseas eliminar tu mascota?",layoutInflater,this)
            }

        pet_image.setOnClickListener {
            val options = arrayOf<CharSequence>("Tomar foto", "Seleccionar de la galería")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Elige una opción")
            builder.setItems(options) { dialog, item ->

                when (item) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
                    }
                }
            }
            builder.show()
        }

        backIcon.setOnClickListener {
            Utilities.showPets(email,this)
        }
    }

    // Modificamos la vista si el usuario quiere crear una nueva mascota
    fun setViewForCreatePet(titulo : TextView){
        titulo.text=getString(R.string.a_ade_una_mascota)
        first_button.text=getString(R.string.nueva)
        second_button.visibility = View.GONE
        first_button.text = "Crear"
        first_button.layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
    }
    // Modificamos la vista si el usuario quiere editar la mascota
    fun setViewForEditPet(titulo : TextView){
        val mascota = intent.extras?.get("EXTRA_PET") as pet
        titulo.text= "Edita tu mascota"

        first_button.text = "Editar"
        second_button.text = "Eliminar"
        name_text.setText(mascota.nombre)
        raza_text.setText(mascota.raza)
        gender_spinner.setText(mascota.genero)
        constitucion_text.setText(mascota.constitucion.toString())
        Glide.with(this )
            .load(mascota.imagen)
            .into(pet_image)
    }

    // esconder keyboard(para el spinner)
    fun hideSoftKeyboard(view: View) {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //metodo que verifica que ningun campo esta vacio
    fun validatePet(name_pet: TextInputEditText,raza_text: TextInputEditText,gender_spinner: AutoCompleteTextView,constitucion_text: TextInputEditText) : Boolean{
        return name_pet.editableText.isEmpty() || name_pet.editableText.isBlank() || raza_text.editableText.isEmpty()|| raza_text.editableText.isBlank() || gender_spinner.editableText.isEmpty() || gender_spinner.editableText.isBlank() || constitucion_text.editableText.isEmpty() || constitucion_text.editableText.isBlank()
    }


    //set animation party
    fun setParty() : Party{
        return  Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 150, TimeUnit.MILLISECONDS).max(150),
            position = Position.Relative(0.5, 0.3)
        )
    }
    // ha editado algo??
    fun hasEditSomething(mascota: pet):Boolean{
        return name_text.editableText.toString()== mascota.nombre && raza_text.editableText.toString()== mascota.raza && gender_spinner.editableText.toString()== mascota.genero &&  constitucion_text.editableText.toString().toDouble()== mascota.constitucion && !changeImage
    }

    fun clearInputs(){
        name_text.setText("")
        raza_text.setText("")
        gender_spinner.setText("")
        constitucion_text.setText("")
        changeImage = false
        selectedImg = null
        pet_image.setImageURI(null)
        pet_image.setImageBitmap(null)
        pet_image.setImageResource(R.drawable.add_image)
    }



    //Sobreescribimos los metodo de la interfaz BDController
    override fun success() {
        if(Utilities.fromwhere) {
            Utilities.EditCreateshowAlert(
                setParty(),
                false,
                "Tu mascota ha sido creada correctamente, ya puede disfrutar de todos nuestros servicios",
                "Tu mascota ha sido creada",
                "Aceptar",
                null,
                LayoutInflater.from(this),
                window,
                this,
                email
            )
            loading.visibility = View.INVISIBLE
            enableButton(first_button)
            clearInputs()
        }else{
            Utilities.EditCreateshowAlert(
                setParty(),
                false,
                "Los datos de tu mascota han sido editados/eliminados",
                "Tu mascota ha sido editada/eliminada",
                "Aceptar",
                null,
                LayoutInflater.from(this),
                window,
                this,
                email
            )
            loading.visibility = View.INVISIBLE
            enableButton(second_button)
            enableButton(first_button)
        }
    }

    override fun Error(error: String) {
        Utilities.showalert(error,this)
    }

    //Sobreescribimos el metodo onBackPressed
    override fun onBackPressed() {
        Utilities.makeToast(this ,"No puedes volver hacia atras \n Utiliza el boton de la esquina superior izquierda")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                        val imageBitmap = data?.extras?.get("data") as Bitmap
                        pet_image.setImageBitmap(imageBitmap)
                        selectedImg = getImageUriFromBitmap(this, imageBitmap)
                        changeImage = true

                }
                REQUEST_IMAGE_GALLERY -> {
                    if (data != null && data.data != null) {
                        selectedImg = data.data!!
                        pet_image.setImageURI(selectedImg)
                        changeImage = true
                    }
                }
            }
        }
    }
    //metodo para obtener la uri de la foto que pasamos por parametro
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val tempFile = File(context.cacheDir, "temp_image.jpg")
        tempFile.createNewFile()
        val outputStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return FileProvider.getUriForFile(context, "com.example.petdaycarekotandfire.fileprovider", tempFile)
    }
    private fun disableButton(button: Button) {
        button.isClickable = false
    }
    private fun enableButton(button: Button) {
        button.isClickable = true
    }

}