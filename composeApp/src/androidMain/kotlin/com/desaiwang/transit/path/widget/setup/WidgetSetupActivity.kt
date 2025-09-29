package com.desaiwang.transit.path.widget.setup

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.desaiwang.transit.path.BaseActivity
import com.desaiwang.transit.path.widget.setup.WidgetSetupScreenContract.Effect.CompleteConfigurationIntent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WidgetSetupActivity : BaseActivity() {

    private val viewModel: WidgetSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setAppWidgetId(intent.getAppWidgetId())

        viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is CompleteConfigurationIntent -> {
                        val resultValue = Intent().putExtra(EXTRA_APPWIDGET_ID, effect.appWidgetId)
                        setResult(RESULT_OK, resultValue)
                        finish()
                    }
                }
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)

        setContent {
            WidgetSetupScreen(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.setAppWidgetId(intent.getAppWidgetId())
    }

    private fun Intent?.getAppWidgetId(): Int {
        this ?: return INVALID_APPWIDGET_ID
        return extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID
    }
}
