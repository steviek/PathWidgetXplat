package com.sixbynine.transit.path.resources

import com.sixbynine.transit.path.PathApplication
import dev.icerock.moko.resources.StringResource

actual fun getString(resource: StringResource): String {
    return resource.getString(PathApplication.instance)
}

actual fun getString(resource: StringResource, arg1: String): String {
    return PathApplication.instance.getString(resource.resourceId, arg1)
}
