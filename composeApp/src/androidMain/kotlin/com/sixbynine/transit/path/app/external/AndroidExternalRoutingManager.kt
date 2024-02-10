package com.sixbynine.transit.path.app.external

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.sixbynine.transit.path.MR.strings
import com.sixbynine.transit.path.app.ui.ActivityRegistry
import com.sixbynine.transit.path.resources.getString
import com.sixbynine.transit.path.util.suspendRunCatching
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AndroidExternalRoutingManager : ExternalRoutingManager {

    override fun openEmail(): Boolean {
        val activity = ActivityRegistry.peekCreatedActivity() ?: return false
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("sixbynineapps@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, getString(strings.app_name))
        }

        return runCatching { activity.startActivity(intent) }.isSuccess
    }

    override fun shareTextToSystem(text: String): Boolean {
        val activity = ActivityRegistry.peekCreatedActivity() ?: return false
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooserIntent = Intent.createChooser(intent, getString(strings.share))

        return runCatching { activity.startActivity(chooserIntent) }.isSuccess
    }

    override suspend fun launchAppRating(): Boolean {
        val activity = ActivityRegistry.peekCreatedActivity() ?: return false
        val manager = ReviewManagerFactory.create(activity)
        return withContext(activity.lifecycleScope.coroutineContext) {
            val request = suspendRunCatching {
                manager.requestReviewFlow().await()
            }.getOrElse {
                Firebase.crashlytics.recordException(ReviewFlowFailedException(it))
                return@withContext activity.openPlayStoreUrl()
            }

            suspendRunCatching { manager.launchReviewFlow(activity, request).await() }
                .fold(
                    onSuccess = { true },
                    onFailure = {
                        Firebase.crashlytics.recordException(ReviewFlowFailedException(it))
                        activity.openPlayStoreUrl()
                    }
                )
        }
    }
}

private fun Activity.openPlayStoreUrl(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data =
        "https://play.google.com/store/apps/details?id=com.sixbynine.transit.path".toUri()
    return runCatching { startActivity(intent) }.isSuccess
}

class FailedToLaunchRatingException(cause: Throwable?) : RuntimeException(cause)
class ReviewFlowFailedException(cause: Throwable?) : RuntimeException(cause)


actual fun ExternalRoutingManager(): ExternalRoutingManager = AndroidExternalRoutingManager