package ru.yourok.tinstaller

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import ru.yourok.tinstaller.content.Content
import ru.yourok.tinstaller.content.ContentHandler.clearContent
import ru.yourok.tinstaller.content.ContentHandler.loadContent
import ru.yourok.tinstaller.utils.ApkInstaller
import ru.yourok.tinstaller.utils.Download
import ru.yourok.tinstaller.utils.Prefs
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    companion object {
        var categories = listOf<String>()
    }

    private var pagerAdapter: CategoryPagerAdapter? = null
    private var searchResults: List<ru.yourok.tinstaller.content.App> = emptyList()
    private var allLabel: String = ""
    private var linksLabel: String = ""
    private var resultLabel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Prefs.isLicenseAgree()) {
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        findViewById<Button>(R.id.btnGoYouTube).setOnClickListener {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/user/Zorronder"))
            webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(webIntent)
        }

        findViewById<Button>(R.id.btnSelectAll).setOnClickListener {
            loadContent {
                it.apps?.forEach {
                    it.select = true
                    it.progress = -1
                }
                updateListView(false)
            }
        }

        findViewById<Button>(R.id.btnDeselect).setOnClickListener {
            loadContent {
                it.apps?.forEach {
                    it.select = false
                    it.progress = -1
                }
                updateListView(false)
            }
        }

        findViewById<Button>(R.id.btnInstall)?.setOnClickListener {
            install()
        }

        findViewById<Button>(R.id.btnEnterUrl)?.setOnClickListener {
            UrlDialog().show(this, Prefs.getURL(), {
                Prefs.setURL(it)
                clearContent()
                loadContent { fillContent(it) }
            }, { })
        }

        findViewById<Button>(R.id.btnSearch)?.setOnClickListener {
            val query = findViewById<EditText>(R.id.etSearch)?.text?.toString()?.trim() ?: ""
            if (query.isEmpty()) {
                // Clear results and remove tab if exists
                if (searchResults.isNotEmpty()) {
                    searchResults = emptyList()
                    updateCategories()
                }
                return@setOnClickListener
            }
            loadContent { content ->
                val results = content.apps
                    ?.filter { it.title?.contains(query, ignoreCase = true) == true }
                    ?.sortedBy { it.title?.lowercase() }
                    ?: emptyList()
                searchResults = results
                updateCategories()
            }
        }
    }

    private fun fillContent(content: Content) {
        val categoryFilter = mutableSetOf<String>()
        content.apps?.forEach {
            categoryFilter.add(it.category)
        }
        val catTmp = categoryFilter.toList().sorted().toMutableList()
        allLabel = getString(R.string.category_all)
        linksLabel = getString(R.string.category_useful_links)
        resultLabel = getString(R.string.category_result)
        catTmp.add(0, allLabel)
        if (content.links?.isNotEmpty() == true)
            catTmp.add(1, linksLabel)

        // Insert Result tab if we already have results (e.g., after rotation)
        if (searchResults.isNotEmpty()) {
            val insertIndex = if (catTmp.contains(linksLabel)) 2 else 1
            catTmp.add(insertIndex, resultLabel)
        }

        categories = catTmp

        val pager = findViewById<ViewPager>(R.id.vpCategories)
        pagerAdapter = CategoryPagerAdapter(supportFragmentManager, content, allLabel, linksLabel, resultLabel) { searchResults }
        pager.adapter = pagerAdapter
    }

    private fun updateCategories() {
        loadContent { content ->
            val categoryFilter = mutableSetOf<String>()
            content.apps?.forEach { categoryFilter.add(it.category) }
            val catTmp = categoryFilter.toList().sorted().toMutableList()
            if (allLabel.isEmpty()) allLabel = getString(R.string.category_all)
            if (linksLabel.isEmpty()) linksLabel = getString(R.string.category_useful_links)
            if (resultLabel.isEmpty()) resultLabel = getString(R.string.category_result)
            catTmp.add(0, allLabel)
            if (content.links?.isNotEmpty() == true)
                catTmp.add(1, linksLabel)
            if (searchResults.isNotEmpty()) {
                val insertIndex = if (catTmp.contains(linksLabel)) 2 else 1
                catTmp.add(insertIndex, resultLabel)
            }
            categories = catTmp
            updateListView(false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Prefs.getURL().isBlank())
            UrlDialog().show(this, Prefs.getURL(), {
                Prefs.setURL(it)
                clearContent()
                loadContent { fillContent(it) }
            }, {
                finish()
            })
        else
            loadContent { fillContent(it) }
    }

    private fun disableUI() {
        Handler(Looper.getMainLooper()).post {
            findViewById<Button>(R.id.btnInstall)?.isEnabled = false
            findViewById<Button>(R.id.btnSelectAll)?.isEnabled = false
            findViewById<Button>(R.id.btnDeselect)?.isEnabled = false
            findViewById<RecyclerView>(R.id.rvAppList)?.isEnabled = false
        }
    }

    private fun enableUI() {
        Handler(Looper.getMainLooper()).post {
            findViewById<Button>(R.id.btnInstall)?.isEnabled = true
            findViewById<Button>(R.id.btnSelectAll)?.isEnabled = true
            findViewById<Button>(R.id.btnDeselect)?.isEnabled = true
            findViewById<RecyclerView>(R.id.rvAppList)?.isEnabled = true
        }
    }

    private var installing = false
    private fun install() {
        disableUI()
        synchronized(installing) {
            if (installing)
                return
            installing = true
        }

        loadContent { content ->
            thread {
                val tpExecutors = Executors.newFixedThreadPool(5)
                val dpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                val dlDir = File(dpath, "ti")
                if (!dlDir.exists())
                    dlDir.mkdir()
                val wa = CountDownLatch(content.apps?.size ?: 0)
                content.apps?.forEach { app ->
                    if (app.select) {
                        tpExecutors.submit {
                            try {
                                val fname = Download.getFileNameFromURL(app.url)
                                val dlPath = File(dlDir, fname)
                                if (app.progress != 101)
                                    Download.download(app, dlPath) { prc ->
                                        app.progress = prc
                                        updateListView()
                                    }
                                app.progress = 101
                                if (ApkInstaller.installApplication(this, dlPath.path))
                                    app.select = false
                                updateListView(false)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                app.progress = -2
                            }
                            wa.countDown()
                            app.select = false
                        }
                    } else
                        wa.countDown()
                }
                tpExecutors.shutdown()
                wa.await()

                updateListView(false)
                installing = false
                enableUI()
            }
        }
    }

    private fun checkPermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                            )
                        )
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {}

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                            )
                        )
                    }
                }
            }).check()


    }

    var lastUpdate = 0L
    private fun updateListView(timeout: Boolean = true) {
        synchronized(lastUpdate) {
            if (timeout && System.currentTimeMillis() - lastUpdate < 250)
                return

            lastUpdate = System.currentTimeMillis()
        }
        Handler(Looper.getMainLooper()).post {
            pagerAdapter?.notifyDataSetChanged()
        }
    }

    private class CategoryPagerAdapter(
        fm: FragmentManager,
        val content: Content,
        private val allLabel: String,
        private val linksLabel: String,
        private val resultLabel: String,
        private val getSearchResults: () -> List<ru.yourok.tinstaller.content.App>
    ) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            if (categories[position] == allLabel)
                return PagerAppFragment.newInstance(
                    (content.apps ?: return Fragment()).sortedBy { it.title?.lowercase() }
                )
            else if (categories[position] == linksLabel)
                return PagerLinksFragment.newInstance(content.links ?: return Fragment())
            else if (categories[position] == resultLabel)
                return PagerAppFragment.newInstance(getSearchResults())
            else {
                val list = content.apps
                    ?.filter { it.category == categories[position] }
                    ?.sortedBy { it.title?.lowercase() }
                    ?: emptyList()
                return PagerAppFragment.newInstance(list)
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return categories[position]
        }

        override fun getCount(): Int {
            return categories.size
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }
}
