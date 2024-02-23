package me.devsaki.hentoid.json.sources

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.devsaki.hentoid.database.domains.Attribute
import me.devsaki.hentoid.database.domains.AttributeMap
import me.devsaki.hentoid.database.domains.Content
import me.devsaki.hentoid.database.domains.ImageFile
import me.devsaki.hentoid.enums.AttributeType
import me.devsaki.hentoid.enums.Site
import me.devsaki.hentoid.enums.StatusContent
import me.devsaki.hentoid.util.Helper
import me.devsaki.hentoid.util.StringHelper
import timber.log.Timber

@JsonClass(generateAdapter = true)
data class DeviantArtDeviation(
    val deviation: Deviation
) {
    @JsonClass(generateAdapter = true)
    data class Deviation(
        val title: String,
        val url: String,
        val publishedTime: String,
        val author: Author,
        val media: Media,
        val extended: ExtendedData
    )

    @JsonClass(generateAdapter = true)
    data class Author(
        val username: String
    )

    @JsonClass(generateAdapter = true)
    data class Media(
        val baseUri: String,
        val prettyName: String,
        val token: List<String>,
        val types: List<MediaType>
    ) {
        fun getThumbUrl(): String {
            return types.minByOrNull { it.width }?.getUrl(baseUri, prettyName, token[0]) ?: ""
        }

        fun getPictureUrl(): String {
            return types.maxByOrNull { it.width }?.getUrl(baseUri, prettyName, token[0]) ?: ""
        }
    }

    @JsonClass(generateAdapter = true)
    data class MediaType(
        @Json(name = "t")
        val title: String,
        @Json(name = "c")
        val path: String?, // Not always present for fullview
        @Json(name = "h")
        val height: Int,
        @Json(name = "w")
        val width: Int
    ) {
        fun getUrl(baseUri: String, prettyName: String, token: String): String {
            if (null == path) return ""
            return baseUri.replace("\\/", "/") +
                    path.replace("\\/", "/").replace("<prettyName>", prettyName) +
                    "?token=${token}"
        }
    }

    @JsonClass(generateAdapter = true)
    data class ExtendedData(
        val tags: List<Tag>,
        val download: DownloadData?
    )

    @JsonClass(generateAdapter = true)
    data class Tag(
        val name: String,
        val url: String
    )

    @JsonClass(generateAdapter = true)
    data class DownloadData(
        val url: String
    )

    fun update(content: Content, updateImages: Boolean): Content {
        content.setSite(Site.DEVIANTART)

        deviation.apply {
            content.setRawUrl(url.replace("\\/", "/"))
            try {
                if (publishedTime.isNotEmpty())
                    content.uploadDate = Helper.parseDatetimeToEpoch(
                        publishedTime,
                        "yyyy-MM-dd'T'HH:mm:ssz"
                    ) // e.g. 2024-01-22T08:27:45-0800
            } catch (t: Throwable) {
                Timber.w(t)
            }

            content.setTitle(StringHelper.removeNonPrintableChars(title))

            val attributes = AttributeMap()
            val artist = Attribute(
                AttributeType.ARTIST,
                author.username,
                RELATIVE_URL_PREFIX + author.username,
                Site.DEVIANTART
            )
            attributes.add(artist)

            extended.tags.forEach {
                val tag = Attribute(
                    AttributeType.TAG,
                    StringHelper.removeNonPrintableChars(it.name),
                    it.url.replace("\\/", "/"),
                    Site.DEVIANTART
                )
                attributes.add(tag)
            }
            content.putAttributes(attributes)

            content.setCoverImageUrl(media.getThumbUrl())

            if (updateImages) {
                val downloadUrl = extended.download?.url?.replace("\\/", "/") ?: ""
                val pictureUrl = media.getPictureUrl()
                val picture = if (downloadUrl.isNotEmpty()) {
                    val img = ImageFile.fromImageUrl(1, downloadUrl, StatusContent.SAVED, 1)
                    img.backupUrl = pictureUrl
                    img
                } else ImageFile.fromImageUrl(1, pictureUrl, StatusContent.SAVED, 1)

                val result = ArrayList<ImageFile>()
                if (media.getThumbUrl().isNotEmpty()) result.add(
                    ImageFile.newCover(media.getThumbUrl(), StatusContent.SAVED)
                )
                result.add(picture)
                content.setQtyPages(1)
            }
        }
        return content
    }

    companion object {
        private const val RELATIVE_URL_PREFIX = "https://www.deviantart.com/"
    }
}
