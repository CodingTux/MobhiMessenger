package com.example.mobhimessenger.views

import com.example.mobhimessenger.R
import com.example.mobhimessenger.User
import com.example.mobhimessenger.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){

    var chatPartnerUser: User ? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.latestMessage_latest_message.text = chatMessage.text
        val chatPartnerId: String

        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }else{
            chatPartnerId = chatMessage.fromId
        }


        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java) ?: return
                viewHolder.itemView.username_latest_message.text = chatPartnerUser?.username
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.profilePicture_latest_message)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
    override fun getLayout(): Int {
        return R.layout.latest_messages_row
    }
}