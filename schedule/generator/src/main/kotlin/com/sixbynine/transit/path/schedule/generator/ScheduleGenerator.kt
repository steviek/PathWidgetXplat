package com.sixbynine.transit.path.schedule.generator

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.util.readRemoteFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

private val jobComplete = CompletableDeferred<Unit>()

fun main() {
    val taskScope = CoroutineScope(Dispatchers.Default)
    taskScope.launch { ScheduleGenerator.generateSchedule() }

    runBlocking {
        jobComplete.await()
        exitProcess(0)
    }
}

object ScheduleGenerator {
    suspend fun generateSchedule() {
        val response =
            readRemoteFile("https://www.panynj.gov/content/path/en.model.json")
                .getOrElse {
                    Logging.e("Failed to reach path model", it)
                    exitProcess(1)
                }

        ScheduleParser.parse(response, "regular")

        jobComplete.complete(Unit)
    }
}

