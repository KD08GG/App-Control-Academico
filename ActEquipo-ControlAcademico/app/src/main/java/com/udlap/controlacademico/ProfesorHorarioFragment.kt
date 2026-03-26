package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class ProfesorHorarioFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var listViewHorario: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSinHorario: TextView
    private lateinit var tvInfo: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profesor_horario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        listViewHorario = view.findViewById(R.id.listViewHorarioProfesor)
        progressBar = view.findViewById(R.id.progressHorarioProfesor)
        tvSinHorario = view.findViewById(R.id.tvSinHorarioProfesor)
        tvInfo = view.findViewById(R.id.tvHorarioProfesorInfo)

        cargarHorarioProfesor()
    }

    private fun cargarHorarioProfesor() {
        val uidProfesor = sessionManager.obtenerUid()

        if (uidProfesor == null) {
            Toast.makeText(requireContext(), "No se encontró la sesión del profesor", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        tvSinHorario.visibility = View.GONE

        db.collection("materias")
            .whereEqualTo("profesorId", uidProfesor)
            .get()
            .addOnSuccessListener { resultado ->
                progressBar.visibility = View.GONE

                if (resultado.isEmpty) {
                    tvSinHorario.visibility = View.VISIBLE
                    listViewHorario.adapter = null
                    return@addOnSuccessListener
                }

                val listaHorario = mutableListOf<String>()

                for (documento in resultado.documents) {
                    val nombreMateria = documento.getString("nombre") ?: "Materia sin nombre"
                    val horario = documento.getString("horario") ?: "Horario no disponible"

                    listaHorario.add(
                        "$nombreMateria\nHorario: $horario"
                    )
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    listaHorario
                )

                listViewHorario.adapter = adapter
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error al cargar horario: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}