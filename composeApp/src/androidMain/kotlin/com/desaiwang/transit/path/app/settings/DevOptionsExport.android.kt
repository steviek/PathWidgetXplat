package com.desaiwang.transit.path.app.settings

import android.content.Intent
import androidx.core.content.FileProvider
import com.desaiwang.transit.path.LogRecord
import com.desaiwang.transit.path.PathApplication
import com.desaiwang.transit.path.app.ui.ActivityRegistry
import com.desaiwang.transit.path.time.NewYorkTimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

actual fun exportDevLogs(logs: List<LogRecord>) {
    val context = PathApplication.instance
    val activity = ActivityRegistry.peekCreatedActivity() ?: return
    val filename = "logs.tsv"
    val logsDir = File(context.cacheDir, "logs")
    logsDir.mkdirs()
    val file = File(logsDir, filename)
    file.createNewFile()

    BufferedWriter(FileWriter(file)).use { writer ->
        writer.write("timestamp\tlevel\tmessage")
        writer.newLine()

        logs.asReversed().forEach { log ->
            writer.write(log.timestamp.toLocalDateTime(NewYorkTimeZone).toString())
            writer.write("\t")
            writer.write(log.level.toString())
            writer.write("\t")
            writer.write(log.message)
            writer.newLine()
        }
    }

    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/tsv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    activity.startActivity(Intent.createChooser(shareIntent, "Share TSV"))
}