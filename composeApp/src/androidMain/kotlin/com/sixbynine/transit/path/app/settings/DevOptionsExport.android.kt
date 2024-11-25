package com.sixbynine.transit.path.app.settings

import android.content.Intent
import androidx.core.content.FileProvider
import com.sixbynine.transit.path.LogRecord
import com.sixbynine.transit.path.PathApplication
import com.sixbynine.transit.path.app.ui.ActivityRegistry
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

actual fun exportDevLogs(logs: List<LogRecord>) {
    val context = PathApplication.instance
    val activity = ActivityRegistry.peekCreatedActivity() ?: return
    val filename = "logs.csv"
    val logsDir = File(context.cacheDir, "logs")
    logsDir.mkdirs()
    val file = File(logsDir, filename)
    file.createNewFile()

    BufferedWriter(FileWriter(file)).use { writer ->
        writer.write("timestamp,level,message")
        writer.newLine()

        logs.forEach { log ->
            writer.write(log.timestamp.toString())
            writer.write(",")
            writer.write(log.level.toString())
            writer.write(",")
            writer.write(log.message)
            writer.newLine()
        }
    }

    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    activity.startActivity(Intent.createChooser(shareIntent, "Share CSV"))
}