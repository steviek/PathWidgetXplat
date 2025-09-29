package com.desaiwang.transit.path.app.settings

import com.desaiwang.transit.path.LogRecord
import com.desaiwang.transit.path.time.NewYorkTimeZone
import kotlinx.cinterop.BetaInteropApi
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

@OptIn(BetaInteropApi::class)
actual fun exportDevLogs(logs: List<LogRecord>) {
    val csvString =
        logs.joinToString(separator = "\n", prefix = "timestamp\tlevel\tmessage\n") {
            it.timestamp.toLocalDateTime(NewYorkTimeZone).toString() +
                    "\t" + it.level.toString() +
                    "\t" + it.message
        }

    val cacheDirectory =
        NSFileManager
            .defaultManager
            .URLsForDirectory(directory = NSCachesDirectory, inDomains = NSUserDomainMask)
            .firstNotNullOfOrNull { it as? NSURL }
            ?: return

    val filePath = NSURL.fileURLWithPath("logs.tsv", relativeToURL = cacheDirectory)

    NSString.create(format = csvString)
        .writeToURL(filePath, atomically = true)

    val viewController =
        UIApplication.sharedApplication
            .windows
            .firstNotNullOfOrNull { (it as? UIWindow)?.rootViewController }
            ?: return

    val activityViewController = UIActivityViewController(listOf(filePath), null)
    viewController.presentViewController(
        activityViewController,
        animated = true,
        completion = null,
    )
}
