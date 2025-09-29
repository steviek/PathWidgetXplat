package com.desaiwang.transit.path.api.templine

import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.RemoteFileRepository
import com.desaiwang.transit.path.util.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object HobClosureConfigRepository {

    init {
        GlobalScope.launch {
            delay(2.seconds)
            helper.get(now()).await()
        }
    }

    private val helper = RemoteFileRepository(
        keyPrefix = "hob_closue_config",
        url = "https://raw.githubusercontent.com/steviek/PathWidgetXplat/main/hob_closure.json",
        serializer = HobClosureConfig.serializer(),
        maxAge = 1.hours,
    )

    fun getConfig(): HobClosureConfig {
        return helper.get(now()).previous?.value ?: HobClosureConfig.fallback
    }
}
