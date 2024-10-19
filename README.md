# MedicControl
[![Project Status](https://img.shields.io/badge/status-brightgreen.svg)](https://github.com/bpdevop/MedicControl)

MedicControl es una aplicación diseñada para ayudar a médicos independientes a gestionar de manera eficiente los historiales médicos de sus pacientes. La aplicación permite el registro, consulta y generación de reportes médicos, automatizando tareas administrativas y facilitando el cumplimiento de las normativas del Ministerio de Salud Pública y Asistencia Social (MSPAS). MedicControl está desarrollado utilizando **Jetpack Compose**, **Firebase** y **Clean Architecture** para asegurar una estructura sólida y escalable.

## Características principales

- **Gestión de pacientes**: Registro, edición y eliminación de pacientes.
- **Generación de informes**: Reportes solicitados por el MSPAS de forma automatizada.
- **Historial médico**: Almacenamiento y acceso seguro a los historiales médicos de cada paciente.
- **Exportación de recetas**: Funcionalidad para generar y exportar recetas en formato PDF.
- **Autenticación**: Gestión de usuarios y sesiones mediante Firebase Authentication.
- **Compatibilidad multiplataforma**: Disponible para dispositivos Android.

## Tecnologías utilizadas

- **Kotlin**: Lenguaje principal del proyecto.
- **Jetpack Compose**: Framework de UI para construir interfaces modernas y declarativas.
- **Firebase Firestore**: Base de datos en la nube para almacenar los datos de los pacientes.
- **Firebase Authentication**: Autenticación segura para la gestión de usuarios.
- **Hilt**: Inyección de dependencias para mejorar la modularidad y el mantenimiento del código.
- **Material Design 3**: Para garantizar una interfaz amigable y consistente.

## Arquitectura

El proyecto sigue el patrón **Clean Architecture**, que permite una separación clara entre las capas de presentación, dominio y datos, facilitando la escalabilidad y el mantenimiento.

- **Presentación**: Jetpack Compose para la UI.
- **Dominio**: Lógica de negocio y casos de uso.
- **Datos**: Repositorios que interactúan con Firebase Firestore y otras fuentes de datos.


## Requisitos

- **Android Studio 4.2+**
- **JDK 11+**
- **Gradle 7.0+**
- **Firebase Account**: Para la configuración de Firestore y Authentication.

## Configuración del proyecto
1. Clona el repositorio:
  ```bash
   git clone https://github.com/bpdevop/MedicControl.git
   ```

2. **Abre el proyecto en Android Studio.**

3. **Configura Firebase en tu proyecto:**
   - Crea un nuevo proyecto en [Firebase Console](https://console.firebase.google.com/).
   - Habilita **Firestore** y **Authentication**.
   - Descarga y coloca el archivo `google-services.json` en la carpeta `app`.

4. **Ejecuta el proyecto en un emulador o dispositivo Android.**

## Uso de la Aplicación

Una vez instalada la aplicación, los médicos pueden:

- **Registrar nuevos pacientes.**
- **Consultar y modificar los historiales médicos.**
- **Generar informes solicitados por el MSPAS.**
- **Exportar recetas médicas en formato PDF.**
- **Iniciar sesión de manera segura utilizando Firebase Authentication.**

## Licencia

Este proyecto está licenciado bajo la [Licencia MIT](LICENSE). Para más detalles, consulta el archivo `LICENSE`.