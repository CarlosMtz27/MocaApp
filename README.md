# MocaApp 

**MocaApp** es una aplicación Android diseñada para el registro de momentos compartidos y el conocimiento mutuo mediante cuestionarios y dinámicas .

## 🚀 Características Principales

- **Chat en Tiempo Real:** Comunicación instantánea con tu pareja, incluyendo soporte para mensajes de texto, fotos, videos y audios.
- **Diario Compartido:** Registra tus días, emociones y momentos especiales con fotos y videos. Puedes elegir qué recuerdos compartir con tu pareja.
- **Calendario de Recuerdos:** Visualiza tus entradas del diario en un formato de calendario para revivir momentos pasados.
- **Cuestionarios y Dinámicas:** Fortalece el vínculo respondiendo preguntas y participando en retos diseñados para parejas.
- **Notificaciones Push:** Mantente al tanto de nuevos mensajes, recuerdos compartidos y actividades pendientes mediante OneSignal.
- **Gestión de Perfil:** Personaliza tu información, sube tu foto de perfil (vía Cloudinary) y vincula tu cuenta con la de tu pareja.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture (por módulos).
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) para una interfaz moderna y declarativa.
- **Backend/Base de Datos:** [Firebase](https://firebase.google.com/) (Auth, Firestore, Storage, Messaging).
- **Notificaciones:** [OneSignal](https://onesignal.com/).
- **Almacenamiento de Multimedia:** [Cloudinary](https://cloudinary.com/) para la gestión de imágenes.
- **Inyección de Dependencias:** Implementación manual orientada a modularización.
- **Estructura Modular:**
    - `:app`: Punto de entrada y ensamblado de la aplicación.
    - `:feature`: Lógica de negocio y UI de las funcionalidades (Chat, Diario, Auth, etc.).
    - `:core`: Modelos de datos comunes, utilidades y lógica compartida.
    - `:widgets`: Componentes de UI reutilizables.

## ⚙️ Configuración del Proyecto

Para ejecutar este proyecto, deberás configurar las siguientes claves en tu archivo `local.properties`:

```properties
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
ONESIGNAL_APP_ID=tu_onesignal_app_id
ONESIGNAL_REST_KEY=tu_onesignal_rest_key
```

Además, asegúrate de incluir el archivo `google-services.json` en el directorio `/app` para la integración con Firebase.

## 📁 Estructura del Código

El código está organizado por módulos de características dentro de `:feature`:
- `auth`: Registro, inicio de sesión y recuperación de contraseña.
- `chat`: Mensajería individual con soporte multimedia.
- `diario`: Gestión de entradas diarias, emociones y archivos multimedia.
- `cuestionarios`: Lógica de preguntas y respuestas para parejas.
- `notificaciones`: Repositorios y servicios para el manejo de alertas y badges.
- `perfil`: Gestión de datos de usuario y vinculación de pareja.
