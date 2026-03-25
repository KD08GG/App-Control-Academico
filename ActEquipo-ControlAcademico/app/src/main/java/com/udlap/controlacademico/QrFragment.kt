package com.udlap.controlacademico

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

/**
 * QrFragment — Genera un código QR para el pase de lista.
 *
 * Flujo:
 * 1. Carga las materias donde el alumno está inscrito (campo "alumnos" contiene su UID).
 * 2. El alumno selecciona la materia en un Spinner.
 * 3. Al presionar "Generar QR", se crea un QR con formato:
 *    "<uid_alumno>|<id_materia>"
 *    Este string es el que escaneará el profesor para registrar asistencia.
 */
class QrFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var spinnerMaterias: Spinner
    private lateinit var btnGenerarQr: Button
    private lateinit var imgQr: ImageView
    private lateinit var tvInstruccion: TextView
    private lateinit var progressBar: ProgressBar

    // Mapas para relacionar nombre mostrado ↔ ID de Firestore
    private val nombresMaterias = mutableListOf<String>()
    private val idsMaterias     = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_qr, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db             = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        spinnerMaterias = view.findViewById(R.id.spinnerMateriasQr)
        btnGenerarQr    = view.findViewById(R.id.btnGenerarQr)
        imgQr           = view.findViewById(R.id.imgQr)
        tvInstruccion   = view.findViewById(R.id.tvInstruccionQr)
        progressBar     = view.findViewById(R.id.progressQr)

        cargarMaterias()

        btnGenerarQr.setOnClickListener { generarQr() }
    }

    /**
     * Consulta Firestore para obtener las materias donde el UID del alumno
     * está en el array "alumnos" del documento de materia.
     */
    private fun cargarMaterias() {
        val uid = sessionManager.obtenerUid() ?: return
        progressBar.visibility = View.VISIBLE

        db.collection("materias")
            .whereArrayContains("alumnos", uid)
            .get()
            .addOnSuccessListener { resultado ->
                progressBar.visibility = View.GONE
                nombresMaterias.clear()
                idsMaterias.clear()

                if (resultado.isEmpty) {
                    tvInstruccion.text = "No estás inscrito en ninguna materia."
                    btnGenerarQr.isEnabled = false
                    return@addOnSuccessListener
                }

                for (doc in resultado.documents) {
                    nombresMaterias.add(doc.getString("nombre") ?: "Sin nombre")
                    idsMaterias.add(doc.id)
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    nombresMaterias
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMaterias.adapter = adapter
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar materias", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Genera el QR usando la librería ZXing.
     * El contenido del QR es: "<uid>|<idMateria>"
     * El profesor lo escanea y separa por "|" para saber qué alumno en qué materia.
     */
    private fun generarQr() {
        val uid = sessionManager.obtenerUid() ?: return
        val index = spinnerMaterias.selectedItemPosition
        if (index < 0 || index >= idsMaterias.size) {
            Toast.makeText(requireContext(), "Selecciona una materia", Toast.LENGTH_SHORT).show()
            return
        }

        val idMateria   = idsMaterias[index]
        val contenidoQr = "$uid|$idMateria"

        try {
            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.encodeBitmap(
                contenidoQr,
                BarcodeFormat.QR_CODE,
                600, 600
            )
            imgQr.setImageBitmap(bitmap)
            imgQr.visibility    = View.VISIBLE
            tvInstruccion.text  = "Muestra este QR al profesor para registrar tu asistencia en:\n${nombresMaterias[index]}"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al generar QR: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}