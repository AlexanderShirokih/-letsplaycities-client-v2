package ru.quandastudio.lpsclient.core

import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import ru.quandastudio.lpsclient.model.BlackListItem
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.HistoryInfo

interface LpsApi {

    companion object {
        fun create(baseUrl: String, userId: Int, hash: String): LpsApi {
            val retrofit = Retrofit.Builder()
                .client(OkHttpClient().newBuilder().addInterceptor { chain ->
                    chain.proceed(
                        chain.request()
                            .newBuilder()
                            .header("Authorization", Credentials.basic(userId.toString(), hash))
                            .build()
                    )
                }.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
            return retrofit.create(LpsApi::class.java)
        }
    }

    @GET("friend/")
    fun getFriendsList(): Observable<List<FriendInfo>>

    @GET("history/")
    fun getHistoryList(): Observable<List<HistoryInfo>>

    @GET("blacklist/")
    fun getBlackList(): Observable<List<BlackListItem>>

    @DELETE("friend/{id}")
    fun deleteFriend(@Path("id") friendId: Int): Completable

    @DELETE("blacklist/{id}")
    fun deleteFromBlacklist(@Path("id") bannedId: Int): Completable
}