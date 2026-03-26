package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsuariosFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var authPrincipal: FirebaseAuth

    // Sección agregar usuario
    private lateinit var etNombreNuevoUsuario: EditText
    private lateinit var etEmailNuevoUsuario: EditText
    private lateinit var etPasswordNuevoUsuario: EditText
    private lateinit var rgRolesNuevoUsuario: RadioGroup
    private lateinit var btnAgregarUsuario: Button

    // Sección actualizar rol
    private lateinit var etEmailUsuario: EditText
    private lateinit var rgRoles: RadioGroup
    private lateinit var btnActualizarRol: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admin_usuarios, container, false)

        db = FirebaseFirestore.getInstance()
        authPrincipal = FirebaseAuth.getInstance()

        // Agregar usuario
        etNombreNuevoUsuario = view.findViewById(R.id.etNombreNuevoUsuarioAdmin)
        etEmailNuevoUsuario = view.findViewById(R.id.etEmailNuevoUsuarioAdmin)
        etPasswordNuevoUsuario = view.findViewById(R.id.etPasswordNuevoUsuarioAdmin)
        rgRolesNuevoUsuario = view.findViewById(R.id.rgRolesNuevoUsuarioAdmin)
        btnAgregarUsuario = view.findViewById(R.id.btnAgregarUsuarioAdmin)

        // Actualizar rol
        etEmailUsuario = view.findViewById(R.id.etEmailUsuarioAdmin)
        rgRoles = view.findViewById(R.id.rgRolesAdmin)
        btnActualizarRol = view.findViewById(R.id.btnActualizarRol)

        btnAgregarUsuario.setOnClickListener { agregarUsuarioNuevoConAuth() }
        btnActualizarRol.setOnClickListener { cambiarRolUsuario() }

        return view
    }

    private fun agregarUsuarioNuevoConAuth() {
        val nombre = etNombreNuevoUsuario.text.toString().trim()
        val email = etEmailNuevoUsuario.text.toString().trim().lowercase()
        val password = etPasswordNuevoUsuario.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(context, "Ingresa el nombre del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(context, "Ingresa el correo del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val rolSeleccionado = when (rgRolesNuevoUsuario.checkedRadioButtonId) {
            R.id.rbNuevoProfesor -> "profesor"
            R.id.rbNuevoAdmin -> "admin"
            else -> "alumno"
        }

        db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { resultado ->
                if (!resultado.isEmpty) {
                    Toast.makeText(
                        context,
                        "Ya existe un usuario con ese correo en Firestore",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                crearUsuarioEnAuthYFirestore(nombre, email, password, rolSeleccionado)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Error al validar usuario: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun crearUsuarioEnAuthYFirestore(
        nombre: String,
        email: String,
        password: String,
        rol: String
    ) {
        try {
            val appName = "SecondaryApp"

            val existingApp = try {
                FirebaseApp.getInstance(appName)
            } catch (e: IllegalStateException) {
                null
            }

            val secondaryApp = existingApp ?: FirebaseApp.initializeApp(
                requireContext(),
                FirebaseApp.getInstance().options,
                appName
            )

            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

            secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val nuevoUid = authResult.user?.uid

                    if (nuevoUid == null) {
                        Toast.makeText(
                            context,
                            "No se pudo obtener el UID del nuevo usuario",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    val nuevoUsuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "rol" to rol
                    )

                    db.collection("usuarios")
                        .document(nuevoUid)
                        .set(nuevoUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Usuario creado correctamente como $rol",
                                Toast.LENGTH_LONG
                            ).show()

                            etNombreNuevoUsuario.text.clear()
                            etEmailNuevoUsuario.text.clear()
                            etPasswordNuevoUsuario.text.clear()
                            rgRolesNuevoUsuario.check(R.id.rbNuevoAlumno)

                            secondaryAuth.signOut()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error al guardar en Firestore: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error al crear usuario en Auth: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error general: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cambiarRolUsuario() {
        val email = etEmailUsuario.text.toString().trim().lowercase()

        if (email.isEmpty()) {
            Toast.makeText(context, "Ingresa el correo del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val rolSeleccionado = when (rgRoles.checkedRadioButtonId) {
            R.id.rbProfesor -> "profesor"
            R.id.rbAdmin -> "admin"
            else -> "alumno"
        }

        db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    Toast.makeText(
                        context,
                        "No se encontró usuario con ese correo",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val documento = resultado.documents[0]
                documento.reference.update("rol", rolSeleccionado)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Rol de $email cambiado a $rolSeleccionado",
                            Toast.LENGTH_LONG
                        ).show()

                        etEmailUsuario.text.clear()
                        rgRoles.check(R.id.rbAlumno)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Error al buscar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}