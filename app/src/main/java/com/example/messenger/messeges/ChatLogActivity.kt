package com.example.messenger.messeges

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.AutoText
import android.util.Log
import com.example.messenger.NewMessegeActivity
import com.example.messenger.R
import com.example.messenger.User
import com.example.messenger.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.textview_from_row
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }


    val adapter = GroupAdapter<ViewHolder>()
    val toUser : User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)


        recycler_view_chat_log.adapter = adapter

        val username = intent.getStringExtra(NewMessegeActivity.USER_KEY)
        supportActionBar?.title = username

        //ok

//     setupDummyData()
        ListenForMessages()
        send_button_chat_log.setOnClickListener {
            Log.d(TAG,"Attempt to send message......")
            performSendMessage()
        }
    }


    private  fun ListenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener{



            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatmessage = p0.getValue(ChatMessage::class.java)

                if (chatmessage != null){

                    Log.d(TAG, chatmessage.text)

                    if (chatmessage.fromId == FirebaseAuth.getInstance().uid){

                        adapter.add(ChatFromItem(chatmessage.text))

                    }
                    else{

                        adapter.add(ChatToItem(chatmessage.text))
                    }


                }


                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }


    private fun performSendMessage(){
        //how do we send data to firebase database

        val text = edittext_chat_log.text.toString()

        val user_id = intent.getStringExtra((NewMessegeActivity.USER_ID_KEY))
        val fromId = FirebaseAuth.getInstance().uid
//        val user = intent.getParcelableExtra<User>(NewMessegeActivity.USER_KEY)
        val toId = user_id

        if (fromId == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
                val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val to_reference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatmessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis()/1000)

        reference.setValue(chatmessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                edittext_chat_log.text.clear()
                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
to_reference.setValue(chatmessage)

        val latestMessegeRef = FirebaseDatabase.getInstance().getReference("/latest-messeges/$fromId/$toId")
        latestMessegeRef.setValue(chatmessage)
        val latestMessegeToRef = FirebaseDatabase.getInstance().getReference("/latest-messeges/$toId/$fromId")
        latestMessegeToRef.setValue(chatmessage)
    }

}

class ChatFromItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text
    }
    override fun getLayout(): Int {
    return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text
    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
