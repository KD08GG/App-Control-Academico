package com.udlap.controlacademico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    private lateinit var tvLogOut: TextView
    private lateinit var btnUsuarios: Button
    private lateinit var btnAcademica: Button
    private lateinit var tvNombreSeccion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        tvLogOut = findViewById(R.id.tvLogOut)
        btnUsuarios = findViewById(R.id.btnGestionUsuarios)
        btnAcademica = findViewById(R.id.btnGestionAcademica)
        tvNombreSeccion = findViewById(R.id.tvNombreSeccionAdmin)

        if (savedInstanceState == null) {
            tvNombreSeccion.text = "Gestión de Usuarios"
            reemplazarFragment(AdminUsuariosFragment())
        }

        btnUsuarios.setOnClickListener {
            tvNombreSeccion.text = "Gestión de Usuarios"
            reemplazarFragment(AdminUsuariosFragment())
        }

        btnAcademica.setOnClickListener {
            tvNombreSeccion.text = "Gestión Académica"
            reemplazarFragment(AdminAcademicaFragment())
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

    private fun reemplazarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerAdmin, fragment)
            .commit()
    }
}