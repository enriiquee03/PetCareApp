package com.example.petdaycarekotandfire

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Locale


class pets : AppCompatActivity(), BDController {
    lateinit var recyclerview : RecyclerView
    lateinit var searchView : androidx.appcompat.widget.SearchView
    lateinit var find_pet_text : TextView
    lateinit var empty_list_text : TextView
    lateinit var empty_list_image : ImageView
    lateinit var email : String
     var pList = ArrayList<pet>()
    var adapter : petAdapter = petAdapter(emptyList())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pets)


        find_pet_text = findViewById(R.id.find_pet_text)
        recyclerview = findViewById(R.id.recyclerview_pets)
        searchView = findViewById(R.id.search_widget)
        empty_list_text = findViewById(R.id.empty_list_text)
        empty_list_image = findViewById(R.id.empty_list_image)
        val add_button  = findViewById<ImageView>(R.id.add_pet)
        val logOutButton  = findViewById<ImageView>(R.id.logout)



        //Setup
        val bundle = intent.extras
         email = bundle?.getString("email")!!
        setup(email?: "")

        val prefs = getSharedPreferences("prefs_file_key",Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()



        //recyclerview
        recyclerview.setHasFixedSize(false)
        recyclerview.layoutManager = LinearLayoutManager(applicationContext)

        lifecycleScope.launch {
            getPetsOfUser(email,pList,empty_list_text,empty_list_image)
        }


        add_button.setOnClickListener {
            añadirmascota()
        }
        logOutButton.setOnClickListener {
            Utilities.areYouSureAlert({ logout() },"Cerrar Sesión","¿Estas seguro de que quieres cerrar sesión=",layoutInflater,this)
        }




        searchView.setOnQueryTextListener(object  : OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })

    }

    fun logout(){
        val prefs = getSharedPreferences("prefs_file_key",Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
        logOut()
        val i = Intent(this , Login::class.java)
        startActivity(i)
    }

     fun setup(email:String ){
        find_pet_text.text = email
    }

    fun filterList(query : String?){
        if(query!= null){
            val filteredlist = ArrayList<pet>()

            for(i in pList){
                if(i.nombre!!.lowercase(Locale.ROOT).contains(query) || i.raza!!.lowercase(Locale.ROOT).contains(query)){
                    filteredlist.add(i)
                }
            }

            if(filteredlist.isEmpty()){
                Utilities.makeToast(applicationContext,"No se ha encontrado ningún resultado")
            }else{
                adapter.setfileteredList(filteredlist)
            }
        }
    }


    fun editarmascotas(position :Int){
        Utilities.fromwhere = false
        val i = Intent(this,AddEditPets::class.java)
        i.putExtra("EXTRA_PET", adapter.pList[position])
        i.putExtra("email", email)
        startActivity(i)
    }
    fun añadirmascota(){
        Utilities.fromwhere = true
        val i = Intent(this,AddEditPets::class.java)
        i.putExtra("email", email)
        startActivity(i)
    }





    override fun success() {
        Utilities.makeToast(applicationContext,"Todo ha salido correctamente")
        adapter = petAdapter(pList)
        recyclerview.adapter = adapter

        adapter.setOnItemClickListener(object: petAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                editarmascotas(position)
            }

        })
    }

    override fun Error(error: String) {
        Utilities.showalert(error,this)
    }

    override fun onBackPressed() {
        Utilities.makeToast(applicationContext,"No puedes volver hacia atrás")
    }
}