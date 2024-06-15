package me.devsaki.hentoid.parsers.content

import android.net.Uri
import me.devsaki.hentoid.database.domains.Content
import me.devsaki.hentoid.enums.Site
import me.devsaki.hentoid.enums.StatusContent
import me.devsaki.hentoid.parsers.getUserAgent
import me.devsaki.hentoid.retrofit.sources.PixivServer
import me.devsaki.hentoid.util.isNumeric
import me.devsaki.hentoid.util.network.getCookies
import timber.log.Timber
import java.io.IOException

class PixivContent : BaseContentParser() {
    override fun update(content: Content, url: String, updateImages: Boolean): Content {
        val urlParts = url.split("/")
        var id = urlParts[urlParts.size - 1]
        if (id.contains("?")) {
            id = id.substring(0, id.indexOf("?"))
        }
        val entity = urlParts[urlParts.size - 2]
        val uri = Uri.parse(url)
        when (entity) {
            "artworks", "illust" -> if (!isNumeric(id)) id =
                uri.getQueryParameter("illust_id") ?: ""

            "user", "users" -> if (!isNumeric(id)) id = uri.getQueryParameter("id") ?: ""

            else -> {}
        }
        try {
            if (id.isNotEmpty()) {
                val cookieStr = getCookies(
                    url,
                    null,
                    Site.PIXIV.useMobileAgent(),
                    Site.PIXIV.useHentoidAgent(),
                    Site.PIXIV.useWebviewAgent()
                )
                val userAgent = getUserAgent(Site.PIXIV)
                val acceptAll = "*/*"
                when (entity) {
                    "artworks", "illust" -> {
                        val metadata =
                            PixivServer.api.getIllustMetadata(id, cookieStr, acceptAll, userAgent)
                                .execute().body()
                        if (metadata != null) return metadata.update(content, url, updateImages)
                    }

                    "series_content", "series" -> {
                        val seriesData =
                            PixivServer.api.getSeriesMetadata(id, cookieStr, acceptAll, userAgent)
                                .execute().body()
                        if (seriesData != null) return seriesData.update(content, updateImages)
                    }

                    "user", "users" -> {
                        val userData =
                            PixivServer.api.getUserMetadata(id, cookieStr, acceptAll, userAgent)
                                .execute().body()
                        if (userData != null) return userData.update(content, updateImages)
                    }

                    else -> {}
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Error parsing content.")
        }
        return Content().setSite(Site.PIXIV).setStatus(StatusContent.IGNORED)
    }
}