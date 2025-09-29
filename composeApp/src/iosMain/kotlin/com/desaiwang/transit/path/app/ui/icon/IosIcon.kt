package com.desaiwang.transit.path.app.ui.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.desaiwang.transit.path.app.ui.icon.IconType.ArrowDown
import com.desaiwang.transit.path.app.ui.icon.IconType.ArrowUp
import com.desaiwang.transit.path.app.ui.icon.IconType.Back
import com.desaiwang.transit.path.app.ui.icon.IconType.Delete
import com.desaiwang.transit.path.app.ui.icon.IconType.Edit
import com.desaiwang.transit.path.app.ui.icon.IconType.ExpandDown
import com.desaiwang.transit.path.app.ui.icon.IconType.Filter
import com.desaiwang.transit.path.app.ui.icon.IconType.Internet
import com.desaiwang.transit.path.app.ui.icon.IconType.LayoutOneColumn
import com.desaiwang.transit.path.app.ui.icon.IconType.Settings
import com.desaiwang.transit.path.app.ui.icon.IconType.Sort
import com.desaiwang.transit.path.app.ui.icon.IconType.Station
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy


@Composable
actual fun IconPainter(icon: IconType): Painter {
    val imageName = when (icon) {
        Edit -> "pencil"
        Station -> "tram.fill"
        Sort -> "arrow.up.arrow.down"
        Filter -> "line.3.horizontal.decrease"
        LayoutOneColumn -> "rectangle.portrait"
        ArrowUp -> "arrow.up"
        ArrowDown -> "arrow.down"
        Settings -> "gearshape.fill"
        Delete -> "minus.circle.fill"
        Back -> "chevron.left"
        ExpandDown -> "chevron.down.circle.fill"
        Internet -> "safari.fill"
    }
    val bitmap = rememberImageBitmapWithSystemName(imageName)
    return BitmapPainter(bitmap)
}

private fun ImageBitmapWithSystemName(systemName: String): ImageBitmap {
    return requireNotNull(UIImage.systemImageNamed(systemName)) {
        "Image with name $systemName not found"
    }.toComposeImageBitmap()
}

@Composable
private fun rememberImageBitmapWithSystemName(systemName: String): ImageBitmap {
    return remember(systemName) {
        ImageBitmapWithSystemName(systemName)
    }
}

private fun UIImage.toComposeImageBitmap(): ImageBitmap {
    val bytes = requireNotNull(UIImagePNGRepresentation(this)) {
        "Failed to get PNG representation of image"
    }

    return Image.makeFromEncoded(bytes.toByteArray())
        .toComposeImageBitmap()
}

private fun NSData.toByteArray(): ByteArray = ByteArray(this@toByteArray.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
}
