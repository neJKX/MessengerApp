package com.messange.app.messengerapp.domain.usecase.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.messange.app.messengerapp.R
import com.messange.app.messengerapp.databinding.ChatItemBinding
import com.messange.app.messengerapp.domain.model.ChatModel

class ChatRecyclerView(
    private val chatList: List<ChatModel>,
    private val onChatClick: (ChatModel) -> Unit
) : RecyclerView.Adapter<ChatRecyclerView.ChatHolder>() {

    class ChatHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val binding = ChatItemBinding.bind(item)

        fun bind(chatItem: ChatModel, onChatClick: (ChatModel) -> Unit) {
            with(binding) {
                user.text = chatItem.name
                message.text = chatItem.lastMessage

                Glide.with(root.context)
                    .load(chatItem.otherImage)
                    .placeholder(R.drawable.profile_icon)
                    .into(image)

                root.setOnClickListener { onChatClick(chatItem) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatHolder(view)
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bind(chatList[position], onChatClick)
    }
}