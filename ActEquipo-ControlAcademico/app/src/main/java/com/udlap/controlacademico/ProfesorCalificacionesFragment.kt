package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class ProfesorCalificacionesFragment : Fragment() {

    private lateinit var spinnerMaterias: Spinner
    private lateinit var spinnerAlumnos: Spinner
    private lateinit var etCalificacion: EditText
    private lateinit var btnGuardar: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private val listaMaterias = mutableListOf<String>()
    private val listaIdsMaterias = mutableListOf<String>()

    private val listaAlumnos = mutableListOf<String>()
    private val listaIdsAlumnos = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profesor_calificaciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerMaterias = view.findViewById(R.id.spinnerMateriasCalifProfesor)
        spinnerAlumnos = view.findViewById(R.id.spinnerAlumnosCalifProfesor)
        etCalificacion = view.findViewById(R.id.etCalificacionProfesor)
        btnGuardar = view.findViewById(R.id.btnGuardarCalificacionProfesor)

        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        cargarMateriasProfesor()

        spinnerMaterias.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaIdsMaterias.isNotEmpty() && listaIdsMaterias[position].isNotEmpty()) {
                    cargarAlumnosDeMateria(listaIdsMaterias[position])
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        btnGuardar.setOnClickListener {
            guardarCalificacion()
        }
    }

    private fun cargarMateriasProfesor() {
        val uidProfesor = sessionManager.obtenerUid()

        if (uidProfesor == null) {
            Toast.makeText(requireContext(), "No se encontró la sesión del profesor", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("materias")
            .whereEqualTo("profesorId", uidProfesor)
            .get()
            .addOnSuccessListener { resultado ->
                listaMaterias.clear()
                listaIdsMaterias.clear()

                if (resultado.isEmpty) {
                    listaMaterias.add("No tienes materias asignadas")
                    listaIdsMaterias.add("")
                } else {
                    for (documento in resultado.documents) {
                        val nombre = documento.getString("nombre") ?: "Materia sin nombre"
                        listaMaterias.add(nombre)
                        listaIdsMaterias.add(documento.id)
                    }
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listaMaterias
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMaterias.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar materias: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun cargarAlumnosDeMateria(idMateria: String) {
        db.collection("materias")
            .document(idMateria)
            .get()
            .addOnSuccessListener { documento ->
                listaAlumnos.clear()
                listaIdsAlumnos.clear()

                val alumnosIds = documento.get("alumnos") as? List<*>

                if (alumnosIds.isNullOrEmpty()) {
                    listaAlumnos.add("No hay alumnos inscritos")
                    listaIdsAlumnos.add("")
                    actualizarSpinnerAlumnos()
                    return@addOnSuccessListener
                }

                var pendientes = alumnosIds.size

                for (uid in alumnosIds) {
                    val uidAlumno = uid as? String ?: continue

                    db.collection("usuarios")
                        .document(uidAlumno)
                        .get()
                        .addOnSuccessListener { docAlumno ->
                            val nombreAlumno = docAlumno.getString("nombre") ?: "Alumno sin nombre"
                            listaAlumnos.add(nombreAlumno)
                            listaIdsAlumnos.add(uidAlumno)

                            pendientes--
                            if (pendientes == 0) {
                                actualizarSpinnerAlumnos()
                            }
                        }
                        .addOnFailureListener {
                            pendientes--
                            if (pendientes == 0) {
                                actualizarSpinnerAlumnos()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar alumnos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun actualizarSpinnerAlumnos() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listaAlumnos
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAlumnos.adapter = adapter
    }

    private fun guardarCalificacion() {
        if (listaIdsMaterias.isEmpty() || listaIdsAlumnos.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona una materia y un alumno", Toast.LENGTH_SHORT).show()
            return
        }

        val posicionMateria = spinnerMaterias.selectedItemPosition
        val posicionAlumno = spinnerAlumnos.selectedItemPosition

        val idMateria = listaIdsMaterias[posicionMateria]
        val uidAlumno = listaIdsAlumnos[posicionAlumno]
        val nombreAlumno = listaAlumnos[posicionAlumno]

        if (idMateria.isEmpty() || uidAlumno.isEmpty()) {
            Toast.makeText(requireContext(), "Selección inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val textoCalificacion = etCalificacion.text.toString().trim()

        if (textoCalificacion.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa una calificación", Toast.LENGTH_SHORT).show()
            return
        }

        val calificacion = textoCalificacion.toDoubleOrNull()

        if (calificacion == null) {
            Toast.makeText(requireContext(), "Calificación inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = hashMapOf(
            "calificacion" to calificacion,
            "nombre" to nombreAlumno
        )

        db.collection("materias")
            .document(idMateria)
            .collection("calificaciones")
            .document(uidAlumno)
            .set(datos)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Calificación guardada correctamente", Toast.LENGTH_SHORT).show()
                etCalificacion.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}