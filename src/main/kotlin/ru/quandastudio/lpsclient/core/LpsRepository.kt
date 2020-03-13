package ru.quandastudio.lpsclient.core

import okhttp3.MediaType
import okhttp3.RequestBody
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.model.RequestType
import ru.quandastudio.lpsclient.model.SignUpRequest
import ru.quandastudio.lpsclient.model.SignUpResponse

/**
 * Repository for REST requests to LPS server
 */
class LpsRepository constructor(private val api: LpsApi) {

    /**
     * Returns friends for current user.
     */
    suspend fun getFriendsList() = api.getFriendsList()

    /**
     * Returns history list for current user.
     */
    suspend fun getHistoryList() = api.getHistoryList()

    /**
     * Returns banned players for current user.
     */
    suspend fun getBlackList() = api.getBlackList()

    /**
     * Delete friend from user friends list
     * @param friendId fiends ID to delete
     */
    suspend fun deleteFriend(friendId: Int) = api.deleteFriend(friendId)

    /**
     * Sends friendship request result from current player to server
     * @param userId request sender ID
     * @param isAccepted `true` if user accept the request, `false` otherwise
     */
    suspend fun sendFriendRequestResult(userId: Int, isAccepted: Boolean) =
        api.sendFriendRequest(userId, if (isAccepted) RequestType.ACCEPT else RequestType.DENY)

    /**
     * Sends negative result of game request from current player to server.
     * @param userId request sender ID
     */
    suspend fun declineGameRequestResult(userId: Int) =
        api.sendGameRequestResult(userId, RequestType.DENY)

    /**
     * Deletes user from current player ban list.
     * @param bannedId banned user ID to be removed
     */
    suspend fun deleteFromBlacklist(bannedId: Int) = api.deleteFromBlacklist(bannedId)

    /**
     * Updates firebase token for current user.
     * @param newToken fresh firebase token
     */
    suspend fun updateToken(newToken: String) = api.updateToken(newToken)

    /**
     * Update user's picture on game server.
     * @param type picture type (one of png, jpeg, gif)
     * @param hash MD5 code of picture data
     * @param data binary picture data
     * @return "ok" or error
     * @throws ru.quandastudio.lpsclient.LPSException if error happens
     */
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

    /**
     * Removes picture from current user profile
     */
    suspend fun deletePicture() = api.deletePicture()

    /**
     * Sends sign up request.
     * @param request input request with authentication data
     * @return SignUpResponse when sign up successful
     * @throws AuthorizationException when cannot authenticate player
     */
    suspend fun signUp(request: SignUpRequest): SignUpResponse {
        val response = api.signUp(request)

        if (response.error != null)
            throw AuthorizationException(response.error)

        return response.data!!
    }
}