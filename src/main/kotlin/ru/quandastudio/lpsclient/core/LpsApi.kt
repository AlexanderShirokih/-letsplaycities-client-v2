package ru.quandastudio.lpsclient.core

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.quandastudio.lpsclient.model.*

interface LpsApi {

    class AuthorizationInterceptor(userId: Int, hash: String) : Interceptor {
        private val credentials = Credentials.basic(userId.toString(), hash)

        override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
            chain.request()
                .newBuilder()
                .header("Authorization", credentials)
                .build()
        )

    }

    companion object {

        fun create(baseUrl: String, userId: Int, hash: String): LpsApi {
            return create(
                baseUrl, OkHttpClient().newBuilder().addInterceptor(
                    AuthorizationInterceptor(userId, hash)
                ).build()
            )
        }

        fun create(baseUrl: String, client: OkHttpClient): LpsApi {
            val retrofit = Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
            return retrofit.create(LpsApi::class.java)
        }
    }

    @GET("friend/")
    fun getFriendsList(): Maybe<List<FriendInfo>>

    @GET("history/")
    fun getHistoryList(): Maybe<List<HistoryInfo>>

    @GET("blacklist/")
    fun getBlackList(): Maybe<List<BlackListItem>>

    @DELETE("friend/{id}")
    fun deleteFriend(@Path("id") friendId: Int): Completable

    @DELETE("blacklist/{id}")
    fun deleteFromBlacklist(@Path("id") bannedId: Int): Completable

    @POST("user/picture")
    fun updatePicture(@Path("t") type: String, @Path("hash") hash: String, @Body data: ByteArray): Single<MessageWrapper<String>>

    @DELETE("user/picture")
    fun deletePicture(): Completable

    @POST("user/")
    fun signUp(@Body request: SignUpRequest): Maybe<MessageWrapper<SignUpResponse>>
}