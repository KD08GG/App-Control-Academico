package com.udlap.controlacademico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class AlumnoActivity : AppCompatActivity() {

    private lateinit var tvLogOut: TextView
    private lateinit var btnQR: Button
    private lateinit var btnCalif: Button
    private lateinit var btnHorario: Button
    private lateinit var tvNombreSeccion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alumno)

        tvLogOut = findViewById(R.id.tvLogOutAlumno)
        btnQR = findViewById(R.id.btnQR)
        btnCalif = findViewById(R.id.btnCalif)
        btnHorario = findViewById(R.id.btnHorarioAlumno)
        tvNombreSeccion = findViewById(R.id.tvNombreSeccionAlumno)

        if (savedInstanceState == null) {
            tvNombreSeccion.text = "Asistencia (QR)"
            cargarFragmento(QrFragment())
        }

        btnQR.setOnClickListener {
            tvNombreSeccion.text = "Asistencia (QR)"
            cargarFragmento(QrFragment())
        }

        btnCalif.setOnClickListener {
            tvNombreSeccion.text = "Calificaciones"
            cargarFragmento(CalifFragment())
        }

        btnHorario.setOnClickListener {
            tvNombreSeccion.text = "Horario"
            cargarFragmento(HorarioAlumnoFragment())
        }

        tvLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val sessionManager = SessionManager(this)
            sessionManager.cerrarSesion()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmento, fragment)
            .commit()
    }
}