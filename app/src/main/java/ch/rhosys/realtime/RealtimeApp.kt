package ch.rhosys.realtime

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ch.rhosys.realtime.data.local.location.LocationProvider
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RealtimeApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var locationProvider: LocationProvider

    var startupError: Throwable? = null
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Track location for the whole app lifetime, not just while a screen asks for it —
        // a no-op if permission isn't granted yet; retried once it is.
        locationProvider.start()
        installCrashHandler()

        try {
            initPostHog()
        } catch (e: Throwable) {
            Log.e("RealtimeApp", "PostHog init failed", e)
            startupError = e
        }
    }

    private fun initPostHog() {
        val config = PostHogAndroidConfig(
            apiKey = "phc_D195RxeDm7isiEPFR31SxBu0KED0Bdc0z9nwSlWM58",
            host = "https://live.rhosys.ch",
        ).apply {
            captureApplicationLifecycleEvents = true
            captureDeepLinks = true
            sessionReplay = true
        }
        PostHogAndroid.setup(this, config)
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                PostHog.capture(
                    event = "app_crashed",
                    properties = mapOf(
                        "exception" to throwable.javaClass.name,
                        "message" to (throwable.message ?: ""),
                        "stacktrace" to throwable.stackTraceToString(),
                    ),
                )
                PostHog.flush()
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
