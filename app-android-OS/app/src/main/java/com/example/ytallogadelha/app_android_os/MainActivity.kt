package com.example.ytallogadelha.app_android_os

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.ConnectivityManager
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    lateinit var botaoAdicionar: Button
    lateinit var listView: ListView
    lateinit var queue: RequestQueue
    lateinit var url: String
    lateinit var jsonArrayRequest: JsonArrayRequest
    lateinit var jsonList: String
    lateinit var gson: Gson
    lateinit var servicoList: List<OrdemServico>
    lateinit var adapter: ArrayAdapter<OrdemServico>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //referêciando os componentes a partir do identificador
        listView = findViewById(R.id.list_view)

        //verificaConexao(this)
    }

    //Função chamada na criação de e restart da activity
    override fun onResume() {
        super.onResume()

        // Instanciando a RequestQueue.
        queue = Volley.newRequestQueue(this)

        //Criando a URL
        url = "http://192.168.0.5:3000/list"

        //Criando a requisição. Verbo GET
        jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->

                    //Recebendo string por meio do response
                    jsonList = response.toString()

                    println(jsonList)

                    //Instanciando o gson para converter json em objeto
                    gson = GsonBuilder().setPrettyPrinting().create()

                    //Criando a lista de produtos
                    servicoList = gson.fromJson(jsonList, object : TypeToken<List<OrdemServico>>() {}.type)

                    //Criando o adapter necessário ao listView
                    adapter = ArrayAdapter<OrdemServico>(
                            this, // Context
                            android.R.layout.simple_list_item_1, // Layout
                            servicoList // List
                    )

                    //Adiconando o adapter ao listView
                    listView.adapter = adapter

                    // Configurando o click listener da ListView
                    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

                        //Criando a Intent para fazer a navegação e passar os dados entre as atividades
                        var intent: Intent = Intent(this, DetalhesActivity::class.java)
                        intent.putExtra("servico", servicoList[position])

                        startActivity(intent)
                    }
                },

                Response.ErrorListener { error ->

                    println("Erro: ${error}")
                }
        )
        //Adicionando a requisição na RequestQueue.
        queue.add(jsonArrayRequest)
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
