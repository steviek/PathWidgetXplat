package com.sixbynine.transit.path.flipper

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader

object FlipperUtil {

    private val networkPlugin = NetworkFlipperPlugin()

    fun initialize(context: Context) {
        SoLoader.init(context, false)

        val client = AndroidFlipperClient.getInstance(context)

        client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
        client.addPlugin(SharedPreferencesFlipperPlugin(context, "path"))
        client.addPlugin(networkPlugin)

        client.start()
    }

    fun interceptor(): Any? {
        return FlipperOkhttpInterceptor(networkPlugin)
    }
}