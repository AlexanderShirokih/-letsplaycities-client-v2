package ru.quandastudio.lpsclient.core

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import ru.quandastudio.lpsclient.model.BlackListItem
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.HistoryInfo

interface LpsApi {

    object Factory {
        fun create(baseUrl: String): LpsApi {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
            return retrofit.create(LpsApi::class.java)
        }
    }

    @GET("user/friends")
    fun getFriendsList(): Observable<List<FriendInfo>>

    @GET("user/history")
    fun getHistoryList(): Observable<List<HistoryInfo>>

    @GET("user/blacklist")
    fun getBlackList(): Observable<List<BlackListItem>>

}