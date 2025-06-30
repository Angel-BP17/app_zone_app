package com.example.app_s10

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GamesListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameAdapter
    private lateinit var tvEmptyList: TextView
    private lateinit var currentUser: FirebaseUser

    companion object {
        private const val TAG = "GamesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Verificar autenticación
        currentUser = auth.currentUser ?: run {
            finish()
            return
        }

        // Configurar UI
        setupUI()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar datos del usuario
        loadUserGames()
    }

    private fun setupUI() {
        recyclerView = findViewById(R.id.rvGames)
        tvEmptyList = findViewById(R.id.tvEmptyList)

        // Configurar título con nombre de usuario
        supportActionBar?.title = "Juegos de ${currentUser.displayName ?: "Usuario"}"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        adapter = GameAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadUserGames() {
        val userId = currentUser.uid
        val gamesRef = database.getReference("games").child(userId)

        gamesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gamesList = mutableListOf<Game>()

                for (gameSnapshot in snapshot.children) {
                    val game = gameSnapshot.getValue(Game::class.java)
                    game?.let {
                        // Asignar el ID del juego (que es la clave en Firebase)
                        gamesList.add(it.copy(id = gameSnapshot.key ?: ""))
                    }
                }

                // Actualizar el adaptador
                adapter.updateGames(gamesList)

                // Mostrar mensaje si la lista está vacía
                if (gamesList.isEmpty()) {
                    tvEmptyList.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmptyList.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                Log.d(TAG, "Juegos cargados: ${gamesList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar juegos: ${error.message}")
                tvEmptyList.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}