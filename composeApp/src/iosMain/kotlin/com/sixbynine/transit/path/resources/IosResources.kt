package com.sixbynine.transit.path.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

actual fun getString(resource: StringResource): String {
    return StringDesc.Resource(resource).localized()
}

actual fun getString(resource: StringResource, arg1: String): String {
    return StringDesc.ResourceFormatted(resource, arg1).localized()
}

actual fun getString(resource: StringResource, arg1: String, arg2: String): String {
    return StringDesc.ResourceFormatted(resource, arg1, arg2).localized()
}
