package com.example.ytallogadelha.app_android_os

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

//Classe responsável por fazer a requisição POST em uma thread secundária para fazer o upload de uma imagem no servidor
class PostRequest : AsyncTask<ArrayList<Bitmap>, Void, String>() {

    var arrayList: ArrayList<Bitmap>? = ArrayList()

    //Criação da variável estática que receberá o nome da foto
    companion object {

        lateinit var nomeImagem: String

        fun setNome(nome: String){
            nomeImagem = nome
        }
    }

    //Função que é executada antes da Thread ser iniciada
    override fun onPreExecute() {
        super.onPreExecute()
        Log.i("AsyncTask", " Por favor aguarde: " + Thread.currentThread().getName())
        println("Por favor aguarde...")
    }

    //Função executada em uma thread separada para evitar o travamento da tela do usuário
    override fun doInBackground(vararg p0: ArrayList<Bitmap>?): String? {

        var connection: HttpURLConnection? = null

        try {

            //Criação da conexão
            val url:URL = URL("http://192.168.0.5:3000/save")
            connection = url.openConnection() as HttpURLConnection?
            connection?.requestMethod = "POST"

            //Configuração das propriedades
            connection?.setRequestProperty("Content-Type","image/jpeg")
            connection?.setRequestProperty("X-Filename", nomeImagem)
            connection?.doOutput = true

            //Criação do stream para depois fazer o envio da imagem
            val outputStream: OutputStream? = connection!!.outputStream

            val array = p0.first()

            val bitmap = array!!.get(0)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            //Conversões de tipo
            val imgString: String = outputStream!!.toString()
            val imgByteArray: ByteArray = imgString.toByteArray()

            //Enviando a imagem ao servidor
            outputStream.write(imgByteArray)
            outputStream.flush()
            outputStream.close()

            //Obtendo a resposta
            val inputStream: InputStream = connection.inputStream
            val rd: BufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String
            val response: StringBuffer = StringBuffer()
            while (rd.readLine() != null) {
                line = rd.readText()
                response.append(line)
                response.append('\r')
            }

            Log.i("AsyncTask", "Imagem sendo salva: " + Thread.currentThread().getName())
            println("Imagem sendo salva!!!")
            rd.close()
            return response.toString()

        }catch(error: MalformedURLException) {
            //Handles an incorrectly entered URL
            println(error.stackTrace)
            return null

        }
        catch(error: SocketTimeoutException) {
            //Handles URL access timeout.
            println(error.stackTrace)
            return null

        }
        catch (error: IOException) {
            //Lida com os erros de entra e saída
            println(error.stackTrace)
            return null

        } finally {

            if (connection != null) {
                connection.disconnect()
            }
        }
    }

    //Função chamado ao término do método doInBackground
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        Log.i("AsyncTask", "Imagem foi salva: " + Thread.currentThread().getName());
        println("Imagem foi salva!!!")
    }
}