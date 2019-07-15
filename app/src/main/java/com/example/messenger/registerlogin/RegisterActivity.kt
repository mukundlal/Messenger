package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        register_button_register.setOnClickListener {

            performRegister()


        }
        already_registered_textview.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")
            finish()
        }
    }
    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText( this, "Please Enter email or password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is :"+email)
        Log.d("RegisterActivity", "Password : $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {


                // this method execute after authentication completion
                //checking if the authentication is successful
                if (it.isSuccessful)
                {
                    //authentication is successful
                    //calling usersavetoDB
                    saveUserToFirebaseDatabase()
                    Log.d("RegisterActivity", "Successfully created user with uid : ${it.result!!.user.uid}")

                }
                else{

                    Log.d("RegisterActivityFailure", "Failed Becouse ${it.exception}")
                    return@addOnCompleteListener
                }


            }
            .addOnFailureListener{
                // this method runs when the authentication failed the it.message returns the reason

                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText( this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()

            }

    }
    private fun saveUserToFirebaseDatabase(){

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")


        val user = User(uid,username_edittext_register.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {

                //this method execute when the user is saved to db
                Log.d(" RegisterActivity", "Finally we saved the user to firebase database")

                val intent = Intent(this, LatestMessegeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                //FIXME Commented code is for intenting to loginscreen but i dont think we need that
                //user need to go to chat after reg
                //val intent = Intent(this, LoginActivity::class.java)
                //
                //                startActivity(intent)

            }
            .addOnFailureListener {

                //this method execute when the user is saved to db is failed
                //it.message returns the reason

                Log.d("RegisterActivityFailure", "Filed Because ${it.message}")

            }
    }



}
class User(val uid:String, val username:String){
    constructor() : this("","")
}