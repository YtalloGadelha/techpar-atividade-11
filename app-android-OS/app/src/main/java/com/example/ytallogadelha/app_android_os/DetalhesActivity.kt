package com.example.ytallogadelha.app_android_os

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import org.json.JSONObject

class DetalhesActivity : AppCompatActivity() {

    lateinit var textId: TextView
    lateinit var textID: TextView
    lateinit var textFuncionario: TextView
    lateinit var editFeedback: EditText
    lateinit var textDescricao: TextView
    lateinit var botaoVoltar: Button
    lateinit var botaoSalvar: Button
    lateinit var textTitulo: TextView
    lateinit var servico: OrdemServico
    lateinit var servicoAtualizado: OrdemServico

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        //Referenciando os componentes de acordo com os identificadores
        textId = findViewById(R.id.text_id)
        textID = findViewById(R.id.text_ID)
        textFuncionario = findViewById(R.id.text_funcionario)
        editFeedback = findViewById(R.id.edit_text_feedback)
        textDescricao = findViewById(R.id.text_descricao)
        botaoVoltar = findViewById(R.id.button_voltar)
        botaoSalvar = findViewById(R.id.button_salvar)
        textTitulo = findViewById(R.id.text_titulo)

        //Criando a Intent para capturar os dados enviados pela navegação
        var intent = getIntent()
        servico = intent.getParcelableExtra("servico")

        //Populando os componentes com os dados passados pela Intent
        textId.text = servico.idOS.toString()
        textFuncionario.setText(servico.funcionarioOS)
        textDescricao.setText(servico.descricaoOS)
        editFeedback.setText(servico.feedbackOS)

        //Configurando o botão voltar
        botaoVoltar.setOnClickListener(View.OnClickListener {

            //Finalizando a atividade
            finish()
        })

        //Configurando o botão salvar
        botaoSalvar.setOnClickListener(View.OnClickListener {

            servicoAtualizado = servico

            //Instanciando o gson
            val gson = GsonBuilder().setPrettyPrinting().create()

            //Populando o feedback com as informações passadas
            servicoAtualizado.feedbackOS = editFeedback.text.toString()

            //Criando string no formato de json a partir do objeto produtoSalvo
            val stringPoduto: String = gson.toJson(servicoAtualizado)

            //Criando json a partir de uma string
            val jsonObject = JSONObject(stringPoduto)

            // Instanciando a RequestQueue.
            var queue = Volley.newRequestQueue(this)

            //Criando a URL
            var url = "http://192.168.0.5:3000/save"

            //Criando a requisição. Verbo PUT
            val httpProtocolo = Request.Method.PUT
            val request = JsonObjectRequest( httpProtocolo, url, jsonObject,
                    Response.Listener { response ->

                        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    },

                    Response.ErrorListener { error ->

                        Toast.makeText(this, "BD bugado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            )
            //Adicionando a requisição na RequestQueue.
            queue.add(request)
        })
    }
}