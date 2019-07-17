package com.example.mobhimessenger

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mobhimessenger.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycleView_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username
        //setupDummyData()
        listenForMessages()

        sendbtn_chat_log.setOnClickListener {
            Log.d("ChatLog", "Attempt to send msg... ${chatbox_chat_log.text.toString()}")

            performSendMsg()
        }

    }


    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        val fromUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null) {
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return

                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    }else{
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

                recycleView_chat_log.scrollToPosition(adapter.itemCount - 1)

                Log.d("listenToChat", chatMessage?.text)
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

    private fun performSendMsg(){
        //val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val text = chatbox_chat_log.text.toString()
        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        if(text != "") {
            ref.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("ChatLog", "Saved our chat message ${ref.key}")
                    chatbox_chat_log.text.clear()
                    recycleView_chat_log.scrollToPosition(adapter.itemCount-1)
                }

            toRef.setValue(chatMessage)
        }

        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageReference.setValue(chatMessage)

        val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToReference.setValue(chatMessage)

    }

//    private fun setupDummyData(){
//        val adapter = GroupAdapter<ViewHolder>()
//        adapter.add(ChatFromItem("From Message"))
//        adapter.add(ChatToItem("To Message"))
//        recycleView_chat_log.adapter = adapter
//    }
}

class ChatFromItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chats_from_row_textview.text = text
        val targetInto = viewHolder.itemView.imageView_from_row
        Picasso.get().load(user.profileImageUrl).into(targetInto)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chats_to_row_textview.text = text
        val targetInto = viewHolder.itemView.imageView_to_row
        Picasso.get().load(user.profileImageUrl).into(targetInto)
    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}