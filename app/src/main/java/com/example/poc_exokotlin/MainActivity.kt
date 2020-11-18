package com.example.poc_exokotlin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poc_exokotlin.AudioPlayerService.AudioServiceBinder
import com.example.poc_exokotlin.databinding.ActivityMainBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var bound = false
    private var mService: AudioPlayerService? = null
    private lateinit var player: SimpleExoPlayer

    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioServiceBinder
            mService = binder.service
            player = binder.player
            bound = true
            initializePlayer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerClickEvents()

        AudioPlayerService.newIntent(this, "Teste", "https://fps5.listen2myradio.com:2199/listen.php?ip=78.129.139.48&port=9360&type=s1").also {intent ->
            Util.startForegroundService(this, intent)
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun initializePlayer() { // Inicializa um player para conectar com a view
        if (bound) {
            mService?.play(Uri.parse("https://fps5.listen2myradio.com:2199/listen.php?ip=78.129.139.48&port=9360&type=s1"))
        }
    }

    private fun registerClickEvents() {
        binding.btnPlay.setOnClickListener {
            mService?.let {
                mService?.play(Uri.parse("https://fps5.listen2myradio.com:2199/listen.php?ip=78.129.139.48&port=9360&type=s1"))
            } ?: run{
                Toast.makeText(this, "ESPERA Aê!!", Toast.LENGTH_LONG).show()
            }
        }
    }

}