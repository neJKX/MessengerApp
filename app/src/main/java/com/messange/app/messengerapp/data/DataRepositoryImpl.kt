package com.messange.app.messengerapp.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.messange.app.messengerapp.domain.model.ChannelModel
import com.messange.app.messengerapp.domain.model.ChatModel
import com.messange.app.messengerapp.domain.model.UserModel
import com.messange.app.messengerapp.domain.repository.AuthRepository
import com.messange.app.messengerapp.domain.repository.ChatRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class DataRepositoryImpl : AuthRepository, ChatRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val database = Firebase.database

    override suspend fun registerWithEmail(email: String, password : String): Result<UserModel> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val id = result.user?.uid ?: return Result.failure(Exception("Ошибка регистрации"))
            val user = UserModel(id, null, null, null, email, null, password)
            firestore.collection("users").document(id).set(user)
            SharedPrefManager.saveUser(user)
            return Result.success(user)
        }catch (e : Exception){
            return Result.failure(Exception("Ошибка регистрации"))
        }
    }

    override suspend fun registerWithPhone(phone: String, activity: Activity): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    continuation.resume(Result.failure(Exception("Auto verification not supported")))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    continuation.resume(Result.failure(e))
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    SharedPrefManager.saveVerificationId(verificationId)
                    continuation.resume(Result.success(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }


    override suspend fun confirmPhoneSignIn(verificationId: String, code: String): Result<UserModel> {
        return suspendCancellableCoroutine { continuation ->
            Log.d("AuthRepository", "Проверка кода: $code с verificationId: $verificationId")

            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val userDocRef = firestore.collection("users").document(user.uid)

                            userDocRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userModel = document.toObject(UserModel::class.java)
                                    userModel?.let { SharedPrefManager.saveUser(it) }
                                    if (userModel != null) {
                                        continuation.resume(Result.success(userModel))
                                        SharedPrefManager.saveUser(userModel)
                                    } else {
                                        continuation.resume(Result.failure(Exception("Ошибка получения данных пользователя")))
                                    }
                                } else {
                                    val newUser = UserModel(
                                        id = user.uid,
                                        phone = user.phoneNumber,
                                        name = null,
                                        email = null,
                                        password = null,
                                        city = null,
                                        citate = null
                                    )
                                    userDocRef.set(newUser)
                                        .addOnSuccessListener {
                                            continuation.resume(Result.success(newUser))
                                            SharedPrefManager.saveUser(newUser)
                                        }
                                        .addOnFailureListener { e ->
                                            continuation.resume(Result.failure(e))
                                        }
                                }
                            }.addOnFailureListener { e ->
                                continuation.resume(Result.failure(e))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("User is null after login")))
                        }
                    } else {
                        Log.e("AuthRepository", "Ошибка входа: ${task.exception?.localizedMessage}")
                        continuation.resume(Result.failure(task.exception ?: Exception("Login failed")))
                    }
                }
        }
    }


    override suspend fun loginWithEmail(email: String, password: String): Result<UserModel> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val id = result?.user?.uid ?: return Result.failure(Exception("Ошибка входа"))

            val userDocs = firestore.collection("users").document(id).get().await()
            val name = userDocs.getString("name") ?: ""
            val phone = userDocs.getString("phone") ?: ""
            val user = UserModel(id ,name , null, null, email, null, password)
            SharedPrefManager.saveUser(user)
            Result.success(user)
        }catch (e : Exception){
            Result.failure(Exception("Ошибка"))
        }
    }

    override suspend fun loginWithPhone(verificationId: String, code: String): Result<UserModel> {
        return confirmPhoneSignIn(verificationId, code)
    }

    override suspend fun logOut() {
        auth.signOut()
    }

    override suspend fun createChatWithUser(otherId: String): Result<ChatModel> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Пользователь не авторизован"))

        val userRef = firestore.collection("users").document(userId)
        val otherUserRef = firestore.collection("users").document(otherId)

        try {
            val userSnapshot = userRef.get().await()
            val otherUserSnapshot = otherUserRef.get().await()

            val userName = userSnapshot.getString("name") ?: "Unknown"
            val userImage = userSnapshot.getString("profileImage") ?: ""

            val otherUserName = otherUserSnapshot.getString("name") ?: "Unknown"
            val otherUserImage = otherUserSnapshot.getString("profileImage") ?: ""

            val chatId = firestore.collection("chats").document().id
            val chatRef = firestore.collection("chats").document(chatId)

            val chat = ChatModel(
                id = chatId,
                name = otherUserName,
                otherImage = otherUserImage,
                otherId = otherId,
                lastMessage = ""
            )

            chatRef.set(mapOf("users" to listOf(userId, otherId))).await()

            val userChatRef = userRef.collection("chats").document(chatId)
            val otherUserChatRef = otherUserRef.collection("chats").document(chatId)

            userChatRef.set(chat).await()

            val otherUserChat = chat.copy(name = userName, otherImage = userImage, otherId = userId)
            otherUserChatRef.set(otherUserChat).await()

            return Result.success(chat)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun createGroupChat(
        chatName: String,
        groupImage: String,
        users: List<String>
    ): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))

        if (users.isEmpty()) return Result.failure(Exception("Добавьте хотя бы одного участника"))

        val chatId = firestore.collection("chats").document().id
        val chatRef = firestore.collection("chats").document(chatId)

        val chatData = mapOf(
            "id" to chatId,
            "name" to chatName,
            "image" to groupImage,
            "users" to users + userId,
            "lastMessage" to ""
        )

        return try {
            chatRef.set(chatData).await()
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUserToGroup(chatId: String, newUserId: String): Result<Unit> {
        val chatRef = firestore.collection("chats").document(chatId)

        return try {
            val chatSnapshot = chatRef.get().await()
            val usersList = chatSnapshot.get("users") as? List<String> ?: return Result.failure(Exception("Чат не найден"))

            if (newUserId in usersList) return Result.failure(Exception("Пользователь уже в чате"))

            chatRef.update("users", usersList + newUserId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createChannel(
        channelName: String,
        subscribers: List<String>
    ): Result<String> {
        val ownerId = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))

        val channelId = firestore.collection("channels").document().id
        val channelRef = firestore.collection("channels").document(channelId)

        val channelData = mapOf(
            "id" to channelId,
            "name" to channelName,
            "ownerId" to ownerId,
            "users" to subscribers + ownerId
        )

        return try {
            channelRef.set(channelData).await()
            Result.success(channelId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUserToChannel(channelId: String, newUserId: String): Result<Unit> {
        val channelRef = firestore.collection("channels").document(channelId)

        return try {
            val channelSnapshot = channelRef.get().await()
            val usersList = channelSnapshot.get("users") as? List<String> ?: return Result.failure(Exception("Канал не найден"))

            if (newUserId in usersList) return Result.failure(Exception("Пользователь уже подписан"))

            channelRef.update("users", usersList + newUserId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessageToChannel(channelId: String, messageText: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))

        val channelRef = firestore.collection("channels").document(channelId)
        val messagesRef = channelRef.collection("messages").document()

        return try {
            val channelSnapshot = channelRef.get().await()
            val ownerId = channelSnapshot.getString("ownerId") ?: return Result.failure(Exception("Канал не найден"))

            if (userId != ownerId) return Result.failure(Exception("Вы не можете отправлять сообщения в этот канал"))

            val messageData = mapOf(
                "senderId" to userId,
                "text" to messageText,
                "timestamp" to System.currentTimeMillis()
            )

            messagesRef.set(messageData).await()
            channelRef.update("lastMessage", messageText).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getUserChats(): Result<List<ChatModel>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Пользователь не авторизован"))

        val userChatsRef = firestore.collection("users").document(userId).collection("chats")

        return try {
            val chatSnapshots = userChatsRef.get().await()
            val chatList = chatSnapshots.documents.mapNotNull { it.toObject(ChatModel::class.java) }

            Result.success(chatList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserChannels(): Result<List<ChannelModel>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Пользователь не авторизован"))

        return try {
            val channelSnapshots = firestore.collection("channels")
                .whereArrayContains("users", userId)
                .get().await()

            val channelList = channelSnapshots.documents.mapNotNull { it.toObject(ChannelModel::class.java) }
            Result.success(channelList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
