package com.example.poc_exokotlin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.example.poc_exokotlin.AudioPlayerService.LocalBinder
import com.example.poc_exokotlin.databinding.ActivityMainBinding
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mIntent: Intent
    private var bound = false
    private lateinit var mService: AudioPlayerService

    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocalBinder
            mService = binder.getBinder()
            bound = true
            initializePlayer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerView.useController = true
        binding.playerView.showController()
        binding.playerView.controllerAutoShow = true
        binding.playerView.controllerHideOnTouch = false
       //Inicializa o Serviço em Foreground que permite que o app execute o Serviço na notificação de controle

        mIntent = Intent(this, AudioPlayerService::class.java) // Prepara um intent para inicializar um Serviço Foreground/Background
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE) //Conecta de fato a Activity com o Serviço do player
        initializePlayer()
    }

    private fun initializePlayer() { // Inicializa um player para conectar com a view
        if (bound) {
            binding.playerView.player = mService.getPlayerInstance()
        }
    }

    override fun onStart() {
        super.onStart()
        Util.startForegroundService(this, mIntent)
    }
}