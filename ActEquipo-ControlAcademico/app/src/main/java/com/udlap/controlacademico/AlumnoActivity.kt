package com.udlap.controlacademico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * AlumnoActivity — Panel principal del alumno.
 * Usa un BottomNavigationView para navegar entre 3 fragmentos:
 *  1. QrFragment      → Genera QR para el pase de lista
 *  2. CalifFragment   → Ve sus calificaciones por materia
 *  3. HorarioAlumnoFragment → Consulta su horario
 */
class AlumnoActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alumno)

        sessionManager = SessionManager(this)
        bottomNav     = findViewById(R.id.bottomNavAlumno)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionAlumno)

        // Fragmento inicial: QR
        if (savedInstanceState == null) {
            cargarFragmento(QrFragment())
        }

        // Navegación entre pestañas
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_qr       -> { cargarFragmento(QrFragment());             true }
                R.id.nav_calif    -> { cargarFragmento(CalifFragment());           true }
                R.id.nav_horario  -> { cargarFragmento(HorarioAlumnoFragment());   true }
                else              -> false
            }
        }

        btnCerrarSesion.setOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmento, fragment)
            .commit()
    }
}