package com.desaiwang.transit.path.flipper

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin.SharedPreferencesDescriptor
import com.facebook.soloader.SoLoader

object FlipperUtil {

    private val networkPlugin = NetworkFlipperPlugin()

    fun initialize(context: Context) {
        SoLoader.init(context, false)

        val client = AndroidFlipperClient.getInstance(context)

        val sharedPreferencesDescriptors =
            listOf("path", "widget_data")
                .map { SharedPreferencesDescriptor(it, Context.MODE_PRIVATE) }

        client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
        client.addPlugin(SharedPreferencesFlipperPlugin(context, sharedPreferencesDescriptors))
        client.addPlugin(networkPlugin)

        client.start()
    }

    fun interceptor(): Any? {
        return FlipperOkhttpInterceptor(networkPlugin)
    }
}