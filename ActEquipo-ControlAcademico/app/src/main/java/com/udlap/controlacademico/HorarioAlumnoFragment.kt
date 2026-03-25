package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

/**
 * HorarioAlumnoFragment — Muestra el horario de clases del alumno.
 *
 * Consulta todas las materias donde el UID del alumno está en el array "alumnos",
 * y muestra el nombre + horario de cada una en una ListView.
 */
class HorarioAlumnoFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var listViewHorario: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSinMaterias: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_horario_alumno, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db             = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        listViewHorario = view.findViewById(R.id.listViewHorarioAlumno)
        progressBar     = view.findViewById(R.id.progressHorarioAlumno)
        tvSinMaterias   = view.findViewById(R.id.tvSinMateriasAlumno)

        cargarHorario()
    }

    private fun cargarHorario() {
        val uid = sessionManager.obtenerUid() ?: return
        progressBar.visibility = View.VISIBLE
        tvSinMaterias.visibility = View.GONE

        db.collection("materias")
            .whereArrayContains("alumnos", uid)
            .get()
            .addOnSuccessListener { resultado ->
                progressBar.visibility = View.GONE

                if (resultado.isEmpty) {
                    tvSinMaterias.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val lineas = resultado.documents.map { doc ->
                    val nombre  = doc.getString("nombre")  ?: "Sin nombre"
                    val horario = doc.getString("horario") ?: "Sin horario"
                    "$nombre\n  $horario"
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    lineas
                )
                listViewHorario.adapter = adapter
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar horario", Toast.LENGTH_SHORT).show()
            }
    }
}