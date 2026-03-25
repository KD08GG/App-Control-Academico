package com.udlap.controlacademico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

/**
 * AdminActivity — Panel de administración.
 * 1. Cambiar el rol de cualquier usuario buscándolo por correo.
 * 2. Crear nuevas materias y asignarlas a un profesor.
 */
class AdminActivity : AppCompatActivity() {

    // Referencia a Firestore para leer y escribir datos
    private lateinit var db: FirebaseFirestore

    // Gestor de sesión para manejar el cierre de sesión
    private lateinit var sessionManager: SessionManager

    // Referencias a los elementos visuales
    private lateinit var etEmailUsuario: EditText
    private lateinit var radioGroupRol: RadioGroup
    private lateinit var btnCambiarRol: Button
    private lateinit var etNombreMateria: EditText
    private lateinit var etHorario: EditText
    private lateinit var etEmailProfesor: EditText
    private lateinit var btnCrearMateria: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Inicializamos Firestore y SessionManager
        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)

        // Vinculamos las variables con los elementos del XML
        etEmailUsuario   = findViewById(R.id.etEmailUsuario)
        radioGroupRol    = findViewById(R.id.radioGroupRol)
        btnCambiarRol    = findViewById(R.id.btnCambiarRol)
        etNombreMateria  = findViewById(R.id.etNombreMateria)
        etHorario        = findViewById(R.id.etHorario)
        etEmailProfesor  = findViewById(R.id.etEmailProfesor)
        btnCrearMateria  = findViewById(R.id.btnCrearMateria)
        btnCerrarSesion  = findViewById(R.id.btnCerrarSesion)

        // Configuramos los listeners de cada botón
        btnCambiarRol.setOnClickListener   { cambiarRolUsuario() }
        btnCrearMateria.setOnClickListener { crearMateria() }

        btnCerrarSesion.setOnClickListener {
            // Borramos la sesión local y regresamos al Login
            sessionManager.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Busca al usuario por correo en Firestore y actualiza su rol.
     *
     * Firestore no tiene búsqueda por email directa como Auth,
     * así que usamos whereEqualTo() para filtrar documentos
     * donde el campo "email" coincida con el correo ingresado.
     */
    private fun cambiarRolUsuario() {
        val email = etEmailUsuario.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresa el correo del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Determinamos qué rol está seleccionado en el RadioGroup
        val rolSeleccionado = when (radioGroupRol.checkedRadioButtonId) {
            R.id.radioProfesor -> "profesor"
            R.id.radioAdmin    -> "admin"
            else               -> "alumno"  // radioAlumno es el default
        }

        // Buscamos en la colección "usuarios" el documento con ese email
        db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    Toast.makeText(this, "No se encontró usuario con ese correo", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // Tomamos el primer (y único) documento que coincide
                val documento = resultado.documents[0]

                // update() modifica solo los campos especificados,
                // sin borrar los demás datos del documento
                documento.reference.update("rol", rolSeleccionado)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Rol de $email cambiado a $rolSeleccionado",
                            Toast.LENGTH_LONG
                        ).show()
                        etEmailUsuario.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al buscar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Crea una nueva materia en Firestore.
     *
     * Primero busca al profesor por correo para obtener su UID,
     * luego crea el documento de la materia con ese UID como referencia.
     */
    private fun crearMateria() {
        val nombre  = etNombreMateria.text.toString().trim()
        val horario = etHorario.text.toString().trim()
        val emailProfesor = etEmailProfesor.text.toString().trim()

        if (nombre.isEmpty() || horario.isEmpty() || emailProfesor.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Primero obtenemos el UID del profesor por su correo
        db.collection("usuarios")
            .whereEqualTo("email", emailProfesor)
            .whereEqualTo("rol", "profesor")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    Toast.makeText(
                        this,
                        "No se encontró un profesor con ese correo",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val profesorUid = resultado.documents[0].id

                // Construimos el mapa de datos de la materia.
                // En Firestore, un documento es un mapa clave-valor.
                val materia = hashMapOf(
                    "nombre"     to nombre,
                    "horario"    to horario,
                    "profesorId" to profesorUid,
                    "alumnos"    to listOf<String>()  // lista vacía al inicio
                )

                // add() crea un documento nuevo con ID autogenerado por Firestore
                db.collection("materias").add(materia)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Materia '$nombre' creada exitosamente",
                            Toast.LENGTH_LONG
                        ).show()
                        // Limpiamos los campos
                        etNombreMateria.text.clear()
                        etHorario.text.clear()
                        etEmailProfesor.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al crear: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al buscar profesor: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}