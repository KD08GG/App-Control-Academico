package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

/**
 * CalifFragment — Muestra las calificaciones del alumno.
 *
 * Estructura en Firestore:
 *   materias/{idMateria}/calificaciones/{uid_alumno}
 *     - calificacion: Number
 *     - nombre: String (nombre del alumno, denormalizado para facilitar consultas)
 *
 * Este fragmento consulta todas las materias donde el alumno está inscrito
 * y, para cada una, lee su calificación en la subcolección.
 */
class CalifFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var listViewCalif: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSinCalif: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calif, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db             = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        listViewCalif = view.findViewById(R.id.listViewCalificaciones)
        progressBar   = view.findViewById(R.id.progressCalif)
        tvSinCalif    = view.findViewById(R.id.tvSinCalificaciones)

        cargarCalificaciones()
    }

    private fun cargarCalificaciones() {
        val uid = sessionManager.obtenerUid() ?: return
        progressBar.visibility = View.VISIBLE
        tvSinCalif.visibility  = View.GONE

        // Paso 1: Obtenemos las materias del alumno
        db.collection("materias")
            .whereArrayContains("alumnos", uid)
            .get()
            .addOnSuccessListener { materias ->
                if (materias.isEmpty) {
                    progressBar.visibility = View.GONE
                    tvSinCalif.visibility  = View.VISIBLE
                    tvSinCalif.text        = "No estás inscrito en ninguna materia."
                    return@addOnSuccessListener
                }

                val lineas     = mutableListOf<String>()
                var pendientes = materias.size()

                // Paso 2: Por cada materia, buscamos la calificación del alumno
                for (materia in materias.documents) {
                    val nombreMateria = materia.getString("nombre") ?: "Sin nombre"
                    val horario       = materia.getString("horario") ?: ""

                    materia.reference
                        .collection("calificaciones")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { calDoc ->
                            val calificacion = if (calDoc.exists()) {
                                val valor = calDoc.getDouble("calificacion")
                                if (valor != null) String.format("%.1f", valor) else "Sin calificar"
                            } else {
                                "Sin calificar"
                            }

                            lineas.add("$nombreMateria\n  Horario: $horario\n  Calificación: $calificacion")
                            pendientes--

                            // Cuando terminamos de consultar todas las materias, mostramos
                            if (pendientes == 0) {
                                progressBar.visibility = View.GONE
                                mostrarLista(lineas)
                            }
                        }
                        .addOnFailureListener {
                            lineas.add("$nombreMateria\n  Error al cargar calificación")
                            pendientes--
                            if (pendientes == 0) {
                                progressBar.visibility = View.GONE
                                mostrarLista(lineas)
                            }
                        }
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar materias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarLista(lineas: List<String>) {
        if (lineas.isEmpty()) {
            tvSinCalif.visibility = View.VISIBLE
            return
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            lineas
        )
        listViewCalif.adapter = adapter
    }
}