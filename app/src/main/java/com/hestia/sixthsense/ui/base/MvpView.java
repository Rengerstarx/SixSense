package com.hestia.sixthsense.ui.base;

import android.widget.Toast;

/**
 * MVP View
 *
 * @category   UI
 * @package    Base
 * @subpackage View
 * @author     Dmitry Abakumov <killerinshadow2@gmail.com>
 */
public interface MvpView {

    /**
     * Translate string
     *
     * @param resourceId resource id
     *
     * @return translated string
     */
    String translate(int resourceId);

    /**
     * Show toast
     *
     * By default using LENGTH_SHORT for toast duration
     *
     * @param resourceId resource id
     * @param duration   toast duration time
     *
     * @see Toast
     */
    void toast(int resourceId, Integer duration);

}
