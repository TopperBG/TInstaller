package ru.yourok.tinstaller

import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ru.yourok.tinstaller.utils.Prefs

class SplashActivity : AppCompatActivity() {
    private val anim_duration = 2000L
    private val anim_delay = 50L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startAnimations {
            showLicense {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun showLicense(onAccept: () -> Unit) {
        if (!Prefs.isLicenseAgree()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.agreement)
                .setCancelable(false)
                .setMessage(R.string.license)
                .setPositiveButton(R.string.agree) { dialog, which ->
                    dialog?.dismiss()
                    Prefs.setLicenseAgree()
                    onAccept()
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    dialog?.dismiss()
                }
                .setOnDismissListener {
                    if (!Prefs.isLicenseAgree()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            finishAndRemoveTask()
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            finishAffinity()
                        else finish()
                    }
                }
                .show()
        } else
            onAccept()
    }

    private fun startAnimations(onEnd: () -> Unit) {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height: Float = (size.y).toFloat()

        val lstCharViews = listOf(
            findViewById<TextView>(R.id.tvLogoC1),
            findViewById<TextView>(R.id.tvLogoC2),
            findViewById<TextView>(R.id.tvLogoC3),
            findViewById<TextView>(R.id.tvLogoC4),
            findViewById<TextView>(R.id.tvLogoC5),
            findViewById<TextView>(R.id.tvLogoC6),
            findViewById<TextView>(R.id.tvLogoC7),
            findViewById<TextView>(R.id.tvLogoC8),
            findViewById<TextView>(R.id.tvLogoC9),
            findViewById<TextView>(R.id.tvLogoC10),
        )

        lstCharViews.forEachIndexed { index, textView ->
            textView.startAnimation(
                AlphaAnimation(
                    0f, 1f
                ).apply {
                    duration = anim_duration
                    startOffset = index * anim_delay
                    if (index == lstCharViews.size - 1)
                        this.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationRepeat(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                onEnd()
                            }
                        })
                }
            )
        }
    }
}
