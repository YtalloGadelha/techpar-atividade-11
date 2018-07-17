package com.example.ytallogadelha.app_android_os

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.ConnectivityManager
import android.support.v7.widget.Toolbar
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var queue: RequestQueue
    lateinit var url: String
    lateinit var jsonArrayRequest: JsonArrayRequest
    lateinit var jsonList: String
    lateinit var gson: Gson
    lateinit var servicoList: List<OrdemServico>
    lateinit var adapter: ArrayAdapter<OrdemServico>
    lateinit var myToolbar: Toolbar
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mCurrentLocation: Location
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var geocoder: Geocoder
    var addresses: List<Address> = emptyList()
    var requestingLocationUpdates: Boolean = true

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Referenciando a toolbar personalizada
        myToolbar = findViewById(R.id.my_toolbar) as Toolbar
        myToolbar.title = "Ordem de Serviço"
        setSupportActionBar(myToolbar)

        //Referêciando os componentes a partir do identificador
        listView = findViewById(R.id.list_view)

        //Criando o cliente do serviço de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Pegando a última localização do dispositivo[latitude e longitude]
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Tem a última localização conhecida. Em algumas situações raras, isso pode ser nulo.
                    if (location != null) {
                        mCurrentLocation = location
                    }
                }

        //Criando o objeto locationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    mCurrentLocation = location
                    println("Atualizando a localização!")

                    //Pegando o endereço real a partir da latitude e longitude
                    geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addresses != null && addresses.size > 0 ) {
                        var local = addresses[0].getAddressLine(0)
                        println(local)

                    }else{return }
                }
            }
        }
    }

    //Função chamada na criação de e restart da activity
    override fun onResume() {
        super.onResume()

        verificarConexao(this)

        //Iniciando a atualização de localização
        if (requestingLocationUpdates) iniciarAtualizacaoLocalizacao()

        //Instanciando a RequestQueue.
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

    //Função chamada quando a atividade pausa[aplicação vai para background ou outra atividade é iniciada]
    override fun onPause() {
        super.onPause()

        //Pausando a atualização da localização
        stopLocationUpdates()
    }

    //Função que solicita uma requisição de localização e faz a configuração dos parâmetros[intervalo padrão, rápido e prioridade]
    fun solicitarLocalizacao() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    //Função que inicia a atualização da localização
    @SuppressLint("MissingPermission")
    fun iniciarAtualizacaoLocalizacao() {

        //Solicitando a localização para iniciar o objeto locationRequest
        solicitarLocalizacao()

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */)
    }

    //Função que pausa a atualização da localização
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        println("Pausando a atualização da localização!")
    }

    //Função para verificar a disponibilidade de conexão com a rede
    private fun verificarConexao(contexto: Context): Boolean {

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
