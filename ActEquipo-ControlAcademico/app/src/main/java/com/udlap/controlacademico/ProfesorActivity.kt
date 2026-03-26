package com.udlap.controlacademico

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class ProfesorActivity : AppCompatActivity() {

    private lateinit var btnCerrarSesion: Button

    private lateinit var btnAsistencia: Button
    private lateinit var btnCalificaciones: Button
    private lateinit var btnHorario: Button
    private lateinit var tvNombreSeccion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profesor)

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnAsistencia = findViewById(R.id.btnAsistencia)
        btnCalificaciones = findViewById(R.id.btnCalificaciones)
        btnHorario = findViewById(R.id.btnHorario)
        tvNombreSeccion = findViewById(R.id.tvNombreSeccion)

        if (savedInstanceState == null) {
            tvNombreSeccion.text = "Asistencia"
            reemplazarFragment(ProfesorAsistenciaFragment())
        }

        btnAsistencia.setOnClickListener {
            tvNombreSeccion.text = "Asistencia"
            reemplazarFragment(ProfesorAsistenciaFragment())
        }

        btnCalificaciones.setOnClickListener {
            tvNombreSeccion.text = "Calificaciones"
            reemplazarFragment(ProfesorCalificacionesFragment())
        }

        btnHorario.setOnClickListener {
            tvNombreSeccion.text = "Horario"
            reemplazarFragment(ProfesorHorarioFragment())
        }

        btnCerrarSesion.setOnClickListener {

            // 1. Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Limpiar sesión local (si usas SessionManager)
            val sessionManager = SessionManager(this)
            sessionManager.cerrarSesion()

            // 3. Regresar a Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // 4. Cerrar esta Activity
            finish()
        }
    }

    private fun reemplazarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerProfesor, fragment)
            .commit()
    }
}