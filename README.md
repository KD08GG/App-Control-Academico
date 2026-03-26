# Control Académico - Aplicación Android

[![Kotlin](https://img.shields.io/badge/Kotlin-Android-blue)]()
[![Firebase](https://img.shields.io/badge/Firebase-Backend-orange)]()
[![Platform](https://img.shields.io/badge/Platform-Android-green)]()
[![API](https://img.shields.io/badge/API-24%2B-lightgrey)]()

Aplicación móvil desarrollada en Android con Kotlin para la gestión académica de materias, alumnos y profesores. El sistema integra autenticación segura, base de datos en la nube y control de asistencia mediante códigos QR.

---

## Descripción

Control Académico es una solución móvil diseñada para optimizar la administración de información educativa mediante tres roles principales: Administrador, Profesor y Alumno. La aplicación permite gestionar materias, registrar asistencia en tiempo real y consultar calificaciones de forma eficiente.

El sistema está respaldado por Firebase, lo que garantiza persistencia, sincronización en tiempo real y escalabilidad.

---

## Vista General

### Pantallas principales

Agrega aquí tus capturas de pantalla (recomendado subirlas a una carpeta `/screenshots` en el repositorio):

```
/screenshots/admin.png
/screenshots/profesor.png
/screenshots/alumno.png
```

Luego puedes mostrarlas así:

![Admin](screenshots/admin.png)
![Profesor](screenshots/profesor.png)
![Alumno](screenshots/alumno.png)

---

## Demo

APK disponible para instalación:

* Descargar APK: (agrega aquí el enlace a GitHub Releases o Drive)

---

## Arquitectura del Sistema

### Backend (Firebase)

* Firebase Authentication para gestión de usuarios
* Cloud Firestore como base de datos NoSQL

Estructura:

* usuarios

  * UID
  * correo
  * rol

* materias

  * nombre
  * horario
  * profesorId
  * alumnos

* calificaciones (subcolección)

  * alumno
  * nota

---

### Frontend (Android - Kotlin)

* Arquitectura basada en Activities y Fragments
* Navegación dinámica con FragmentManager
* Uso de adapters personalizados para listas y menús

---

### Sistema de Asistencia con QR

* Generación:

  * Formato: UID|ID_MATERIA
  * Implementado con ZXing

* Escaneo:

  * Lectura mediante cámara
  * Procesamiento del contenido

* Validación:

  * Coincidencia de materia antes de registrar asistencia

---

### Persistencia de Sesión

* Implementada con SharedPreferences
* Almacena UID, correo y rol
* Permite inicio automático sin reautenticación

---

## Funcionalidades

### Administrador

* Gestión de usuarios
* Creación de materias
* Asignación de profesores y alumnos

### Profesor

* Registro de asistencia mediante QR
* Gestión de calificaciones
* Consulta de horario

### Alumno

* Generación de QR
* Consulta de calificaciones
* Consulta de horario

---

## Flujo de Uso

1. El administrador configura materias y usuarios.
2. El alumno genera su código QR.
3. El profesor escanea el QR y registra asistencia.
4. El profesor asigna calificaciones.
5. El alumno consulta su información académica.

---

## Tecnologías Utilizadas

* Kotlin
* Android SDK (API 24+)
* Firebase Authentication
* Cloud Firestore
* ZXing
* SharedPreferences

---

## Instalación

```bash
git clone https://github.com/KD08GG/App-Control-Academico
```

1. Abrir en Android Studio
2. Configurar Firebase (google-services.json)
3. Ejecutar en dispositivo o emulador

---

## Estructura del Proyecto

Ejemplo de organización:

```
/app
  /activities
  /fragments
  /adapters
  /models
  /utils
```

---

## Mejoras Futuras

* Notificaciones en tiempo real
* Panel web administrativo
* Exportación de datos académicos
* Mayor robustez en validaciones

---

## Autoría

Proyecto desarrollado como parte del programa de Ingeniería en Sistemas Computacionales.

---

## Licencia

Uso académico.
