package me.devsaki.hentoid.parsers.content

import me.devsaki.hentoid.activities.sources.Hentai2ReadActivity
import me.devsaki.hentoid.database.domains.AttributeMap
import me.devsaki.hentoid.database.domains.Content
import me.devsaki.hentoid.enums.AttributeType
import me.devsaki.hentoid.enums.Site
import me.devsaki.hentoid.enums.StatusContent
import me.devsaki.hentoid.parsers.ParseHelper
import me.devsaki.hentoid.parsers.images.Hentai2ReadParser
import me.devsaki.hentoid.util.StringHelper
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.annotation.Selector
import timber.log.Timber
import java.io.IOException
import java.util.Locale
import java.util.regex.Pattern

class Hentai2ReadContent : BaseContentParser() {
    companion object {
        private val GALLERY_PATTERN = Pattern.compile(Hentai2ReadActivity.GALLERY_PATTERN)
    }

    @Selector(value = "div.img-container img[src*=cover]")
    private var cover: Element? = null

    @Selector(value = "span[itemprop^=name]")
    private var title: List<Element>? = null

    @Selector("ul.list li")
    private var properties: List<Element>? = null

    @Selector(value = "li.dropdown a[data-mid]", attr = "data-mid", defValue = "")
    private lateinit var uniqueId: String

    @Selector(value = "script")
    private var scripts: List<Element>? = null


    override fun update(content: Content, url: String, updateImages: Boolean): Content {
        content.setSite(Site.HENTAI2READ)
        if (url.isEmpty()) return Content().setStatus(StatusContent.IGNORED)
        content.setRawUrl(url)
        return if (GALLERY_PATTERN.matcher(url).find()) updateGallery(
            content,
            updateImages
        ) else updateSingleChapter(content, url, updateImages)
    }

    private fun updateSingleChapter(
        content: Content,
        url: String,
        updateImages: Boolean
    ): Content {
        val urlParts = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (urlParts.size > 1) content.uniqueSiteId = urlParts[urlParts.size - 2]
        else content.uniqueSiteId = urlParts[0]
        try {
            val info = Hentai2ReadParser.getDataFromScripts(scripts)
            if (info != null) {
                val title = StringHelper.removeNonPrintableChars(info.title)
                content.setTitle(title)
                val chapterImgs =
                    info.images.map { s: String -> Hentai2ReadParser.IMAGE_PATH + s }
                if (updateImages && chapterImgs.isNotEmpty()) {
                    val coverUrl = chapterImgs[0]
                    content.setImageFiles(
                        ParseHelper.urlsToImageFiles(
                            chapterImgs,
                            coverUrl,
                            StatusContent.SAVED
                        )
                    )
                    content.setQtyPages(chapterImgs.size)
                }
            }
        } catch (ioe: IOException) {
            Timber.w(ioe)
        }
        return content
    }

    private fun updateGallery(content: Content, updateImages: Boolean): Content {
        cover?.let {
            content.setCoverImageUrl(ParseHelper.getImgSrc(it))
        }
        title?.let {
            if (it.isNotEmpty()) {
                val titleStr = it[it.size - 1].text() // Last span is the title
                content.setTitle(
                    if (titleStr.isNotEmpty()) StringHelper.removeNonPrintableChars(
                        titleStr
                    ) else ""
                )
            }
        } ?: { content.setTitle(NO_TITLE) }
        content.uniqueSiteId = uniqueId
        val attributes = AttributeMap()
        properties?.let { props ->
            var currentProperty = ""
            for (e in props) {
                for (child in e.children()) {
                    if (child.nodeName() == "b") currentProperty =
                        child.text().lowercase(Locale.getDefault())
                            .trim { it <= ' ' } else if (child.nodeName() == "a") {
                        when (currentProperty) {
                            "parody" -> ParseHelper.parseAttribute(
                                attributes,
                                AttributeType.SERIE,
                                child,
                                false,
                                Site.HENTAI2READ
                            )

                            "artist" -> ParseHelper.parseAttribute(
                                attributes,
                                AttributeType.ARTIST,
                                child,
                                false,
                                Site.HENTAI2READ
                            )

                            "language" -> ParseHelper.parseAttribute(
                                attributes,
                                AttributeType.LANGUAGE,
                                child,
                                false,
                                Site.HENTAI2READ
                            )

                            "character" -> ParseHelper.parseAttribute(
                                attributes,
                                AttributeType.CHARACTER,
                                child,
                                false,
                                Site.HENTAI2READ
                            )

                            "content", "category" -> ParseHelper.parseAttribute(
                                attributes,
                                AttributeType.TAG,
                                child,
                                false,
                                Site.HENTAI2READ
                            )

                            else -> {}
                        }
                    }
                }
            }
        }
        content.putAttributes(attributes)
        if (updateImages) {
            content.setImageFiles(emptyList())
            content.setQtyPages(0)
        }
        return content
    }
}