package org.kpu.retrofit_example.retrofit

import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.kpu.retrofit_example.utils.API
import org.kpu.retrofit_example.utils.Constants
import org.kpu.retrofit_example.utils.isJsonArray
import org.kpu.retrofit_example.utils.isJsonObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    //레트로핏 클라이언트 선언
    private var retrofitClient : Retrofit? = null
    //private lateinit var retrofitClient : Retrofit 돟일

    fun getClient(baseUrl : String) : Retrofit? {
        Log.d(Constants.TAG, "RetrofitClient - getClient() called")

        //okhttp 인스턴스 생성
        val client = OkHttpClient.Builder()
        //로그를 직기 위해 로깅 인터셉터 추가
        val loggingInterceptor = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger{
            override fun log(message: String) {
                Log.d(Constants.TAG, "RetrofitClient - log() called / message : $message")

                when{
                    message.isJsonObject() ->
                        Log.d(Constants.TAG, JSONObject(message).toString(4))
                    message.isJsonArray() ->
                        Log.d(Constants.TAG, JSONObject(message).toString(4))
                    else -> {
                        try {
                            Log.d(Constants.TAG, JSONObject(message).toString(4))
                        }catch(e: Exception){
                            Log.d(Constants.TAG, message)
                        }
                    }
                }
            }

        })

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)

        //위에서 설정한 로깅 인터셉터를 okhttp 클라이언트에 추가
        client.addInterceptor(loggingInterceptor)

        //기본 파라메터 추가
        val baseParameterInterceptor : Interceptor = (object : Interceptor{
            override fun intercept(chain: Interceptor.Chain): Response {
                Log.d(Constants.TAG, "RetrofitClient - intercept() called")
                val originalRequest : Request = chain.request()
                val addedUrl = originalRequest.url.newBuilder().addQueryParameter("client_id", API.CLIENT_ID).build()   //쿼리 파라미터 추가
                val finalRequest = originalRequest.newBuilder().url(addedUrl)
                    .method(originalRequest.method, originalRequest.body)
                    .build()

                return chain.proceed(finalRequest)
            }

        })

        //위에서 설정한 기본파라미터 인터셉터를 okhttp 클라이언트에 추가한다.
        client.addInterceptor(baseParameterInterceptor)

        //커넥션 타임아웃
        client.connectTimeout(10, TimeUnit.SECONDS)
        client.readTimeout(10, TimeUnit.SECONDS)
        client.writeTimeout(10, TimeUnit.SECONDS)
        client.retryOnConnectionFailure(true)   //실패했을 때 재시도 true

        if(retrofitClient == null){
            //레트로핏 빌더를 통해 인스턴스 생성
            retrofitClient = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build()) //위에서 설정한 클라이언트로 레트로핏 클라이언트를 설정한다.
                .build()
        }

        return retrofitClient
    }
}