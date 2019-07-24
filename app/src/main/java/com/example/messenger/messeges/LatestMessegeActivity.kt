package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.messenger.NewMessegeActivity.Companion.USER_ID_KEY
import com.example.messenger.NewMessegeActivity.Companion.USER_KEY
import com.example.messenger.messeges.ChatLogActivity
import com.example.messenger.models.ChatMessage
import com.example.messenger.views.Latestmessege
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messege.*
import kotlinx.android.synthetic.main.latest_messege_row.view.*

class  LatestMessegeActivity : AppCompatActivity() {
companion object{
    var TAG = "LatestMessages"
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messege)
        recyclerview_latest_messeges.adapter = adapter
        recyclerview_latest_messeges.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        //set item click listenr on ur adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG, "123")
            val intent = Intent (this, ChatLogActivity::class.java)
            val row = item as Latestmessege
            intent.putExtra(USER_KEY, row.chatPartnerUser!!.username)
            intent.putExtra(USER_ID_KEY, row.chatPartnerUser!!.uid)
            startActivity(intent)
        }

//        setupDummyRows()
        listenForLatestmesseges()
        verifyUserIsLoggedIn()
    }


    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(Latestmessege(it))
        }
    }

    private fun listenForLatestmesseges()
    {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messeges/$fromId")
        ref.addChildEventListener(object :ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatmessage = p0.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[p0.key!!] = chatmessage
                refreshRecyclerViewMessages()

            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)?: return

                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {


            }
        })


    }

    val adapter = GroupAdapter<ViewHolder>()

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){

            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_new_messege -> {
                val intent = Intent( this, NewMessegeActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }


        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }
}
