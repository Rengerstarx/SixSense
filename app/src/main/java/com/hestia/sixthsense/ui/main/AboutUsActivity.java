package com.hestia.sixthsense.ui.main;

import android.os.Bundle;

import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.network.ApiHelper;
import com.hestia.sixthsense.ui.base.BaseActivity;
import com.hestia.sixthsense.ui.routing.RoutingActivity;

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
public class AboutUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
    }
}
