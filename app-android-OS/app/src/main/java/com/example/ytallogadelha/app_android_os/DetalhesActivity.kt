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
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetalhesActivity : AppCompatActivity() {

    val NOME_ARQUIVO = "arquivoLocal.txt"
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
    lateinit var servico: OrdemServico
    lateinit var servicoAtualizado: OrdemServico
    lateinit var myToolbar: Toolbar
    lateinit var imageFileName: String
    lateinit var imagemNome: String
    var imagemRotacionada: Bitmap? = null
    var imagemSelecionada: Bitmap? = null
    var array: ArrayList<Bitmap>? = ArrayList()

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

        //Criação da Intent para capturar os dados enviados pela navegação
        val intent = getIntent()
        servico = intent.getParcelableExtra("servico")

        //Populando os componentes com os dados passados pela Intent
        textId.text = servico.idOS.toString()
        textFuncionario.setText(servico.funcionarioOS)
        textDescricao.setText(servico.descricaoOS)
        editFeedback.setText(servico.feedbackOS)

        //Configuração do botão voltar. Finaliza a activity
        botaoVoltar.setOnClickListener(View.OnClickListener {

            finish()
        })

        //Configuração do botão capturar(foto com a câmera)
        botaoCapturar.setOnClickListener(View.OnClickListener {

            capturarFoto()
        })

        //Configuração do botão selecionar(foto da galeria)
        botaoSelecionar.setOnClickListener(View.OnClickListener {

            recuperarFoto()
        })

        //Configuração do botão salvar
        botaoSalvar.setOnClickListener(View.OnClickListener {

            //Condição para verificar se alguma imagem foi setada
            if(imagemSelecionada != null || imagemRotacionada != null){

                servicoAtualizado = servico

                //Instanciando o gson
                val gson = GsonBuilder().setPrettyPrinting().create()

                //Populando o feedback com as informações passadas pelo usuário
                servicoAtualizado.feedbackOS = editFeedback.text.toString()

                //Criação da string no formato de json a partir do objeto serviçoAtualizado
                val stringPoduto: String = gson.toJson(servicoAtualizado)

                //Criação do json a partir de uma string
                val jsonObject = JSONObject(stringPoduto)

                // Instanciando a RequestQueue.
                val queue = Volley.newRequestQueue(this)

                //Criação da URL
                val url = "http://192.168.0.5:3000/save"

                //Criação da requisição. Verbo PUT
                val httpProtocolo = Request.Method.PUT
                val request = JsonObjectRequest(httpProtocolo, url, jsonObject,
                        Response.Listener { response ->

                            //Mensagem de sucesso
                            Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        },

                        Response.ErrorListener { error ->

                            //Mensagem de erro
                            Toast.makeText(this, "BD bugado", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                )
                //Adição da requisição na RequestQueue.
                queue.add(request)

                //Gravação local dos dados que foram enviados ao servidor
                gravarNoArquivo(stringPoduto)

                //Salvando a foto no servidor
                imagemNome = "idOS:${servicoAtualizado.idOS}"

                //Verificando qual imagem vai ser salva. Pode ser imagem da câmera, galeria ou ambas
                if (imagemRotacionada == null) {

                    //Adição da imagem da galeria no array
                    array!!.add(imagemSelecionada!!)

                } else if (imagemSelecionada == null) {

                    //Adição da imagem da câmera no array
                    array!!.add(imagemRotacionada!!)

                } else {

                    //Adição da imagem da galeria e da câmera no array
                    array!!.add(imagemSelecionada!!)
                    array!!.add(imagemRotacionada!!)
                }

                //Passando o array e o nome da imagem para o método que vai chamar a AsyncTask
                chamarAsyncTask(array!!, imagemNome)
            }

            //Caso nenhuma imagem seja setada, o usuário receberá um feedback informando
            if(imagemSelecionada == null && imagemRotacionada == null){

                Toast.makeText(this, "Selecione ou capture uma imagem!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Função chamado quando o aplicativo manda a foto de volta
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //Retorno da foto capturada pela câmera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            //Recuperação da foto por meio da intent
            val extras = data?.extras

            val imageBitmap = extras!!.get("data") as Bitmap

            //Rotacionando a imagem
            //Objeto que contém a imagem
            imagemRotacionada = rotacionarBitmap(imageBitmap, 90)

            //Salvando a imagem localmente
            salvarBitmap(imagemRotacionada!!)

            Toast.makeText(this, "Imagem capturada e salva!!!", Toast.LENGTH_SHORT).show()
        }

        //Retorno da foto selecionada a partir da galeria
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK){

            //Recuperação da foto selecionada por meio da intent
            //Objeto que contém a imagem
            val imagemSelecionadaURI = data!!.data

            //Pegando o bitmap a partir de uma URI
            imagemSelecionada = MediaStore.Images.Media.getBitmap(this.contentResolver, imagemSelecionadaURI)

            Toast.makeText(this, "Imagem selecionada!!!", Toast.LENGTH_SHORT).show()
        }
    }

    //Função que delega a atividade de tirar foto para o aplicativo da câmera
    private fun capturarFoto(){

        //Intent que delega a atividade para a câmera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (cameraIntent.resolveActivity(packageManager) != null) {

            startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO)
        }
    }

    //Função que recupera uma foto da galeria
    private fun recuperarFoto(){

        //intent que acessa a galeria
        val galeriaIntent  =  Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI)

        startActivityForResult(Intent.createChooser(galeriaIntent, "Selecione uma imagem!"), REQUEST_PICK_IMAGE)
    }

    //Função que salva a imagem como PNG
    private fun salvarBitmap(bitmap: Bitmap){

        //Criação do filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "JPEG" + timeStamp + ".jpeg"

        //Criação do diretório
        val diretorio: File = Environment.getExternalStorageDirectory()
        val destino = File(diretorio, imageFileName)

        try {
            //Criação do outputstream para salvar a imagem
            val saida = FileOutputStream(destino)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, saida)
            saida.flush()
            saida.close()

        }catch ( ex: IOException){
            println("Erro ao salvar a imagem -> ${ex}")
        }
    }

    //Função que rotaciona o bitmap
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

        try{
            //Criação do outputstream com o arquivo .txt
            val outputStreamWriter = OutputStreamWriter(openFileOutput(NOME_ARQUIVO, Context.MODE_APPEND))
            outputStreamWriter.write(texto)
            outputStreamWriter.close()
            println("Gravado com sucesso!!!")

            //Ler do arquivo .txt para confirmar salvamento
            lerDoArquivo()

        }catch (e: IOException) {
            Log.v("MainActivityGravar", e.toString())
        }
    }

    //Função que lê de um arquivo .txt. Usada apenas para confirmar o salvamento dos dados localmente
    private fun lerDoArquivo() {

        var resultado = ""

        try {
            //Abertura do arquivo
            val arquivo = openFileInput(NOME_ARQUIVO)

            if (arquivo != null) {

                //Criação do inputstream para ler o arquivo
                val inputStreamReader = InputStreamReader(arquivo)

                //Gerar buffer do arquivo lido
                val bufferedReader = BufferedReader(inputStreamReader)

                //Recuperar o que está inscrito no arquivo .txt
                var linhaArquivo = ""

                while (bufferedReader.readLine() != null) {

                    linhaArquivo = bufferedReader.readText()
                    resultado += linhaArquivo

                }

                arquivo.close()
            }

        } catch (e: IOException) {
            Log.v("DetalhesActivityLer", e.toString())
        }

        println(resultado)
    }

    //Função que chama a classe PostRequest, responsável por fazer a requisição POST para salvar a imagem no servidor
    private fun chamarAsyncTask(bitmap: ArrayList<Bitmap>, nomeImagem: String) {

        val post = PostRequest()
        Log.i("AsyncTask", "AsyncTask sendo chamado Thread: " + Thread.currentThread().name)

        PostRequest.setNome(nomeImagem)
        post.execute(bitmap)
    }
}