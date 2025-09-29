package com.desaiwang.transit.path

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.desaiwang.transit.path.widget.DepartureBoardWidget
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                DepartureBoardWidget().updateAll(MobilePathApplication.instance)
            }
        }

        setContent {
            App()
        }
    }

    companion object {
        fun createAppWidgetLaunchAction(): Action {
            return Intent(MobilePathApplication.instance, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .let { actionStartActivity(it) }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}