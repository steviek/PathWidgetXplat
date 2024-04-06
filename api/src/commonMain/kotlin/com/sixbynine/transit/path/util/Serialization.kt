package com.sixbynine.transit.path.util

import kotlinx.serialization.json.Json

val JsonFormat = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}
