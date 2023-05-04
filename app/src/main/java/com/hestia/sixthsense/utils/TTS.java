package com.hestia.sixthsense.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Класс для работы с TextToSpeech с возможностью пауз
 * Используется паттерн Builder
 */
public class TTS implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Locale locale;
    private Context context;

    /**
     * Скорость речи
     */
    private float speechRate = 1.0f;
    /**
     * Знак, встретив который распознователь будет делать паузу
     */
    private String signSeparation = "@";
    /**
     * Длительность паузы для одного знака signSeparation
     */
    private int pauseDurationMc = 1000;

    private String queueText = "";
    /**
     * Инициализирован ли TTS
     */
    private boolean initialized;

    public static class Builder {
        private Locale locale;
        private Context context;

        private float speechRate = 1.0f;
        private String signSeparation = "@";
        private int pauseDurationMc = 1000;

        public Builder(Context context, Locale locale) {
            this.context = context;
            this.locale = locale;
        }

        public Builder setSpeechRate(float speechRate) {
            this.speechRate = speechRate;
            return this;
        }

        public Builder setSignSeparation(String signSeparation) {
            this.signSeparation = signSeparation;
            return this;
        }

        public Builder setPauseDurationMc(int pauseDurationMc) {
            this.pauseDurationMc = pauseDurationMc;
            return this;
        }

        public TTS build() {
            return new TTS(this);
        }
    }

    public TTS(Builder builder) {
        this.context = builder.context;
        this.locale = builder.locale;
        this.signSeparation = builder.signSeparation;
        this.speechRate = builder.speechRate;
        this.pauseDurationMc = builder.pauseDurationMc;

        tts = new TextToSpeech(this.context, this);
    }

    public String getSignSeparation() {
        return signSeparation;
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {

            tts.setLanguage(locale);
            tts.setSpeechRate(speechRate);

            initialized = true;

            if (queueText != null) {
                speak(queueText);
            }
        } else {
            throw new IllegalArgumentException("TextToSPeach cannot be initialized");
        }
    }

    /**
     * Функция говорилка
     * Если TTS еще не инициализирован, заносит текст в очередь, когда инициализируется,
     * очередь произносится
     * <p>
     * По символу signSeparation делается пауза длительностью pauseDuration
     *
     * @param s строка, которую будет произносить TTS
     */
    public void speak(String s) {
        if (!initialized) {
            queueText = s;
            return;
        }
        queueText = null;
        String[] t = s.split("!");

        for (String str : t) {
            tts.playSilentUtterance(0, TextToSpeech.QUEUE_ADD, null);
            tts.speak(str, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public void shutdown() {
        if (tts != null)
            this.tts.shutdown();
    }
}

