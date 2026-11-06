package ir.revayat.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import ir.revayat.app.util.Jalali
import org.json.JSONArray
import org.json.JSONObject

data class TimelineItem(val year: String, val text: String)
data class Quote(val text: String, val source: String)

data class Person(
    val id: String,
    val name: String,
    val title: String,
    val category: String,
    val day: Int,
    val birth: String,
    val martyrdom: String,
    val place: String,
    val colorDark: Color,
    val colorAccent: Color,
    val summary: String,
    val story: List<String>,
    val bio: List<String>,
    val timeline: List<TimelineItem>,
    val quotes: List<Quote>,
) {
    val isMartyr: Boolean get() = martyrdom.isNotBlank()
    val lifeSpan: String
        get() = if (martyrdom.isBlank()) birth else "$birth — $martyrdom"
}

class Content(val categories: List<String>, val people: List<Person>) {
    fun byId(id: String): Person? = people.firstOrNull { it.id == id }
    val dailyPeople: List<Person> get() = people.filter { it.day > 0 }.sortedBy { it.day }
    val allQuotes: List<Pair<Person, Quote>> get() = people.flatMap { p -> p.quotes.map { p to it } }
}

object Repository {
    @Volatile private var cached: Content? = null
    private val photoCache = HashMap<String, Bitmap?>()
    private var photoFiles: Set<String>? = null

    fun load(context: Context): Content {
        cached?.let { return it }
        val text = context.assets.open("content.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        val cats = root.getJSONArray("categories").strings()
        val arr = root.getJSONArray("people")
        val people = (0 until arr.length()).map { i -> parsePerson(arr.getJSONObject(i)) }
        return Content(cats, people).also { cached = it }
    }

    private fun parsePerson(o: JSONObject): Person {
        val colors = o.getJSONArray("colors")
        val tl = o.getJSONArray("timeline")
        val qs = o.getJSONArray("quotes")
        return Person(
            id = o.getString("id"),
            name = o.getString("name"),
            title = o.getString("title"),
            category = o.getString("category"),
            day = o.optInt("day", 0),
            birth = o.optString("birth"),
            martyrdom = o.optString("martyrdom"),
            place = o.optString("place"),
            colorDark = parseColor(colors.getString(0)),
            colorAccent = parseColor(colors.getString(1)),
            summary = o.getString("summary"),
            story = o.getJSONArray("story").strings(),
            bio = o.getJSONArray("bio").strings(),
            timeline = (0 until tl.length()).map {
                val t = tl.getJSONObject(it); TimelineItem(t.getString("year"), t.getString("text"))
            },
            quotes = (0 until qs.length()).map {
                val q = qs.getJSONObject(it); Quote(q.getString("text"), q.getString("source"))
            },
        )
    }

    private fun parseColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))
    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }

    /** Real photo supplied by the owner in assets/photos/<id>.(jpg|jpeg|png|webp), or null. */
    @Synchronized
    fun photo(context: Context, id: String): Bitmap? {
        if (photoCache.containsKey(id)) return photoCache[id]
        val files = photoFiles ?: (context.assets.list("photos")?.toSet() ?: emptySet()).also { photoFiles = it }
        val name = listOf("jpg", "jpeg", "png", "webp").map { "$id.$it" }.firstOrNull { it in files }
        val bmp = name?.let { n ->
            runCatching {
                context.assets.open("photos/$n").use { stream ->
                    val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                    BitmapFactory.decodeStream(stream, null, opts)
                }
            }.getOrNull()
        }
        photoCache[id] = bmp
        return bmp
    }
}

/** App-wide observable settings & user state, persisted in SharedPreferences. */
class AppState(context: Context) {
    private val prefs = context.getSharedPreferences("revayat", Context.MODE_PRIVATE)

    var onboarded by mutableStateOf(prefs.getBoolean("onboarded", false)); private set
    var themeMode by mutableIntStateOf(prefs.getInt("theme", 0)); private set
    var fontScale by mutableFloatStateOf(prefs.getFloat("font", 1f)); private set
    var unlockAll by mutableStateOf(prefs.getBoolean("unlockAll", false)); private set
    var bookmarks by mutableStateOf(prefs.getStringSet("bookmarks", emptySet())!!.toSet()); private set
    var read by mutableStateOf(prefs.getStringSet("read", emptySet())!!.toSet()); private set

    fun finishOnboarding() { onboarded = true; prefs.edit().putBoolean("onboarded", true).apply() }
    fun updateTheme(v: Int) { themeMode = v; prefs.edit().putInt("theme", v).apply() }
    fun updateFont(v: Float) { fontScale = v; prefs.edit().putFloat("font", v).apply() }
    fun updateUnlockAll(v: Boolean) { unlockAll = v; prefs.edit().putBoolean("unlockAll", v).apply() }

    fun toggleBookmark(id: String) {
        bookmarks = if (id in bookmarks) bookmarks - id else bookmarks + id
        prefs.edit().putStringSet("bookmarks", bookmarks).apply()
    }

    fun markRead(id: String) {
        if (id in read) return
        read = read + id
        prefs.edit().putStringSet("read", read).apply()
    }

    /** Which day of Basij week (1..7) is today; 0 = before the week, 8 = after it. */
    fun weekDay(): Int = Jalali.basijWeekDay()

    fun isUnlocked(p: Person): Boolean {
        if (p.day == 0 || unlockAll) return true
        return weekDay() >= p.day
    }
}
