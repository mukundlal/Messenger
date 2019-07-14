package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.messenger.R
import com.example.messenger.messeges.ChatLogActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_messege.*
import kotlinx.android.synthetic.main.user_row_new_messege.view.*

class NewMessegeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messege)

        supportActionBar?.title = "Select User"

//
//        val adapter = GroupAdapter<ViewHolder>()
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//
//
//        recycler_view_new_messege.adapter = adapter

        fetchUsers()

    }

    companion object{
        val USER_KEY = "USER_KEY"
        val USER_ID_KEY = "USER_ID"
    }

    private  fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

             val adapter = GroupAdapter<ViewHolder>()
             p0.children.forEach{
                 Log.d("NewMessege", it.toString())

                 val user = it.getValue(User::class.java)
                 if (user != null){
                     adapter.add(UserItem(user))
                     }
             }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent =Intent(view.context, ChatLogActivity::class.java )
                    intent.putExtra(USER_KEY, userItem.user.username)

                    intent.putExtra(USER_ID_KEY, userItem.user.uid)
                    startActivity(intent)

                    finish()
                }
                recycler_view_new_messege.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        //will be called in our list for each user object later on....
        viewHolder.itemView.username_textview_new_messege.text = user.username
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_messege
    }
}
