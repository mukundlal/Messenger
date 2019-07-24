package com.example.messenger.views

import com.example.messenger.R
import com.example.messenger.User
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User1
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.scottyab.aescrypt.AESCrypt
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_messege_row.view.*
import java.security.GeneralSecurityException

class Latestmessege(val chatMessage: ChatMessage): Item<ViewHolder>(){
    //FIXME this object changed
    var chatPartnerUser: User1? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {


        viewHolder.itemView.messege_textview_latest_messege.text = textDec_aes(chatMessage.text)
        val chatPartnerId:String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }
        else
        {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User1::class.java)
                viewHolder.itemView.username_textview_latest_messege.text = chatPartnerUser?.username

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })



    }
    private fun textDec_aes(text_to_decrypt:String):String
    {
        val password = "password"
        return try
        {
            val messageAfterDecrypt = AESCrypt.decrypt(password, text_to_decrypt)
            messageAfterDecrypt
        }
        catch (e: GeneralSecurityException) {
            ""
        }

    }
    override fun getLayout(): Int {
        return R.layout.latest_messege_row
    }
}