package com.sixbynine.transit.path.app.settings

import com.sixbynine.transit.path.LogRecord
import kotlinx.cinterop.BetaInteropApi
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
        logs.joinToString(separator = "\n", prefix = "timestamp,level,message\n") {
            it.timestamp.toString() + "," +
                    it.level.toString() + "," +
                    it.message.replace(",", "\",\"")
        }

    val cacheDirectory =
        NSFileManager
            .defaultManager
            .URLsForDirectory(directory = NSCachesDirectory, inDomains = NSUserDomainMask)
            .firstNotNullOfOrNull { it as? NSURL }
            ?: return

    val filePath = NSURL.fileURLWithPath("logs.csv", relativeToURL = cacheDirectory)

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
