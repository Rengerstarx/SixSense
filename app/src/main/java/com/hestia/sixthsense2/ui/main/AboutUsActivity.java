package com.hestia.sixthsense2.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.hestia.sixthsense2.R;
import com.hestia.sixthsense2.ui.base.BaseActivity;
import com.hestia.sixthsense2.ui.routing.RoutingActivity;

/**
 * Экран (Activity), в котором предоставляется информация о разработчиках и приложении ("О нас")
 *
 * @see MainActivity Главная активность
 * @see RoutingActivity Экран "Навигация"
 * @see com.hestia.sixthsense2.ui.route.RouteActivity Экран "Ведение по маршруту"
 * @see SettingsActivity Экран "Настройки"
 *
 * @author Rengerstar <vip.bekezin@mail.ru>
 */
public class AboutUsActivity extends BaseActivity {

    //private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        //tts = new TextToSpeech(this, this);
        //speakOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences2;
        sharedPreferences2 = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences2.edit();
        editor.putBoolean("isFirstLaunch", true);
        editor.apply();
    }
    /*@Override
    public void onInit(int status){
        String text =
                "Шестое чувство - это система навигации внутри помещений для людей с ограниченными возможностями зрения. Проект создан командой Института компьютерных технологий и информационной безопасности Южного федерального университета:" +
                        "Плёнкин Антон Павлович." +
                        "Бекезин Сергей Александрович." +
                        "Москаленко Андрей Сергеевич." +
                        "Михайлова Василиса Дмитриевнна." +
                        "Елькин Дмитрий Максимович.";
        //tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,"");
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
    }*/
}

