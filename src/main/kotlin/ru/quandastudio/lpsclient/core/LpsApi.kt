package ru.quandastudio.lpsclient.core

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.quandastudio.lpsclient.model.*

interface LpsApi {

    class AuthorizationInterceptor(private val credentialsProvider: CredentialsProvider) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
            chain.request()
                .newBuilder()
                .apply {
                    val cred = credentialsProvider.getCredentials()
                    if (cred.isValid()) {
                        header("Authorization", okhttp3.Credentials.basic(cred.userId.toString(), cred.hash))
                    }
                }
                .build()
        )
    }

    companion object {

        fun create(baseUrl: String, credentialsProvider: CredentialsProvider): LpsApi {
            return create(
                baseUrl,
                OkHttpClient().newBuilder().addInterceptor(AuthorizationInterceptor(credentialsProvider)).build()
            )
        }

        fun create(baseUrl: String, client: OkHttpClient): LpsApi {
            val retrofit = Retrofit.Builder()
                .client(client)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
            return retrofit.create(LpsApi::class.java)
        }
    }

    @GET("friend/")
    suspend fun getFriendsList(): List<FriendInfo>

    @GET("history/")
    suspend fun getHistoryList(): List<HistoryInfo>

    @GET("blacklist/")
    suspend fun getBlackList(): List<BlackListItem>

    @DELETE("friend/{id}")
    suspend fun deleteFriend(@Path("id") friendId: Int)

    @PUT("friend/request/{id}/{type}")
    suspend fun sendFriendRequest(@Path("id") userId: Int, @Path("type") requestType: RequestType)

    @PUT("user/request/{id}/{type}")
    suspend fun sendGameRequestResult(@Path("id") userId: Int, @Path("type") requestType: RequestType)

    @DELETE("blacklist/{id}")
    suspend fun deleteFromBlacklist(@Path("id") bannedId: Int)

    @POST("user/picture")
    suspend fun updatePicture(
        @Query("t") type: String,
        @Query("hash") hash: String,
        @Body body: RequestBody
    ): MessageWrapper<String>


    @DELETE("user/picture")
    suspend fun deletePicture()

    @POST("user/token/{token}")
    suspend fun updateToken(@Path("token") newToken: String)

    @POST("user/")
    suspend fun signUp(@Body request: SignUpRequest): MessageWrapper<SignUpResponse>
}
