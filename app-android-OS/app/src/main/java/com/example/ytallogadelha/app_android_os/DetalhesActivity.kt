package com.example.ytallogadelha.app_android_os

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class DetalhesActivity : AppCompatActivity() {

    val NOME_ARQUIVO = "salvarLocal"
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_PICK_IMAGE = 1234
    lateinit var botaoCapturar: Button
    lateinit var botaoSelecionar: Button
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
    lateinit var myToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        //Referenciando a toolbar personalizada
        myToolbar = findViewById(R.id.my_toolbar) as Toolbar
        myToolbar.title = "Detalhes da Ordem de Serviço"
        setSupportActionBar(myToolbar)

        //Referenciando os componentes de acordo com os identificadores
        textId = findViewById(R.id.text_id)
        textID = findViewById(R.id.text_ID)
        textFuncionario = findViewById(R.id.text_funcionario)
        editFeedback = findViewById(R.id.edit_text_feedback)
        textDescricao = findViewById(R.id.text_descricao)
        botaoVoltar = findViewById(R.id.button_voltar)
        botaoSalvar = findViewById(R.id.button_salvar)
        botaoCapturar = findViewById(R.id.button_capturar)
        botaoSelecionar = findViewById(R.id.button_selecionar)

    }

    override fun onResume() {
        super.onResume()

        //Criando a Intent para capturar os dados enviados pela navegação
        var intent = getIntent()
        servico = intent.getParcelableExtra("servico")

        //Populando os componentes com os dados passados pela Intent
        textId.text = servico.idOS.toString()
        textFuncionario.setText(servico.funcionarioOS)
        textDescricao.setText(servico.descricaoOS)
        editFeedback.setText(servico.feedbackOS)

        //vericando se a gravação deu certo
        println("-> Verificando a gravação -> " + lerDoArquivo())

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
            var url = "http://192.168.25.3:3000/save"

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

            //gravando localmente
            var stringGravacao = ""
            stringGravacao = "{idOS: ${servicoAtualizado.idOS}, funcionaioOS: ${servicoAtualizado.funcionarioOS}, " +
                    "descricaoOS: ${servicoAtualizado.descricaoOS}, feedbackOS: ${servicoAtualizado.feedbackOS}}"
            gravarNoArquivo(stringGravacao)
            Toast.makeText(this, "Salvo localmente!", Toast.LENGTH_SHORT).show()


        })

        //configurando o botão capturar(foto com a câmera)
        botaoCapturar.setOnClickListener(View.OnClickListener {

            capturarFoto()
        })

        //configurando o botão selecionar(foto da galeria)
        botaoSelecionar.setOnClickListener(View.OnClickListener {

            recuperarFoto()
        })
    }

    //método chamado quando o aplicativo manda a foto de volta
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            //recuperando a foto por meio da intent
            val extras = data?.extras

            val imageBitmap = extras!!.get("data") as Bitmap

            //rotacionando a imagem
            //objeto que contém a imagem
            val imagemRotacionada = rotacionarBitmap(imageBitmap, 90)

            salvarBitmap(imagemRotacionada)
            Toast.makeText(this, "Imagem capturada e salva!!!", Toast.LENGTH_LONG).show()
        }

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK){

            //recuperando a foto selecionada por meio da intent
            //objeto que contém a imagem
            val imagemSelecionada = data!!.data

            Toast.makeText(this, "Imagem selecionada!!!", Toast.LENGTH_LONG).show()
        }
    }

    //método que delega a atividade de tirar foto para o aplicativo da câmera
    private fun capturarFoto() {

        //intent que delega a atividade para a câmera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (cameraIntent.resolveActivity(packageManager) != null) {

            startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO)
        }
    }

    private fun recuperarFoto(){

        //intent que acessa a galeria
        val galeriaIntent  =  Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI)

        startActivityForResult(Intent.createChooser(galeriaIntent, "Selecione uma imagem!"), REQUEST_PICK_IMAGE)
    }

    //método que salva o imagem como JPEG
    private fun salvarBitmap( bitmap: Bitmap){

        //criando um filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName: String = "PNG" + timeStamp + ".png"

        //criando o diretório
        val diretorio: File = Environment.getExternalStorageDirectory()
        val destino = File(diretorio, imageFileName)

        try {

            //criando o outputstream para salvar a imagem
            val saida = FileOutputStream(destino)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
            saida.flush()
            saida.close()

        }catch ( ex: IOException){
            println("Erro ao salvar a imagem -> ${ex}")
        }
    }

    //método para rotacionar o bitmap
    private fun rotacionarBitmap(original: Bitmap, degrees: Int): Bitmap {
        val largura = original.width
        val altura = original.height

        val matrix = Matrix()
        matrix.preRotate(degrees.toFloat())

        val bitmapRotacionado = Bitmap.createBitmap(original, 0, 0, largura, altura, matrix, true)

        return bitmapRotacionado
    }

    //Função que grava em um arquivo .txt
    private fun gravarNoArquivo(texto: String) {

        try {

            val outputStreamWriter = OutputStreamWriter(openFileOutput("salvarLocal.txt", Context.MODE_PRIVATE))
            outputStreamWriter.write(texto)
            outputStreamWriter.close()

        } catch (e: IOException) {
            Log.v("MainActivityGravar", e.toString())
        }
    }

    //Função que lê de um arquivo .txt
    private fun lerDoArquivo(): String {
        var resultado = ""

        try {
            //Abrir o arquivo
            val arquivo = openFileInput("salvarLocal.txt")

            if (arquivo != null) {

                //Ler o arquivo
                val inputStreamReader = InputStreamReader(arquivo)

                //Gerar buffer do arquivo lido
                val bufferedReader = BufferedReader(inputStreamReader)

                //Recuperar textos do arquivo

                var linhaArquivo = ""

                while (bufferedReader.readLine() != null) {

                    linhaArquivo = bufferedReader.readLines().toString()
                    resultado += linhaArquivo
                }

                arquivo.close()
            }

        } catch (e: IOException) {
            Log.v("MainActivity", e.toString())
        }

        return resultado

    }
}