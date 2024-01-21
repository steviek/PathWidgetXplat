package com.sixbynine.transit.path.resources

import android.content.Context
import com.sixbynine.transit.path.PathApplication
import com.sixbynine.transit.path.PreviewContext
import dev.icerock.moko.resources.StringResource

actual fun getString(resource: StringResource): String {
    return resource.getString(context)
}

actual fun getString(resource: StringResource, arg1: String): String {
    return context.getString(resource.resourceId, arg1)
}

actual fun getString(resource: StringResource, arg1: String, arg2: String): String {
    return context.getString(resource.resourceId, arg1, arg2)
}

private val context: Context
    get() = PreviewContext ?: PathApplication.instance
