package org.kpu.retrofit_example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okio.Utf8
import org.json.JSONObject
import org.kpu.retrofit_example.retrofit.RetrofitManager
import org.kpu.retrofit_example.utils.Constants
import org.kpu.retrofit_example.utils.RESPONSE_STATE
import org.kpu.retrofit_example.utils.SEARCH_TYPE
import org.kpu.retrofit_example.utils.onMyTextChanged
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private var currentSearchType : SEARCH_TYPE = SEARCH_TYPE.PHOTO
    var btn_progress : ProgressBar? = null
    var btn_search : Button? = null
    var result_photo : ImageView? = null
    var result_photo_coil : ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(Constants.TAG, "MainActivity - onCreate() called")

        val search_radiogroup : RadioGroup = findViewById(R.id.search_radiogroup)
        val search_textlayout : TextInputLayout = findViewById(R.id.search_textlayout)
        val search_edittext : TextInputEditText = findViewById(R.id.search_edittext)
        val frame_search_btn : FrameLayout = findViewById(R.id.frame_search_btn)
        val main_scrollview : ScrollView = findViewById(R.id.main_scrollview)
        btn_progress  = findViewById(R.id.btn_progress)
        btn_search = findViewById(R.id.btn_search)
        result_photo = findViewById(R.id.result_photo)
        result_photo_coil = findViewById(R.id.result_photo2_coil)

        btn_progress?.bringToFront()


        search_radiogroup.setOnCheckedChangeListener{ _, checkedId ->
            when(checkedId){
                R.id.photo_search_radiobtn -> {
                    Log.d(Constants.TAG, "사진검색 버튼 클릭")
                    search_textlayout.hint = "사진검색"
                    search_textlayout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_photo_24, resources.newTheme())
                    currentSearchType = SEARCH_TYPE.PHOTO
                }

                R.id.user_search_radiobtn ->{
                    Log.d(Constants.TAG, "사용자검색 버튼 클릭")
                    search_textlayout.hint = "사용자검색"
                    search_textlayout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_assignment_ind_24, resources.newTheme())
                    currentSearchType = SEARCH_TYPE.USER
                }
            }
            Log.d(Constants.TAG, "MainActivity - OnCheckedChanged() called / currentSearchType : ${currentSearchType}")
        }

        search_edittext.onMyTextChanged {
            if(it.toString().count() > 0){
                frame_search_btn.visibility = View.VISIBLE
                main_scrollview.scrollTo(0, 200)    //스크롤 뷰를 올림
                search_textlayout.helperText = " "
            }else{
                frame_search_btn.visibility = View.INVISIBLE
            }

            if(it.toString().count() == 12){
                Log.d(Constants.TAG, "MainActivity - 에러 띄우기 ")
                Toast.makeText(this, "검색어는 12자 까지 입력 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        btn_search?.setOnClickListener {
            Log.d(Constants.TAG, "MainActivity - 검색 버튼 클릭 / currentSearchType : ${currentSearchType}")
            RetrofitManager.instance.searchPhotos(searchTerm = search_edittext.text.toString(), completion = { responseState, responseBody ->
                when(responseState){
                    RESPONSE_STATE.OKAY -> {
                        Log.d(Constants.TAG, "api 호출 성공 : $responseBody")
                        parsingJson(responseBody)
                    }
                    RESPONSE_STATE.FAIL -> {
                        Toast.makeText(this, "api 호출 에러입니다.", Toast.LENGTH_SHORT).show()
                        Log.d(Constants.TAG, "api 호출 실패 : $responseBody")

                    }
                }
            })
            this.handleSearchButtonUi()
        }
    }

    private fun handleSearchButtonUi(){
        btn_search?.visibility = View.INVISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            btn_progress?.visibility = View.INVISIBLE
            btn_search?.visibility = View.VISIBLE
        }, 1500)
    }

    private fun parsingJson(jsonStr : String){
        //json 파싱
        val jsonObject = JSONObject(jsonStr)
        val jsonArray = jsonObject.getJSONArray("results")
        val json = jsonArray.getJSONObject(0)
        val urls = json.getJSONObject("urls")
        val url_small = urls.getString("small")
        Log.d(Constants.TAG, "url : $url_small")
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO){
                downloadImg(url_small)
            }
            result_photo?.setImageBitmap(bitmap)
            result_photo?.visibility = View.VISIBLE
        }

        try{
            result_photo_coil?.load(url_small)      //coil 이용한 imageView에 load
        }catch(e: Exception){
            e.printStackTrace()
        }
        result_photo_coil?.visibility = View.VISIBLE

    }

    private fun downloadImg(imageUrl : String) : Bitmap?{
        var bitmap : Bitmap? = null
        try{
            val url : URL = URL(imageUrl)
            val connection : HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"    //request 방식 설정
            connection.connectTimeout = 10000   //10초의 타임아웃
            //connection.doOutput = true          //OutPutStream으로 데이터를 넘겨주겠다고 설정
                                                    //resCode가 501이 들어오도록 유발함
            connection.doInput = true           //InputStream으로 데이터를 읽겠다는 설정
            connection.useCaches = true         //캐싱 여부
            connection.connect()

            val resCode = connection.responseCode   //연결 상태 확인

            if(resCode == HttpURLConnection.HTTP_OK){   //200번
                val inputStream : InputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)

                connection.disconnect()
            }
        } catch (e : Exception){
            e.printStackTrace()
        }
        return bitmap
    }


}