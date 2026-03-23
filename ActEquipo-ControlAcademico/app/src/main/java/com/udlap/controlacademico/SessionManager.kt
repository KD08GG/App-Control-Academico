package com.udlap.controlacademico

import android.content.Context
import android.content.SharedPreferences

/** SessionManager — Gestor de sesión local con SharedPreferences.
 * Para guardar el UID y el rol del usuario para no tener que consultar Firestore en cada arranque. */

class SessionManager(context: Context) { /* Contex para acceder al almacenamiento local */

    // "prefs" es el objeto que lee y escribe en el almacenamiento local.
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)

    // Constantes para las claves, evita errores de tipeo
    companion object {
        private const val KEY_UID  = "uid" // El ID único del usuario en Firebase Authentication.
        private const val KEY_ROL  = "rol" // "admin", "profesor" o "alumno".
        private const val KEY_NOMBRE = "nombre" // El nombre completo del usuario.
    }

    //Guarda los datos del usuario después de un login exitoso.
    fun guardarSesion(uid: String, rol: String, nombre: String) {
        // edit() abre el almacenamiento para escritura.
        // apply() guarda los cambios de forma asíncrona.
        prefs.edit()
            .putString(KEY_UID, uid)
            .putString(KEY_ROL, rol)
            .putString(KEY_NOMBRE, nombre)
            .apply()
    }

    //Obtiene el UID guardado. Retorna null si no hay sesión activa.
    fun obtenerUid(): String? = prefs.getString(KEY_UID, null)

    //Obtiene el rol guardado. Retorna null si no hay sesión activa.
    fun obtenerRol(): String? = prefs.getString(KEY_ROL, null)

    //Obtiene el nombre guardado. Retorna cadena vacía si no existe.
    fun obtenerNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""

    //Verifica si hay una sesión guardada localmente.
    fun haySesion(): Boolean = obtenerUid() != null

    //Borra todos los datos de sesión guardados.
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}