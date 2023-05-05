package com.hestia.sixthsense.ui.main;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.network.ApiHelper;
import com.hestia.sixthsense.ui.base.BaseActivity;
import com.hestia.sixthsense.ui.routing.RoutingActivity;

import java.util.Locale;

/**
 * Экран (Activity), в котором предоставляется информация о разработчиках и приложении ("О нас")
 *
 * @see MainActivity Главная активность
 * @see RoutingActivity Экран "Навигация"
 * @see com.hestia.sixthsense.ui.route.RouteActivity Экран "Ведение по маршруту"
 * @see SettingsActivity Экран "Настройки"
 *
 * @author Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public class AboutUsActivity extends BaseActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        tts = new TextToSpeech(this, this);
        //speakOut();
    }
    @Override
    public void onInit(int status){
        String text =
                "Шестое чувство - это система навигации внутри помещений для людей с ограниченными возможностями зрения. Проект создан командой Института компьютерных технологий и информационной безопасности Южного федерального университета:" +
                        "Плёнкин Антон Павлович." +
                        "Бекезин Сергей Александрович." +
                        "Москаленко Андрей Сергеевич." +
                        "Михайлова Василиса Дмитриевнна." +
                        "Елькин Дмитрий Максимович.";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,"");
    }
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut() {
        String text =
                "Шестое чувство - это система навигации внутри помещений для людей с ограниченными возможностями зрения. Проект создан командой Института компьютерных технологий и информационной безопасности Южного федерального университета:" +
                "Плёнкин Антон Павлович." +
                "Бекезин Сергей Александрович." +
                "Москаленко Андрей Сергеевич." +
                "Михайлова Василиса Дмитриевнна." +
                "Елькин Дмитрий Максимович.";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,"");
    }
}

