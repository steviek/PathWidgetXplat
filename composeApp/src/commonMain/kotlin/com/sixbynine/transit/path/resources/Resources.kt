package com.sixbynine.transit.path.resources

import dev.icerock.moko.resources.StringResource

expect fun getString(resource: StringResource): String

expect fun getString(resource: StringResource, arg1: String): String

