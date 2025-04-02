package com.messange.app.messengerapp.domain.repository

import com.messange.app.messengerapp.domain.model.ChannelModel
import com.messange.app.messengerapp.domain.model.ChatModel

interface ChatRepository {
    suspend fun createChatWithUser(otherId : String) : Result<ChatModel>
    suspend fun createGroupChat(chatName: String, groupImage: String, users: List<String>) : Result<String>
    suspend fun addUserToGroup(chatId: String, newUserId: String) : Result<Unit>
    suspend fun createChannel(channelName: String, subscribers: List<String>): Result<String>
    suspend fun addUserToChannel(channelId: String, newUserId: String): Result<Unit>
    suspend fun sendMessageToChannel(channelId: String, messageText: String): Result<Unit>
    suspend fun getUserChats(): Result<List<ChatModel>>
    suspend fun getUserChannels(): Result<List<ChannelModel>>

}