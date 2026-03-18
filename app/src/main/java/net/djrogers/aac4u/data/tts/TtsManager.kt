package net.djrogers.aac4u.data.tts

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lifecycle-aware wrapper around AACTextToSpeech.
 * Ensures TTS resources are properly cleaned up when the app is destroyed.
 *
 * Injected as a Singleton via Hilt — one TTS instance for the entire app.
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {

    val tts: AACTextToSpeech by lazy { AACTextToSpeech(context) }

    override fun onDestroy(owner: LifecycleOwner) {
        tts.shutdown()
    }
}
