package com.example.petdaycarekotandfire

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class Login : AppCompatActivity() , BDController {
    lateinit var register_text : TextView
    lateinit var  login_button : Button
    lateinit var  register_button : Button
    lateinit var email : TextInputLayout
    lateinit var password : TextInputLayout
    lateinit var googleSign : RelativeLayout
    lateinit var facebookSign : RelativeLayout
    var emailText = ""
    val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button = findViewById(R.id.login_button)
        register_button = findViewById(R.id.register_button)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        googleSign = findViewById(R.id.google_container)
        facebookSign = findViewById(R.id.facebook_container)

        register_button.setOnClickListener {
            if(checkPassword(password.editText?.text.toString())) {
                emailText = email.editText?.text.toString()
                registerEmailPassword(emailText, password.editText?.text.toString(), this)
            }else{
                Utilities.makeToast(this,"La contraseña debe contener: \nMínimo 8 caracteres \nAl menos una letra \nAl menos un dígito numérico ")
            }
        }
        login_button.setOnClickListener {
            emailText = email.editText?.text.toString()
            loginEmailPassword(emailText,password.editText?.text.toString())

        }
        googleSign.setOnClickListener {
            GoogleSignConfig()
        }
        facebookSign.setOnClickListener {
            Utilities.makeToast(this , "Proximamente...")
        }
        session()
    }

    fun checkPassword(password : String):Boolean{
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
        return passwordRegex.matches(password)
    }

    fun GoogleSignConfig(){
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this,googleConf)
        googleClient.signOut()

        startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)
    }

     fun session(){
        val prefs = getSharedPreferences("prefs_file_key",Context.MODE_PRIVATE)
        val email = prefs.getString("email",null)
        if(email!= null){
            Utilities.showPets(email,this)
        }
    }

    override fun success() {
        Utilities.showPets(emailText,this)
    }

    override fun Error(error: String) {
        Utilities.showalert(error,this)
    }

    override fun onBackPressed() {
        Utilities.makeToast(applicationContext,"No puedes volver hacia atras")
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var context = this
        lifecycleScope.launch {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == GOOGLE_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)


                try {
                    val account = task.getResult(ApiException::class.java)
                    emailText = account.email!!
                    var existeUsuario = false
                    if(getUserIdByEmail(account.email!!) != null){
                        existeUsuario = true
                    }

                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)

                            .addOnCompleteListener {

                                if (it.isSuccessful) {
                                    if (!existeUsuario) {
                                        createUser(account.email!!, applicationContext)
                                    } else {
                                        Utilities.showPets(account.email!!, context)
                                    }


                                } else {
                                    Utilities.showalert(it.exception.toString(), applicationContext)
                                }
                            }
                    }
                } catch (e: ApiException) {
                    Utilities.makeToast(
                        applicationContext,
                        "Se ha producido un error al iniciar sesion con google"
                    )
                }
            }
        }
    }
}
