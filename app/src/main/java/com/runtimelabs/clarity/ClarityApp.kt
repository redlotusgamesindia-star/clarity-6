package com.runtimelabs.clarity

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt generates the [dagger.hilt.android.internal]
 * component tree from here; every @AndroidEntryPoint hangs off this class.
 *
 * Intentionally thin: no eager initialization. Heavy work (content seeding,
 * WorkManager scheduling) arrives in Phase A behind androidx.startup or
 * lazy Hilt provision so cold start stays fast.
 */
@HiltAndroidApp
class ClarityApp : Application()
