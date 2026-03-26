package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminAcademicaFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    
    // UI Crear Materia
    private lateinit var etNombreMateria: EditText
    private lateinit var etHorario: EditText
    private lateinit var etEmailProfesor: EditText
    private lateinit var btnCrearMateria: Button

    // UI Inscribir Alumno
    private lateinit var etEmailAlumno: EditText
    private lateinit var spinnerMaterias: Spinner
    private lateinit var btnInscribir: Button
    
    private val listaMaterias = mutableListOf<Pair<String, String>>() // Nombre to ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_academica, container, false)
        db = FirebaseFirestore.getInstance()

        // Vincular Crear Materia
        etNombreMateria = view.findViewById(R.id.etNombreMateriaAdmin)
        etHorario = view.findViewById(R.id.etHorarioMateriaAdmin)
        etEmailProfesor = view.findViewById(R.id.etEmailProfesorMateriaAdmin)
        btnCrearMateria = view.findViewById(R.id.btnCrearMateriaAdmin)

        // Vincular Inscribir Alumno
        etEmailAlumno = view.findViewById(R.id.etEmailAlumnoInscribir)
        spinnerMaterias = view.findViewById(R.id.spinnerMateriasInscribir)
        btnInscribir = view.findViewById(R.id.btnInscribirAlumno)

        btnCrearMateria.setOnClickListener { crearMateria() }
        btnInscribir.setOnClickListener { inscribirAlumno() }
        
        cargarMateriasEnSpinner()

        return view
    }

    private fun cargarMateriasEnSpinner() {
        db.collection("materias").get()
            .addOnSuccessListener { result ->
                listaMaterias.clear()
                val nombres = mutableListOf<String>()
                for (doc in result) {
                    val nombre = doc.getString("nombre") ?: "Sin nombre"
                    listaMaterias.add(nombre to doc.id)
                    nombres.add(nombre)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMaterias.adapter = adapter
            }
    }

    private fun crearMateria() {
        val nombre = etNombreMateria.text.toString().trim()
        val horario = etHorario.text.toString().trim()
        val emailProfesor = etEmailProfesor.text.toString().trim()

        if (nombre.isEmpty() || horario.isEmpty() || emailProfesor.isEmpty()) {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("usuarios")
            .whereEqualTo("email", emailProfesor)
            .whereEqualTo("rol", "profesor")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    Toast.makeText(context, "No se encontró un profesor con ese correo", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val profesorUid = resultado.documents[0].id
                val materia = hashMapOf(
                    "nombre" to nombre,
                    "horario" to horario,
                    "profesorId" to profesorUid,
                    "alumnos" to listOf<String>()
                )

                db.collection("materias").add(materia)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Materia '$nombre' creada exitosamente", Toast.LENGTH_LONG).show()
                        etNombreMateria.text.clear()
                        etHorario.text.clear()
                        etEmailProfesor.text.clear()
                        cargarMateriasEnSpinner() // Actualizar lista
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al crear: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
    }

    private fun inscribirAlumno() {
        val email = etEmailAlumno.text.toString().trim()
        val pos = spinnerMaterias.selectedItemPosition
        
        if (email.isEmpty() || pos == -1) {
            Toast.makeText(context, "Ingresa el correo del alumno", Toast.LENGTH_SHORT).show()
            return
        }

        val materiaId = listaMaterias[pos].second

        // 1. Buscar al alumno por email para validar rol y obtener su ID
        db.collection("usuarios")
            .whereEqualTo("email", email)
            .whereEqualTo("rol", "alumno")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(context, "No se encontró un alumno con ese correo", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                
                val alumnoId = result.documents[0].id
                
                // 2. Agregar el ID del alumno a la lista 'alumnos' de la materia
                db.collection("materias").document(materiaId)
                    .update("alumnos", FieldValue.arrayUnion(alumnoId))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Alumno inscrito correctamente", Toast.LENGTH_SHORT).show()
                        etEmailAlumno.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al inscribir: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
    }
}