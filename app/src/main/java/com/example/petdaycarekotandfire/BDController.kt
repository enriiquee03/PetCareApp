package com.example.petdaycarekotandfire

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


interface BDController {
    val db: FirebaseFirestore
        get() = Firebase.firestore
    val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    fun success()
    fun Error(error:String)


    fun registerEmailPassword(email:String, password:String,context: Context){
        if(email.isNotEmpty() && password.isNotEmpty() ){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if(it.isSuccessful){
                    success()
                    createUser(email,context)
                }else{
                    Error(it.exception?.message.toString())
                }
            }
        }
    }

    fun loginEmailPassword(email:String, password:String){
        if(email.isNotEmpty() && password.isNotEmpty() ){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if(it.isSuccessful){
                    success()
                }else{
                    Error(it.exception?.message.toString())
                }
            }
        }
    }
    fun logOut(){
            FirebaseAuth.getInstance().signOut()
    }

    fun createUser(email: String,context: Context){
        val user = hashMapOf(
            "email" to email
        )
        db.collection("users")
            .add(user)
            .addOnSuccessListener {
                success()

            }
            .addOnFailureListener { e ->
                Error(e.message.toString())
            }
    }

    suspend fun createpet(
        email: String, petName: String, raza: String, genero: String, constitucion: Double,
        context: CoroutineScope, selectedImg: Uri?
    ) {

        val pet = hashMapOf(
            "id" to getMaxIdOfPets(email),
            "nombre" to petName,
            "photo_url" to UploadImageOfPet(selectedImg!!),
            "raza" to raza,
            "genero" to genero,
            "constitucion" to constitucion
        )

        db.collection("users/${getUserIdByEmail(email)}/pets")
            .add(pet)
            .addOnSuccessListener {
                success()
            }
            .addOnFailureListener { e ->
                Error(e.message.toString())
            }
    }
    suspend fun getUserIdByEmail(email: String): String? {

        val querySnapshot = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].id
        } else {
            null
        }
    }
    suspend fun getPetOfUserIdById(email:String,petid : Int): String? {

        val querySnapshot = db.collection("users/${getUserIdByEmail(email)}/pets")
            .whereEqualTo("id", petid)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].id
        } else {
            null
        }
    }

    suspend fun EditPetOfUserByEmail(email: String, petid: Int, petName: String, raza: String, genero: String, constitucion: Double, selectedImg: Uri?, url: String) {
        val docRef = db.collection("users").document(getUserIdByEmail(email).toString()).collection("pets").document(getPetOfUserIdById(email, petid).toString())
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val updates = hashMapOf<String, Any>(
                                "nombre" to petName,
                                "raza" to raza,
                                "genero" to genero,
                                "constitucion" to constitucion
                            )

                            if (selectedImg != null) {
                                val imageUrl = UploadImageOfPet(selectedImg!!)
                                updates["photo_url"] = imageUrl
                            } else {
                                updates["photo_url"] = url
                            }

                            docRef.update(updates).await()

                            success()
                        } catch (e: Exception) {
                            error(e.message.toString())
                        }
                    }
                } else {
                    error("No se encontró la mascota con el id $petid")
                }
            }
            .addOnFailureListener {
                error(it.message.toString())
            }
    }


    suspend fun deletePetById(id: Int, email: String,url: String) {
        db.collection("users/${getUserIdByEmail(email)}/pets")
            .whereEqualTo("id", id)
            .get()

            .addOnSuccessListener {
                for (document in it.documents) {
                    document.reference.delete()
                }
                deleteImageOfPet(url)
                success()
                }
                .addOnFailureListener {
                    error(it.message.toString())
                }
    }
    suspend fun getMaxIdOfPets(email: String): Int? {

        val querySnapshot = db.collection("users/${getUserIdByEmail(email)}/pets")
            .orderBy("id",Query.Direction.DESCENDING).limit(1)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            (querySnapshot.documents[0]["id"].toString().toInt() + 1)
        } else {
             1
        }
    }

    suspend fun getPetsOfUser(email: String,pList : ArrayList<pet>,emptyListText : TextView , emptyListImage : ImageView) {
        db.collection("users/${getUserIdByEmail(email)}/pets")
            .get()
            .addOnSuccessListener {
                for (document in it){
                    val id = document.data["id"].toString().toInt()
                    val nombre = document.data["nombre"].toString()
                    val raza = document.data["raza"].toString()
                    val genero = document.data["genero"].toString()
                    val photo_url = document.data["photo_url"].toString()
                    val constitucion = document.data["constitucion"].toString().toDouble()
                    pList.add(pet(id,photo_url,nombre,raza, genero, constitucion))
                }
                if(pList.size!=0){
                    emptyListText.visibility = View.INVISIBLE
                    emptyListImage.visibility = View.INVISIBLE
                }else{
                    emptyListText.visibility = View.VISIBLE
                    emptyListImage.visibility = View.VISIBLE
                }
                success()
            }
            .addOnFailureListener {
                Error(it.message.toString())
            }
    }

    fun deleteImageOfPet(url : String){
        val storageRef = storage.getReferenceFromUrl(url)

        storageRef.delete()
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                error(it.message.toString())
            }
    }

     suspend fun UploadImageOfPet(selectedImage: Uri): String {
        return suspendCoroutine {continuar ->
            val reference = storage.reference.child("Pets").child(Date().time.toString())
            reference.putFile(selectedImage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl
                            .addOnSuccessListener { downloadUri ->
                                continuar.resume(downloadUri.toString())
                            }
                            .addOnFailureListener { exception ->
                                continuar.resumeWithException(exception)
                            }
                    } else {
                        continuar.resumeWithException(IllegalStateException("Fallo al subir la imagen."))
                    }
                }
        }
    }

}