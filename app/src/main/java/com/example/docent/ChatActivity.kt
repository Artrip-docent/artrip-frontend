package com.example.docent

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //입력창
        speechText = findViewById(R.id.speech_text)

        //음성 전환 버튼이벤트
        val speechBtn: Button = findViewById(R.id.speech_btn)
        speechBtn.setOnClickListener {
            val intent: Intent = Intent()
            intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            activityResult.launch(intent)
        }

    }


    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){

        //보이스가 있다면
        if(it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){

            //음성전환 준비
            textToSpeech = TextToSpeech(this,this,"com.google.android.tts")
        }else{//없다면 다운로드
            //데이터 다운로드
            val installIntent: Intent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        }

    }

    //TextToSpeech엔진 초기화시 호출되는 함수
    override fun onInit(status: Int) {

        if(status == TextToSpeech.SUCCESS){

            //언어값
            val languageStatus: Int = textToSpeech.setLanguage(Locale.KOREAN)

            //데이터 문제(데이터가 없거나 언를 지원할 수 없는 경우)
            if(languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                languageStatus == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(this, "언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }else{//데이터 성공
                //입력값 변수에 담기
                val data: String = speechText.text.toString()

                val speechStatus: Int = 0

                //안드로이드 버전에 따른 조건(TIRAMISU보다 같거나 높다면)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    val speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH,
                        null, null)
                }else{
                    val speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH,
                        null)
                }

                if(speechStatus == TextToSpeech.ERROR){
                    Toast.makeText(this, "음성전환 에러입니다.", Toast.LENGTH_SHORT).show()
                }

            }
        }else{ //실패
            Toast.makeText(this, "음성전환 엔진 에러입니다.", Toast.LENGTH_SHORT).show()
        }

    }

}

