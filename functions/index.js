const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

//Helper central

async function enviarNotificacion(destinatarioId, tipo, titulo, cuerpo, deepLink) {
  try {
    //Obtener token FCM del usuario
    const userSnap = await db.collection("usuarios").doc(destinatarioId).get();
    const token = userSnap.data()?.fcmToken;
    if (!token) return;

    //Incrementar badge en Firestore
    await db
      .collection("notificaciones")
      .doc(destinatarioId)
      .collection("noLeidos")
      .doc("contadores")
      .set(
        { [tipo]: admin.firestore.FieldValue.increment(1) },
        { merge: true }
      );

    //Enviar FCM
    await messaging.send({
      token,
      data: { tipo, titulo, cuerpo, deepLink },
      notification: { title: titulo, body: cuerpo },
      android: {
        priority: "high",
        notification: {
          channelId: `canal_${tipo}`,
          sound: "default"
        }
      },
      apns: {
        payload: {
          aps: { sound: "default", badge: 1 }
        }
      }
    });
  } catch (e) {
    console.error(`Error enviando notif a ${destinatarioId}:`, e);
  }
}

//Helper: obtener nombre de usuario
async function obtenerNombre(usuarioId) {
  try {
    const snap = await db.collection("usuarios").doc(usuarioId).get();
    return snap.data()?.nombre ?? "Tu pareja";
  } catch {
    return "Tu pareja";
  }
}

//Helper: obtener parejaId de un usuario
async function obtenerParejaId(usuarioId) {
  try {
    const snap = await db.collection("usuarios").doc(usuarioId).get();
    return snap.data()?.parejaId ?? null;
  } catch {
    return null;
  }
}

// TRIGGER 1: Nuevo mensaje de chat
// Estructura: conversaciones/{convId}/mensajes/{msgId}

exports.onNuevoMensaje = onDocumentCreated(
  "conversaciones/{convId}/mensajes/{msgId}",
  async (event) => {
    const mensaje = event.data.data();
    const { remitenteId, texto } = mensaje;
    if (!remitenteId) return;

    const parejaId = await obtenerParejaId(remitenteId);
    if (!parejaId) return;

    const nombre = await obtenerNombre(remitenteId);
    const cuerpo = texto?.length > 60
      ? texto.substring(0, 60) + "…"
      : (texto ?? "Te envió un mensaje");

    await enviarNotificacion(
      parejaId,
      "chat",
      `💬 ${nombre}`,
      cuerpo,
      "main/chat"
    );
  }
);

// TRIGGER 2: Nueva entrada de diario
// Estructura: entradas/{entradaId}

exports.onNuevaEntradaDiario = onDocumentCreated(
  "entradas/{entradaId}",
  async (event) => {
    const entrada = event.data.data();
    const { autorId, titulo } = entrada;
    if (!autorId) return;

    const parejaId = await obtenerParejaId(autorId);
    if (!parejaId) return;

    const nombre = await obtenerNombre(autorId);

    await enviarNotificacion(
      parejaId,
      "diario",
      `📖 ${nombre} agregó un recuerdo`,
      titulo ?? "Nueva entrada en el diario",
      "main/calendario"
    );
  }
);

// TRIGGER 3: Cuestionario respondido por ambos
// Estructura: resultados/{cuestionarioId}
// Solo dispara cuando se completa (tiene respuestasUsuario y respuestasPareja)

exports.onCuestionarioCompletado = onDocumentCreated(
  "resultados/{cuestionarioId}",
  async (event) => {
    const resultado = event.data.data();
    const { usuarioId, parejaId, cuestionarioId } = resultado;
    if (!usuarioId || !parejaId) return;

    // Obtener título del cuestionario
    let tituloCuestionario = "cuestionario";
    try {
      const cSnap = await db
        .collection("cuestionarios")
        .doc(cuestionarioId ?? event.params.cuestionarioId)
        .get();
      tituloCuestionario = cSnap.data()?.titulo ?? tituloCuestionario;
    } catch { }

    const deepLink = `resultados_cuestionario/${event.params.cuestionarioId}`;

    // Notificar a los dos
    await Promise.all([
      enviarNotificacion(
        usuarioId,
        "cuestionario",
        "📋 ¡Resultados listos!",
        `Ya puedes ver los resultados de "${tituloCuestionario}"`,
        deepLink
      ),
      enviarNotificacion(
        parejaId,
        "cuestionario",
        "📋 ¡Resultados listos!",
        `Ya puedes ver los resultados de "${tituloCuestionario}"`,
        deepLink
      )
    ]);
  }
);

// TRIGGER 4: Pareja respondió un cuestionario (pero aún no ambos)
// Estructura: respuestas/{cuestionarioId}/usuarios/{usuarioId}

exports.onParejaRespondio = onDocumentCreated(
  "respuestas/{cuestionarioId}/usuarios/{usuarioId}",
  async (event) => {
    const { cuestionarioId, usuarioId } = event.params;

    const parejaId = await obtenerParejaId(usuarioId);
    if (!parejaId) return;

    // Verificar si la pareja YA respondió (si sí, el trigger de resultados se encarga)
    const parejaSnap = await db
      .collection("respuestas")
      .doc(cuestionarioId)
      .collection("usuarios")
      .doc(parejaId)
      .get();

    if (parejaSnap.exists) return; // Ambos ya respondieron, no duplicar notif

    const nombre = await obtenerNombre(usuarioId);

    let tituloCuestionario = "un cuestionario";
    try {
      const cSnap = await db.collection("cuestionarios").doc(cuestionarioId).get();
      tituloCuestionario = `"${cSnap.data()?.titulo}"` ?? tituloCuestionario;
    } catch { }

    await enviarNotificacion(
      parejaId,
      "cuestionario",
      `📋 ${nombre} ya respondió`,
      `Es tu turno de responder ${tituloCuestionario}`,
      `responder_cuestionario/${cuestionarioId}`
    );
  }
);