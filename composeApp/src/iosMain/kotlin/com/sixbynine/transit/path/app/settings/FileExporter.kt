package com.sixbynine.transit.path.app.settings

interface FileExporter {
    fun export(name: String, content: String)
}

object FileExporterHolder {
    var exporter: FileExporter? = null
}
