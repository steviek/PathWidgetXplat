package com.sixbynine.transit.path.widget.setup

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Effect.CompleteConfigurationIntent
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Effect.LaunchLocationPermissionRequest
import com.sixbynine.transit.path.widget.setup.WidgetSetupScreenContract.Intent.PermissionRequestComplete
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WidgetSetupActivity : ComponentActivity() {

    private val viewModel: WidgetSetupViewModel by viewModels()

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionRequest =
            registerForActivityResult(RequestMultiplePermissions()) { permissions ->
                viewModel.onIntent(PermissionRequestComplete(permissions))
            }

        viewModel.setAppWidgetId(intent.getAppWidgetId())

        viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is LaunchLocationPermissionRequest -> {
                        locationPermissionRequest.launch(effect.permissions)
                    }

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
