package ru.quandastudio.lpsclient.core

import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.model.MessageWrapper
import ru.quandastudio.lpsclient.model.RequestType
import ru.quandastudio.lpsclient.model.SignUpRequest

class LpsRepository constructor(private val api: LpsApi) {

    fun getFriendsList() = api.getFriendsList()

    fun getHistoryList() = api.getHistoryList()

    fun getBlackList() = api.getBlackList()

    fun deleteFriend(friendId: Int) = api.deleteFriend(friendId)

    fun sendFriendRequestResult(userId: Int, isAccepted: Boolean) =
        api.sendFriendRequest(userId, if (isAccepted) RequestType.ACCEPT else RequestType.DENY)

    fun declineGameRequestResult(userId: Int) =
        api.sendGameRequestResult(userId, RequestType.DENY)

    fun deleteFromBlacklist(bannedId: Int) = api.deleteFromBlacklist(bannedId)

    fun updatePicture(type: String, hash: String, data: ByteArray) =
        Single.just(data)
            .map {
                RequestBody.create(
                    MediaType.get(
                        when (type) {
                            "png", "jpeg", "gif" -> "image/$type"
                            else -> "application/octet-stream"
                        }
                    ), data
                )
            }
            .flatMap { body -> api.updatePicture(type, hash, body) }
            .flatMap(MessageWrapper<String>::toSingle)

    fun deletePicture() = api.deletePicture()

    fun signUp(request: SignUpRequest) = api.signUp(request)
        .flatMap {
            if (it.error != null)
                Single.error(AuthorizationException(it.error))
            else
                Single.just(it.data!!)
        }
}