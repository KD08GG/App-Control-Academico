package com.udlap.controlacademico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ProfesorAsistenciaFragment : Fragment() {

    private lateinit var spinnerMateriasProfesor: Spinner
    private lateinit var btnEscanearQr: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    private val listaMaterias = mutableListOf<String>()
    private val listaIdsMaterias = mutableListOf<String>()

    private val qrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentResult: IntentResult =
            IntentIntegrator.parseActivityResult(result.resultCode, result.data)

        if (intentResult.contents != null) {
            procesarQrEscaneado(intentResult.contents)
        } else {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profesor_asistencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerMateriasProfesor = view.findViewById(R.id.spinnerMateriasProfesor)
        btnEscanearQr = view.findViewById(R.id.btnEscanearQr)

        db = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(requireContext())

        cargarMateriasProfesor()

        btnEscanearQr.setOnClickListener {
            if (listaMaterias.isEmpty() || listaIdsMaterias.isEmpty() || listaIdsMaterias[0].isEmpty()) {
                Toast.makeText(requireContext(), "No hay materias disponibles", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            abrirEscanerQr()
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
                spinnerMateriasProfesor.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error al cargar materias: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun abrirEscanerQr() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea el QR del alumno")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.setCaptureActivity(CaptureAct::class.java)

        val intent = integrator.createScanIntent()
        qrLauncher.launch(intent)
    }

    private fun procesarQrEscaneado(contenidoQr: String) {
        val partes = contenidoQr.split("|")

        if (partes.size != 2) {
            Toast.makeText(requireContext(), "QR inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val uidAlumno = partes[0]
        val idMateriaQr = partes[1]

        val posicion = spinnerMateriasProfesor.selectedItemPosition
        val idMateriaSeleccionada = listaIdsMaterias[posicion]
        val nombreMateriaSeleccionada = listaMaterias[posicion]

        if (idMateriaQr != idMateriaSeleccionada) {
            Toast.makeText(
                requireContext(),
                "El QR no pertenece a la materia seleccionada",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Toast.makeText(
            requireContext(),
            "QR válido\nAlumno: $uidAlumno\nMateria: $nombreMateriaSeleccionada",
            Toast.LENGTH_LONG
        ).show()

        // Aquí en el siguiente paso guardaremos la asistencia en Firebase
    }
}