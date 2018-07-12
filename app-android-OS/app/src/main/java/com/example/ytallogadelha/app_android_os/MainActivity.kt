package com.example.ytallogadelha.app_android_os

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.ConnectivityManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var textView: TextView = findViewById(R.id.textView)

        verificaConexao(this)
    }


    //Função para verificar a disponibilidade de conexão com a rede
    private fun verificaConexao(contexto: Context): Boolean {

        val conectado: Boolean
        var aviso: String

        //Pego a conectividade do contexto o qual o metodo foi chamado
        val cm = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //Crio o objeto netInfo que recebe as informacoes da Network
        val netInfo = cm.activeNetworkInfo

        //booleano para saber se há conexão
        conectado = (netInfo != null) && (netInfo.isConnected) && (netInfo.isAvailable)

        aviso = if (conectado == true) "Conectado" else "Desconetado"

        //informação sobre a conectividade do dispositivo
        Toast.makeText(this, aviso, Toast.LENGTH_LONG).show()

        return conectado
    }
}
