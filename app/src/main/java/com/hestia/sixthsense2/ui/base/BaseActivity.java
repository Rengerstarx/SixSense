package com.hestia.sixthsense2.ui.base;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Базовая активность от которой наследется {@link com.hestia.sixthsense2.ui.main.MainActivity}
 *
 */
public abstract class BaseActivity extends AppCompatActivity implements MvpView {

    /**
     * Перевод строки
     *
     * @param resourceId resource id
     *
     * @return Переведенная строка
     */
    public String translate(int resourceId) {
        return getResources().getString(resourceId);
    }

    /**
     * Show toast
     *
     * По умолчанию используется LENGTH_SHORT для продолжительности toast
     *
     * @param resourceId resource id
     * @param duration   toast duration time
     *
     * @see Toast
     */
    public void toast(int resourceId, Integer duration) {
        if (duration == null) {
            duration = Toast.LENGTH_SHORT;
        }
        Toast.makeText(this, resourceId, duration).show();
    }

}
