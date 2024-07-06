package eu.kanade.tachiyomi.extension.all.kopi
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.select.Evaluator
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale

class CTCComic : HttpSource() {
    override val name = "Kopi"
    override val lang = "all"
    override val supportsLatest = false

    override val baseUrl = "https://ctccomic.com"

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/popular?page=$page", headers)

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val mangas = document.select("div.manga-entry").map {
            SManga.create().apply {
                url = it.select("a").attr("href")
                title = it.select("a").attr("title")
                thumbnail_url = it.select("img").attr("src")
            }
        }
        val hasNextPage = document.select("a.next").isNotEmpty()
        return MangasPage(mangas, hasNextPage)
    }

    override fun latestUpdatesRequest(page: Int) = throw UnsupportedOperationException("Not used.")

    override fun latestUpdatesParse(response: Response) = throw UnsupportedOperationException("Not used.")

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("page", page.toString())
            .toString()
        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response) = popularMangaParse(response)

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        val mangaDetails = SManga.create().apply {
            url = manga.url
            title = manga.title
            thumbnail_url = manga.thumbnail_url
            description = "Description is not available"
            initialized = true
        }
        return Observable.just(mangaDetails)
    }

    override fun mangaDetailsParse(response: Response) = throw UnsupportedOperationException("Not used.")

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        val chapter = SChapter.create().apply {
            url = manga.url
            name = "Chapter 1"
            date_upload = System.currentTimeMillis()
            chapter_number = 1f
        }
        return Observable.just(listOf(chapter))
    }

    override fun chapterListParse(response: Response) = throw UnsupportedOperationException("Not used.")

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()
        val images = document.select("div.page img").map { it.attr("src") }
        return images.mapIndexed { index, imageUrl -> Page(index, imageUrl = imageUrl) }
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException("Not used.")

    override fun getFilterList() = FilterList(
        Filter.Header("Search Filters (not implemented)")
    )
}
