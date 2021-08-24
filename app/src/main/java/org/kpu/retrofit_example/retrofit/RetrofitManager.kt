package org.kpu.retrofit_example.retrofit

import android.util.Log
import com.google.gson.JsonElement
import org.kpu.retrofit_example.utils.API
import org.kpu.retrofit_example.utils.Constants
import org.kpu.retrofit_example.utils.RESPONSE_STATE
import retrofit2.Call
import retrofit2.Response

class RetrofitManager {

    companion object{
        val instance :RetrofitManager = RetrofitManager()
    }

    //레트로핏 인터페이스 가져오기
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    //사진 검색 api 호출
    fun searchPhotos(searchTerm: String?, completion: (RESPONSE_STATE, String) -> Unit){
        val term = searchTerm?: ""
        val call = iRetrofit?.searchPhotos(searchTerm = term)?: return

        call.enqueue(object : retrofit2.Callback<JsonElement>{
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(Constants.TAG, "RetrofitManager - onResponse() called / response: ${response.body()}")

                completion(RESPONSE_STATE.OKAY, response.body().toString())
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(Constants.TAG, "RetrofitManager - onFailure() called / t: $t")

                completion(RESPONSE_STATE.FAIL, t.toString())
            }

        })
    }
}