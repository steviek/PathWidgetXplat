package com.sixbynine.transit.path.schedule.generator

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class ScheduleParserTest {

    private val json = Json {

    }

    @Test
    fun parsing() {
        val text = ScheduleParserTest::class.java.getResource("pathModel.json")!!.readText()

        val schedules = ScheduleParser.parse(text, "regular")

        val json = json.encodeToString(schedules)

        println(json)
    }
}
