package com.sixbynine.transit.path.schedule.generator

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.util.readRemoteFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
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
    private val json = Json { }

    suspend fun generateSchedule() {
        val response =
            readRemoteFile("https://www.panynj.gov/content/path/en.model.json")
                .getOrElse {
                    Logging.e("Failed to reach path model", it)
                    exitProcess(1)
                }

        val schedules = ScheduleParser.parse(response, "regular")

        val outputDir = File("build/outputs")
        outputDir.mkdirs()

        val outputFile = File(outputDir, "schedule.json")
        val jsonString = json.encodeToString(schedules)
        outputFile.writeText(jsonString)

        println("Schedule written to: ${outputFile.absolutePath} successfully.")

        jobComplete.complete(Unit)
    }
}

