package com.desaiwang.transit.path

import com.desaiwang.transit.path.platform.IsDebug
import com.desaiwang.transit.path.time.now
import com.desaiwang.transit.path.util.IsTest
import com.desaiwang.transit.path.util.globalDataStore
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object Logging {
    var isDeveloperMode = false
        set(value) {
            field = value
            if (value) {
                devLoggingStore = DeveloperModeLoggingStore()
            } else {
                devLoggingStore?.close()
                devLoggingStore = null
            }
        }

    var nonFatalReporter: NonFatalReporter? = null

    private val hasInitialized = MutableStateFlow(false)

    private var devLoggingStore: DeveloperModeLoggingStore? = null

    fun initialize() {
        if (hasInitialized.compareAndSet(expect = false, update = true)) {
            Napier.base(DebugAntilog())
        }
    }

    fun d(message: String) {
        if (IsTest) {
            println(message)
            return
        }
        if (!IsDebug && !isDeveloperMode) return
        initialize()
        Napier.d(message)
        devLoggingStore?.log('D', message)
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (IsTest) {
            println(message)
            return
        }
        initialize()
        Napier.w(message, throwable)
        devLoggingStore?.log('W', message + (throwable?.message?.let { ": $it" } ?: ""))
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (IsTest) {
            println(message)
            return
        }
        initialize()
        Napier.e(message, throwable)
        devLoggingStore?.log('E', message + (throwable?.message?.let { ": $it" } ?: ""))
        reportNonFatal(throwable)
    }

    fun getLogRecords(): List<LogRecord> {
        return devLoggingStore?.getRecords()?.records ?: emptyList()
    }

    private fun reportNonFatal(e: Throwable?) {
        if (IsTest) return
        val reporter = nonFatalReporter ?: return
        if (e == null) return
        when (e) {
            is CancellationException -> return
        }
        reporter.report(e)
    }
}

private class DeveloperModeLoggingStore {
    private val dataStore = globalDataStore()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    private val logRecords = ArrayDeque<LogRecord>()

    init {
        coroutineScope.launch {
            while (true) {
                delay(5.seconds)
                if (logRecords.isEmpty()) continue
                val records = getRecords().records.toMutableList()
                records.removeAll { it.timestamp < now() - 48.hours }
                mutex.withLock {
                    while (logRecords.isNotEmpty()) {
                        val record = logRecords.removeFirst()
                        records.add(record)
                    }
                }
                val json = Json.encodeToString(LogRecords(records))
                dataStore[Key] = json
            }
        }
    }

    fun log(level: Char, message: String) {
        coroutineScope.launch {
            mutex.withLock {
                logRecords.addLast(LogRecord(level, message))
            }
        }
    }

    internal fun getRecords(): LogRecords {
        val json = dataStore.getString(Key) ?: return EmptyLogRecords
        return runCatching {
            Json.decodeFromString<LogRecords>(json)
        }.getOrElse {
            EmptyLogRecords
        }
    }

    fun close() {
        dataStore[Key] = null as String?
        coroutineScope.cancel()
    }

    private companion object {
        const val Key = "developer_mode_logs"
    }
}

@Serializable(with = LogRecordSerializer::class)
data class LogRecord(val level: Char, val message: String, val timestamp: Instant = now())

private class LogRecordSerializer : KSerializer<LogRecord> {
    override val descriptor = PrimitiveSerialDescriptor("LogRecord", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LogRecord {
        val (timestamp, level, message) = decoder.decodeString().split(';')
        return LogRecord(level.first(), message, Instant.fromEpochMilliseconds(timestamp.toLong()))
    }

    override fun serialize(encoder: Encoder, value: LogRecord) {
        val packed = "${value.timestamp.toEpochMilliseconds()};${value.level};${value.message}"
        encoder.encodeString(packed)
    }
}

@Serializable
private data class LogRecords(val records: List<LogRecord>)

private val EmptyLogRecords = LogRecords(emptyList())
