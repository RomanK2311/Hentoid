package me.devsaki.hentoid.customssiv

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Helper class used to set the source and additional attributes from a variety of sources. Supports
 * use of a bitmap, asset, resource, external file or any other URI.
 * <p>
 * When you are using a preview image, you must set the dimensions of the full size image on the
 * ImageSource object for the full size image using the {@link #dimensions(int, int)} method.
 */
const val FILE_SCHEME = "file:///"
const val ASSET_SCHEME = "file:///android_asset/"


/**
 * Create an instance from a resource. The correct resource for the device screen resolution will be used.
 *
 * @param resId resource ID.
 * @return an [ImageSourceK] instance.
 */
fun resource(resId: Int): ImageSourceK {
    return ImageSourceK(resId)
}

/**
 * Create an instance from an asset name.
 *
 * @param assetName asset name.
 * @return an [ImageSourceK] instance.
 */
fun asset(assetName: String): ImageSourceK {
    return uri(ASSET_SCHEME + assetName)
}

/**
 * Create an instance from a URI. If the URI does not start with a scheme, it's assumed to be the URI
 * of a file.
 *
 * @param uri image URI.
 * @return an [ImageSourceK] instance.
 */
fun uri(uri: String): ImageSourceK {
    var uri = uri
    if (!uri.contains("://")) {
        if (uri.startsWith("/")) {
            uri = uri.substring(1)
        }
        uri = FILE_SCHEME + uri
    }
    return ImageSourceK(Uri.parse(uri))
}

/**
 * Create an instance from a URI.
 *
 * @param uri image URI.
 * @return an [ImageSourceK] instance.
 */
fun uri(uri: Uri): ImageSourceK {
    return ImageSourceK(uri)
}

/**
 * Provide a loaded bitmap for display.
 *
 * @param bitmap bitmap to be displayed.
 * @return an [ImageSourceK] instance.
 */
fun bitmap(bitmap: Bitmap): ImageSourceK {
    return ImageSourceK(bitmap, false)
}

/**
 * Provide a loaded and cached bitmap for display. This bitmap will not be recycled when it is no
 * longer needed. Use this method if you loaded the bitmap with an image loader such as Picasso
 * or Volley.
 *
 * @param bitmap bitmap to be displayed.
 * @return an [ImageSource] instance.
 */
fun cachedBitmap(bitmap: Bitmap): ImageSourceK {
    return ImageSourceK(bitmap, true)
}

@SuppressWarnings("unused", "WeakerAccess")
class ImageSourceK {
    private var uri: Uri? = null
    private var bitmap: Bitmap? = null
    private var resource: Int? = null
    private var tile = false
    private var sWidth = 0
    private var sHeight = 0
    private var sRegion: Rect? = null
    private var cached = false

    internal constructor(bitmap: Bitmap, cached: Boolean) {
        this.bitmap = bitmap
        this.uri = null
        this.resource = null
        this.tile = false
        this.sWidth = bitmap.width
        this.sHeight = bitmap.height
        this.cached = cached
    }

    internal constructor(uri: Uri) {
        // #114 If file doesn't exist, attempt to url decode the URI and try again
        var uri = uri
        val uriString = uri.toString()
        if (uriString.startsWith(FILE_SCHEME)) {
            val uriFile = File(uriString.substring(FILE_SCHEME.length - 1))
            if (!uriFile.exists()) {
                try {
                    uri = Uri.parse(URLDecoder.decode(uriString, "UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    // Fallback to encoded URI. This exception is not expected.
                }
            }
        }
        this.bitmap = null
        this.uri = uri
        this.resource = null
        this.tile = true
    }

    internal constructor(resource: Int) {
        this.bitmap = null
        this.uri = null
        this.resource = resource
        this.tile = true
    }

    /**
     * Enable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap.,
     * and tiling cannot be disabled when displaying a region of the source image.
     *
     * @return this instance for chaining.
     */
    fun tilingEnabled(): ImageSourceK {
        return tiling(true)
    }

    /**
     * Disable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap,
     * and tiling cannot be disabled when displaying a region of the source image.
     *
     * @return this instance for chaining.
     */
    fun tilingDisabled(): ImageSourceK {
        return tiling(false)
    }

    /**
     * Enable or disable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap,
     * and tiling cannot be disabled when displaying a region of the source image.
     *
     * @param tile whether tiling should be enabled.
     * @return this instance for chaining.
     */
    fun tiling(tile: Boolean): ImageSourceK {
        this.tile = tile
        return this
    }

    /**
     * Use a region of the source image. Region must be set independently for the full size image and the preview if
     * you are using one.
     *
     * @param sRegion the region of the source image to be displayed.
     * @return this instance for chaining.
     */
    fun region(sRegion: Rect?): ImageSourceK {
        this.sRegion = sRegion
        setInvariants()
        return this
    }

    /**
     * Declare the dimensions of the image. This is only required for a full size image, when you are specifying a URI
     * and also a preview image. When displaying a bitmap object, or not using a preview, you do not need to declare
     * the image dimensions. Note if the declared dimensions are found to be incorrect, the view will reset.
     *
     * @param sWidth  width of the source image.
     * @param sHeight height of the source image.
     * @return this instance for chaining.
     */
    fun dimensions(sWidth: Int, sHeight: Int): ImageSourceK {
        if (bitmap == null) {
            this.sWidth = sWidth
            this.sHeight = sHeight
        }
        setInvariants()
        return this
    }

    private fun setInvariants() {
        sRegion?.let {
            this.tile = true
            this.sWidth = it.width()
            this.sHeight = it.height()
        }
    }

    protected fun getUri(): Uri? {
        return uri
    }

    protected fun getBitmap(): Bitmap? {
        return bitmap
    }

    protected fun getResource(): Int? {
        return resource
    }

    protected fun getTile(): Boolean {
        return tile
    }

    protected fun getSWidth(): Int {
        return sWidth
    }

    protected fun getSHeight(): Int {
        return sHeight
    }

    protected fun getSRegion(): Rect? {
        return sRegion
    }

    protected fun isCached(): Boolean {
        return cached
    }
}