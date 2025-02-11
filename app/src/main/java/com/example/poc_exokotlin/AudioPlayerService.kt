package com.example.poc_exokotlin

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util


private const val TAG = "AudioPlayerService"

class AudioPlayerService : Service() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "playback_channel"
        const val PLAYBACK_NOTIFICATION_ID = 1
    }

    private var player: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    val context: Context = this
    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        //Classe utilizada no bind do player do serviço com o player da view
        fun getBinder(): AudioPlayerService {
            return this@AudioPlayerService
        }
    }

    private fun startPlayer() {
        // Inicialização do player e dos demais metodos
        Log.e("Ninja", "Teste logs")
        val dataSourceFactory = DefaultHttpDataSourceFactory( Util.getUserAgent(context, getString(R.string.app_name)))

        player = SimpleExoPlayer.Builder(context).setTrackSelector(DefaultTrackSelector(context))
            .build() //Constroi um player
        //Daqui pra baixo configura o gerenciador do player em notificação
        buildSingleMediaSource(dataSourceFactory) //se esse estiver sendo chamado comentar o buildConcatMediaSources
        //  buildConcatMediaSources(dataSourceFactory) //se esse estiver sendo chamado comentar o buildSingleMediaSource
        configNotificationManager()
        configAudioFocus()
        addListeners()
        playerNotificationManager?.setPlayer(player)
        player?.playWhenReady = true
    }

    private fun addListeners() {

        player?.apply {
            addListener(listeners)
        }
    }

    private fun buildSingleMediaSource(dataSourceFactory: DefaultHttpDataSourceFactory) {
        // LINK DA RADIO
        val uri =
            Uri.parse("https://s2.radio.co/sdf9aeb4e9/listen")
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        player?.prepare(mediaSource) //Alimenta o player com a playlist
    }


    private fun configNotificationManager() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name,
            R.string.playback_channel_description,
            PLAYBACK_NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return null
                }

                override fun getCurrentContentText(player: Player): String? {
                    return "Lorem Ipsum is simply dummy text of the printing and typesetting industry."
                }

                override fun getCurrentContentTitle(player: Player): String {
                    return "Teste Titulo"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return context.resources.getDrawable(R.drawable.penguim).toBitmap()
                }
            },
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    super.onNotificationCancelled(notificationId, dismissedByUser)
                    stopSelf()

                }
            }
        )
    }

    private fun configAudioFocus() { //Configura o Foco do audio para modo de discurso pausando e resumindo automaticamente a reprodução quando recebida uma notificação
        val audioAtribute = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
        player?.audioAttributes = audioAtribute
    }

    fun getPlayerInstance(): SimpleExoPlayer? {
        if (player == null) {
            startPlayer()
        }
        return player
    }

    override fun onBind(intent: Intent?): IBinder? =  mBinder


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =  START_STICKY

    override fun onDestroy() {
        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null, null)
        playerNotificationManager?.setPlayer(null)
        player?.release()
        player = null
        super.onDestroy()
    }

    val listeners = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            super.onPlayerError(error)
            Log.e(TAG, "$error")

            when (error.type) {
                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    Log.e(TAG, "TYPE__OUT_OF_MEMORY: ${error.outOfMemoryError.message}")
                }
                ExoPlaybackException.TYPE_REMOTE -> {
                    Log.e(TAG, "TYPE_REMOTE: ${error.message}")
                }
                ExoPlaybackException.TYPE_RENDERER -> {
                    Log.e(TAG, "TYPE_RENDERER: ${error.rendererException.message}")
                }
                ExoPlaybackException.TYPE_SOURCE -> {
                    Log.e(TAG, "TYPE_SOURCE: ${error.sourceException.message}")
                }
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    Log.e(TAG, "TYPE_UNEXPECTED${error.unexpectedException}")
                }
            }
        }

    }
}