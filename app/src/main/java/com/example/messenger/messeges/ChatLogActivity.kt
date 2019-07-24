package com.example.messenger.messeges

import android.app.ProgressDialog
import android.content.Entity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.messenger.NewMessegeActivity
import com.example.messenger.R
import com.example.messenger.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_to_row.view.textview_from_row
import android.provider.OpenableColumns
import android.security.KeyChain
import android.util.Base64
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.CryptoConfig
import com.scottyab.aescrypt.AESCrypt
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.GeneralSecurityException


class ChatLogActivity : AppCompatActivity() {




    val adapter = GroupAdapter<ViewHolder>()
    var uid :String ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)


        recycler_view_chat_log.adapter = adapter

        val username = intent.getStringExtra(NewMessegeActivity.USER_KEY)
        adapter.setOnItemClickListener { item, view ->

            downloadFile(view.textview_from_row.text.toString())
        }
        uid = intent.getStringExtra(NewMessegeActivity.USER_ID_KEY)
        supportActionBar?.title = username

        //ok

//     setupDummyData()
        ListenForMessages()

        SelectFileMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG,"Attempt to send message......")
            if (edittext_chat_log.text.isEmpty()){

                performFileSendMessage()
            }
            else{
                performSendMessage()
            }
        }




    }
private fun SelectFileMessages(){
        upload_any_file_button.setOnClickListener {
            Log.d(TAG,"Attempt to send file......")
            val intent = Intent ()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)

        }

}
    var selectedFileUri: Uri? = null
    var filename:String?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
          selectedFileUri = data?.data //The uri with the location of the file
            Log.d(TAG, "File path is :$selectedFileUri")
           filename=getFileName(selectedFileUri!!)


        }
    }
private fun performFileSendMessage(){
    val p=ProgressDialog(this)

    p.setMessage("Uploading Content..")
    p.show()
if (selectedFileUri == null) return

    val ref = FirebaseStorage.getInstance().getReference("/user/files").child("$filename")
    ref.putFile(selectedFileUri!!)
        .addOnSuccessListener {

            Toast.makeText(this,"Upload Succeeded ${it.metadata!!.path}",Toast.LENGTH_SHORT).show()
            Log.e("uploading file name", "$filename")
            edittext_chat_log.setText(filename)
            performSendMessage()
            p.dismiss()
        }.addOnFailureListener {

            p.dismiss()
            Log.e("----" ,it.message)
            Toast.makeText(this,"Uploading failed",Toast.LENGTH_SHORT).show()
        }
}
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
    companion object {
        val TAG = "ChatLog"
    }
        fun downloadFile(fileName:String) {

            val p=ProgressDialog(this)
            p.show()
            // Create a storage reference from our app
            val storageRef = FirebaseStorage.getInstance().reference


            val pathReference = storageRef.child("/user/files/$fileName")

            Log.e("file name", "/user/files/$fileName")
            Log.e("Download file name", "$fileName")
        val rootPath = File(Environment.getExternalStorageDirectory(),"ChatFile")
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }

        val localFile = File(rootPath, fileName)
        pathReference.getFile(localFile).addOnSuccessListener {
            Toast.makeText(this,"Download SuccessFull.. ${localFile.absolutePath}",Toast.LENGTH_SHORT).show()
            p.dismiss()
            Log.e("download success","123")
        }.addOnFailureListener {
              Toast.makeText(this,"Download Failed...",Toast.LENGTH_SHORT).show()
            p.dismiss()
            }
    }

    private  fun ListenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener{



            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatmessage = p0.getValue(ChatMessage::class.java)

                if (chatmessage != null){

                    Log.d(TAG, chatmessage.text)

                    if (chatmessage.fromId != FirebaseAuth.getInstance().uid){


                        ////////////////////////fbc and aes dec happens for txt msg here////////////////////////

                        /////////modify here for switching//////////////////////
                        adapter.add(ChatFromItem(textDec_fbc(chatmessage.text)))


                    }
                    else{

                        adapter.add(ChatToItem(textDec_fbc(chatmessage.text)))

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



        ///////////////////////////text aes and fbc called here//////////////////////////for enc//////////////////
        ///////////////modify here for switching////////////////////////////////
        val chatmessage = ChatMessage(reference.key!!, textEnc_fbc(text), fromId, toId, System.currentTimeMillis()/1000)



        reference.setValue(chatmessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                edittext_chat_log.text.clear()
                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
            .addOnFailureListener {
                Log.d(TAG, "NotSaved ${it.message}")
            }
        to_reference.setValue(chatmessage)


        val latestMessegeRef = FirebaseDatabase.getInstance().getReference("/latest-messeges/$fromId/$toId")
        latestMessegeRef.setValue(chatmessage)
        val latestMessegeToRef = FirebaseDatabase.getInstance().getReference("/latest-messeges/$toId/$fromId")
        latestMessegeToRef.setValue(chatmessage)
    }




    /////////////////fb conceal text msg ////////////////////////enc and dec////////////////////
    private fun textEnc_fbc(key: String, text_to_encrypt: String): String
    {
        val keyChain = SharedPrefsBackedKeyChain(applicationContext, CryptoConfig.KEY_256);
        val crypto = AndroidConceal.get().createDefaultCrypto(keyChain);

        val cipherText = crypto.encrypt(text_to_encrypt.toByteArray(), com.facebook.crypto.Entity.create("password"))
        return cipherText.toString()

    }
private fun textDec_fbc(key: String, text_to_decrypt: String): String
    {
        val keyChain = SharedPrefsBackedKeyChain(applicationContext, CryptoConfig.KEY_256);
        val crypto = AndroidConceal.get().createDefaultCrypto(keyChain);

        val plaintext =  crypto.decrypt(text_to_decrypt.toByteArray(), com.facebook.crypto.Entity.create("password"))
        return plaintext.toString()

    }

//    ///////////////////////fb conceal file encryption///////////////////////////////////
//    private fun FileEncryption_FbConceal(file_to_encrypt){
//
//
//        val keyChain = SharedPrefsBackedKeyChain(applicationContext, CryptoConfig.KEY_256)
//        val crypto = AndroidConceal.get().createDefaultCrypto(keyChain)
//
//        ​if (!crypto.isAvailable())
//        {
//            return
//        }
//        val fileStream = BufferedOutputStream(
//            FileOutputStream(filename)
//        )
//
//        val outputStream = crypto.getCipherOutputStream(
//            fileStream,
//            Entity.create("entity_id"))
//
//
//        outputStream.write(plainText)
//        outputStream.close()
//    }
//
//
//    //////////////////////fb conceal file decryption//////////////
//    private  fun FileDecryption_FbConceal(file_to_decrypt) {
//        val fileStream = FileInputStream(file)
//
//        val inputStream = crypto.getCipherInputStream(
//            fileStream,
//            Entity.create("entity_id")
//        )
//
//        val read: Int
//        val buffer = ByteArray(1024)
//
//        while ((read = inputStream.read(buffer)) != -1) {
//            out.write(buffer, 0, read)
//        }
//        inputStream.close()
//
//    }


}

///////////////////////////////////////////////////////////////////////
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



//////////////////////aes text enc and dec////////////////////
private  fun textEnc_aes(text_to_encrypt: String): String? {
    val password = "password"
    return try
    {
        val encryptedMsg = AESCrypt.encrypt(password, text_to_encrypt)
        encryptedMsg
    }
    catch (e: GeneralSecurityException) {
        ""
    }

}

private fun textDec_aes(text_to_decrypt:String):String
{
    val password = "password"
    return try
    {
        val messageAfterDecrypt = AESCrypt.decrypt(password, text_to_decrypt)
        messageAfterDecrypt
    }
    catch (e:GeneralSecurityException) {
        ""
    }

}

///////////////////////AES file encryption////////////////////////////////////////////

//@Throws(Exception::class)
//private fun encrypt(raw:ByteArray, clear:ByteArray):ByteArray {
//    val skeySpec = SecretKeySpec(raw, "AES")
//    val cipher = Cipher.getInstance("AES")
//    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
//    val encrypted = cipher.doFinal(clear)
//    return encrypted
//}
//
//
/////////////////////Aes file decryption//////////////////////////////////////////////
//@Throws(Exception::class)
//private fun decrypt(raw:ByteArray, encrypted:ByteArray):ByteArray {
//    val skeySpec = SecretKeySpec(raw, "AES")
//    val cipher = Cipher.getInstance("AES")
//    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
//    val decrypted = cipher.doFinal(encrypted)
//    return decrypted
//}

