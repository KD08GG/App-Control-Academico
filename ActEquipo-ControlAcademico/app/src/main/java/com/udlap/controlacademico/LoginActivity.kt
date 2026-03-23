package com.udlap.controlacademico

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** LoginActivity — Pantalla de inicio de sesión.

 * 1. Si ya hay sesión guardada en SharedPreferences → redirige directo al panel.
 * 2. Si no hay sesión → muestra formulario de login.
 * 3. Al hacer login: autentica con Firebase Auth, consulta Firestore para ver usuario.
 * 4. Al obtener el rol del usuario → redirige según el rol.
 */
class LoginActivity : AppCompatActivity() {

    // FirebaseAuth: maneja todo lo relacionado con autenticación
    private lateinit var auth: FirebaseAuth

    // FirebaseFirestore: muestra base de datos en la nube.
    private lateinit var db: FirebaseFirestore

    // SessionManager: muestro gestor de sesión local (SharedPreferences).
    private lateinit var sessionManager: SessionManager

    // Referencias a los componentes visuales del layout.
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa Firebase y SessionManager.
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        // Vincula variables con elementos XML usando ID.
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // ── VERIFICACIÓN DE SESIÓN PREVIA ─────────────────────────────────────
        if (sessionManager.haySesion() && auth.currentUser != null) {
            // Hay sesión activa — redirigimos directo al panel correspondiente.
            redirigirSegunRol(sessionManager.obtenerRol() ?: "")
            return
        }

        // ── BOTÓN DE LOGIN ────────────────────────────────────────────────────
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validación básica antes de llamar a Firebase.
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener  // Sale del lambda sin hacer nada más.
            }

            iniciarSesion(email, password)
        }
    }

    /**
     * Autentica al usuario con Firebase Authentication.
     * Si tiene éxito, consulta Firestore para obtener su rol.
     */
    private fun iniciarSesion(email: String, password: String) {
        // Spinner de carga: mientras se espera respuesta de Firebase.
        mostrarCargando(true)

        // signInWithEmailAndPassword: intenta autenticar al usuario.
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso — obtenemos el UID del usuario autenticado.
                    val uid = auth.currentUser!!.uid
                    obtenerRolDeFirestore(uid)
                } else {
                    mostrarCargando(false)
                    val mensaje = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "Error: $mensaje", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Consulta Firestore para obtener el rol y nombre del usuario.
     * Una vez obtenidos, guarda la sesión localmente y redirige.
     */
    private fun obtenerRolDeFirestore(uid: String) {
        // Estructura en Firestore: colección "usuarios" → documento con ID = uid.
        // Cada documento de usuario tiene campos: nombre, email, rol.
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { documento -> mostrarCargando(false)
                if (documento.exists()) {
                    // getString("rol") lee el campo "rol" del documento.
                    val rol = documento.getString("rol") ?: "alumno"
                    val nombre = documento.getString("nombre") ?: "Usuario"

                    // Guardamos en SharedPreferences para recordar la sesión.
                    sessionManager.guardarSesion(uid, rol, nombre)

                    // Redirigimos al panel correspondiente.
                    redirigirSegunRol(rol)
                } else {
                    // El usuario existe en Auth pero no en Firestore.
                    Toast.makeText(
                        this,
                        "Usuario no encontrado en la base de datos",
                        Toast.LENGTH_LONG
                    ).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener { e ->
                mostrarCargando(false)
                Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Redirige al usuario a la Activity correcta según su rol.
     * Llama a finish() para que el usuario no pueda volver al login presionando "atrás".
     */
    private fun redirigirSegunRol(rol: String) {
        val destino = when (rol) {
            "admin"    -> AdminActivity::class.java
            "profesor" -> ProfesorActivity::class.java
            else       -> AlumnoActivity::class.java  // "alumno" u otro
        }

        // Intent: abre otra Activity.
        startActivity(Intent(this, destino))
        finish()
    }

    //Muestra u oculta el estado de carga.
    private fun mostrarCargando(cargando: Boolean) {
        progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !cargando
    }
}