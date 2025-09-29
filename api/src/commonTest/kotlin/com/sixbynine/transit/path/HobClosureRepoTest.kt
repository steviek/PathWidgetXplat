package com.desaiwang.transit.path

import com.desaiwang.transit.path.api.templine.HobClosureConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class HobClosureRepoTest {
    @Test
    fun foo() {
        val config = HobClosureConfig.fallback
        println(Json.encodeToString(config))
    }
}