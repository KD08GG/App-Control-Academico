package com.udlap.controlacademico

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QrFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private lateinit var spinnerMateriasQr: Spinner
    private lateinit var btnGenerarQr: Button
    private lateinit var imgQr: ImageView
    private lateinit var progressQr: ProgressBar
    private lateinit var tvInstruccionQr: TextView

    private lateinit var listViewAsistenciaAlumno: ListView
    private lateinit var progressListaAsistenciaAlumno: ProgressBar
    private lateinit var tvSinListaAsistenciaAlumno: TextView

    private val listaMaterias = mutableListOf<String>()
    private val listaIdsMaterias = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        spinnerMateriasQr = view.findViewById(R.id.spinnerMateriasQr)
        btnGenerarQr = view.findViewById(R.id.btnGenerarQr)
        imgQr = view.findViewById(R.id.imgQr)
        progressQr = view.findViewById(R.id.progressQr)
        tvInstruccionQr = view.findViewById(R.id.tvInstruccionQr)

        listViewAsistenciaAlumno = view.findViewById(R.id.listViewAsistenciaAlumno)
        progressListaAsistenciaAlumno = view.findViewById(R.id.progressListaAsistenciaAlumno)
        tvSinListaAsistenciaAlumno = view.findViewById(R.id.tvSinListaAsistenciaAlumno)

        cargarMateriasAlumno()
        cargarListaAsistenciaAlumno()

        btnGenerarQr.setOnClickListener {
            generarQrMateriaSeleccionada()
        }
    }

    private fun cargarMateriasAlumno() {
        val uidAlumno = sessionManager.obtenerUid()

        if (uidAlumno == null) {
            Toast.makeText(requireContext(), "No se encontró la sesión del alumno", Toast.LENGTH_SHORT).show()
            return
        }

        progressQr.visibility = View.VISIBLE

        db.collection("materias")
            .whereArrayContains("alumnos", uidAlumno)
            .get()
            .addOnSuccessListener { resultado ->
                progressQr.visibility = View.GONE
                listaMaterias.clear()
                listaIdsMaterias.clear()

                if (resultado.isEmpty) {
                    Toast.makeText(
                        requireContext(),
                        "No estás inscrito en ninguna materia",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                for (documento in resultado.documents) {
                    val nombre = documento.getString("nombre") ?: "Materia sin nombre"
                    listaMaterias.add(nombre)
                    listaIdsMaterias.add(documento.id)
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listaMaterias
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMateriasQr.adapter = adapter
            }
            .addOnFailureListener { e ->
                progressQr.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error al cargar materias: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun generarQrMateriaSeleccionada() {
        val uidAlumno = sessionManager.obtenerUid()

        if (uidAlumno == null) {
            Toast.makeText(requireContext(), "No se encontró la sesión del alumno", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaIdsMaterias.isEmpty()) {
            Toast.makeText(requireContext(), "No hay materias disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val posicion = spinnerMateriasQr.selectedItemPosition
        val idMateria = listaIdsMaterias[posicion]
        val nombreMateria = listaMaterias[posicion]

        val contenidoQr = "$uidAlumno|$idMateria"

        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(contenidoQr, BarcodeFormat.QR_CODE, 600, 600)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }

            imgQr.setImageBitmap(bitmap)
            imgQr.visibility = View.VISIBLE
            tvInstruccionQr.text = "QR generado para: $nombreMateria"

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error al generar QR: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarListaAsistenciaAlumno() {
        val uidAlumno = sessionManager.obtenerUid()

        if (uidAlumno == null) {
            Toast.makeText(requireContext(), "No se encontró la sesión del alumno", Toast.LENGTH_SHORT).show()
            return
        }

        progressListaAsistenciaAlumno.visibility = View.VISIBLE
        tvSinListaAsistenciaAlumno.visibility = View.GONE

        db.collection("materias")
            .whereArrayContains("alumnos", uidAlumno)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    progressListaAsistenciaAlumno.visibility = View.GONE
                    tvSinListaAsistenciaAlumno.visibility = View.VISIBLE
                    listViewAsistenciaAlumno.adapter = null
                    return@addOnSuccessListener
                }

                val listaFinal = mutableListOf<String>()
                var pendientes = resultado.size()

                for (documento in resultado.documents) {
                    val idMateria = documento.id
                    val nombreMateria = documento.getString("nombre") ?: "Materia sin nombre"

                    db.collection("materias")
                        .document(idMateria)
                        .collection("asistencias")
                        .document(uidAlumno)
                        .get()
                        .addOnSuccessListener { docAsistencia ->
                            val texto = if (docAsistencia.exists()) {
                                "✅ $nombreMateria"
                            } else {
                                "❌ $nombreMateria"
                            }

                            listaFinal.add(texto)
                            pendientes--

                            if (pendientes == 0) {
                                mostrarListaAsistenciaAlumno(listaFinal)
                            }
                        }
                        .addOnFailureListener {
                            listaFinal.add("❌ $nombreMateria")
                            pendientes--

                            if (pendientes == 0) {
                                mostrarListaAsistenciaAlumno(listaFinal)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                progressListaAsistenciaAlumno.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error al cargar lista de asistencia: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun mostrarListaAsistenciaAlumno(lista: List<String>) {
        progressListaAsistenciaAlumno.visibility = View.GONE

        if (lista.isEmpty()) {
            tvSinListaAsistenciaAlumno.visibility = View.VISIBLE
            listViewAsistenciaAlumno.adapter = null
            return
        }

        tvSinListaAsistenciaAlumno.visibility = View.GONE

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            lista
        )
        listViewAsistenciaAlumno.adapter = adapter
    }
}