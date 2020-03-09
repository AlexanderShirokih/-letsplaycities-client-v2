package ru.quandastudio.lpsclient.core

import okhttp3.MediaType
import okhttp3.RequestBody
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.model.RequestType
import ru.quandastudio.lpsclient.model.SignUpRequest
import ru.quandastudio.lpsclient.model.SignUpResponse

class LpsRepository constructor(private val api: LpsApi) {

    suspend fun getFriendsList() = api.getFriendsList()

    suspend fun getHistoryList() = api.getHistoryList()

    suspend fun getBlackList() = api.getBlackList()

    suspend fun deleteFriend(friendId: Int) = api.deleteFriend(friendId)

    suspend fun sendFriendRequestResult(userId: Int, isAccepted: Boolean) =
        api.sendFriendRequest(userId, if (isAccepted) RequestType.ACCEPT else RequestType.DENY)

    suspend fun declineGameRequestResult(userId: Int) =
        api.sendGameRequestResult(userId, RequestType.DENY)

    suspend fun deleteFromBlacklist(bannedId: Int) = api.deleteFromBlacklist(bannedId)

    suspend fun updatePicture(type: String, hash: String, data: ByteArray): String {
        val body = RequestBody.create(
            MediaType.get(
                when (type) {
                    "png", "jpeg", "gif" -> "image/$type"
                    else -> "application/octet-stream"
                }
            ), data
        )

        val picture = api.updatePicture(type, hash, body)

        return picture.requireData()
    }

    suspend fun deletePicture() = api.deletePicture()

    suspend fun signUp(request: SignUpRequest): SignUpResponse {
        val response = api.signUp(request)

        if (response.error != null)
            throw AuthorizationException(response.error)

        return response.data!!
    }
}