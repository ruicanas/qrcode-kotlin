package io.github.g0dkar.qrcode.render

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import javax.imageio.ImageIO

actual class QRCodeGraphics actual constructor(
    val width: Int,
    val height: Int
) {
    private val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val colorCache = mutableMapOf<Int, Color>()

    private fun draw(color: Int, action: (Graphics2D) -> Unit) {
        val graphics = image.createGraphics()
        val jdkColor = colorCache.computeIfAbsent(color) { Color(color, true) }

        graphics.color = jdkColor
        graphics.background = jdkColor
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        action(graphics)

        graphics.dispose()
    }

    /**
     * Returns this image as a [ByteArray] encoded as PNG. Usually recommended to use [writeImage] instead :)
     *
     * @see writeImage
     */
    actual fun getBytes(): ByteArray = getBytes("PNG")

    /**
     * Returns this image as a [ByteArray] encoded as the specified format. Usually recommended to use [writeImage]
     * instead :)
     *
     * @see writeImage
     */
    actual fun getBytes(format: String): ByteArray =
        ByteArrayOutputStream().let {
            writeImage(it, format)
            it.toByteArray()
        }

    /**
     * Writes the QRCode image in the specified [format] into the destination [OutputStream].
     *
     * @see ImageIO.write
     * @throws UnsupportedOperationException No suitable Image Writer was found for the specified format.
     */
    @JvmOverloads
    fun writeImage(destination: OutputStream, format: String = "PNG") {
        val wasImageWritten = ImageIO.write(image, format, destination)

        if (!wasImageWritten) {
            throw UnsupportedOperationException("Unsupported format: $format")
        }
    }

    /**
     * Returns the available formats to be passed as parameters to [getBytes].
     *
     * @see ImageIO.getWriterFileSuffixes
     */
    actual fun availableFormats(): List<String> = ImageIO.getWriterFileSuffixes().toList()

    /** Returns the [BufferedImage] object being worked upon. */
    actual fun nativeImage(): Any = image

    /** Draw a straight line from point `(x1,y1)` to `(x2,y2)`. */
    actual fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        draw(color) { it.drawLine(x1, y1, x2, y2) }
    }

    /** Draw the edges of a rectangle starting at point `(x,y)` and having `width` by `height`. */
    actual fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int) {
        draw(color) { it.drawRect(x, y, width, height) }
    }

    /** Fills the rectangle starting at point `(x,y)` and having `width` by `height`. */
    actual fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Int) {
        draw(color) { it.fillRect(x, y, width, height) }
    }

    /** Fill the whole area of this canvas with the especified [color]. */
    actual fun fill(color: Int) {
        fillRect(0, 0, width, height, color)
    }

    /**
     * Draw the edges of a round rectangle starting at point `(x,y)` and having `width` by `height`
     * with edges that are `borderRadius` pixels round (almost like CSS).
     *
     * If it helps, these would _in theory_ draw the same thing:
     *
     * ```
     * // CSS
     * .roundRect {
     *     width: 100px;
     *     height: 100px;
     *     border-radius: 5px;
     * }
     *
     * // Kotlin
     * drawRoundRect(0, 0, 100, 100, 5)
     * ```
     *
     * **Note:** you can't especify different sizes for different edges. This is just an example :)
     *
     */
    actual fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, borderRadius: Int, color: Int) {
        draw(color) { it.drawRoundRect(x, y, width, height, borderRadius, borderRadius) }
    }

    /**
     * Fills the round rectangle starting at point `(x,y)` and having `width` by `height`
     * with edges that are `borderRadius` pixels round (almost like CSS).
     *
     * If it helps, these would _in theory_ draw the same thing:
     *
     * ```
     * // CSS
     * .roundRect {
     *     width: 100px;
     *     height: 100px;
     *     border-radius: 5px;
     * }
     *
     * // Kotlin
     * drawRoundRect(0, 0, 100, 100, 5)
     * ```
     *
     * **Note:** you can't especify different sizes for different edges. This is just an example :)
     *
     */
    actual fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, borderRadius: Int, color: Int) {
        draw(color) { it.fillRoundRect(x, y, width, height, borderRadius, borderRadius) }
    }

    /** Draw an image inside another. Mostly used to merge squares into the main QRCode. */
    actual fun drawImage(img: QRCodeGraphics, x: Int, y: Int) {
        draw(0) {
            it.drawImage(img.image, x, y, null)
        }
    }
}
