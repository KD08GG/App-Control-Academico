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
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsuariosFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etEmailUsuario: EditText
    private lateinit var rgRoles: RadioGroup
    private lateinit var btnActualizarRol: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_usuarios, container, false)
        db = FirebaseFirestore.getInstance()

        etEmailUsuario = view.findViewById(R.id.etEmailUsuarioAdmin)
        rgRoles = view.findViewById(R.id.rgRolesAdmin)
        btnActualizarRol = view.findViewById(R.id.btnActualizarRol)

        btnActualizarRol.setOnClickListener { cambiarRolUsuario() }

        return view
    }

    private fun cambiarRolUsuario() {
        val email = etEmailUsuario.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(context, "Ingresa el correo del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val rolSeleccionado = when (rgRoles.checkedRadioButtonId) {
            R.id.rbProfesor -> "profesor"
            R.id.rbAdmin    -> "admin"
            else            -> "alumno"
        }

        db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    Toast.makeText(context, "No se encontró usuario con ese correo", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val documento = resultado.documents[0]
                documento.reference.update("rol", rolSeleccionado)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Rol de $email cambiado a $rolSeleccionado", Toast.LENGTH_LONG).show()
                        etEmailUsuario.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al buscar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}