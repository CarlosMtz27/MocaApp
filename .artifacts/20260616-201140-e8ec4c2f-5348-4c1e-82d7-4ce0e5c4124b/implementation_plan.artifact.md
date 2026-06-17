# Implementation Plan - Event Editing Flow

This plan outlines the steps to complete the event editing functionality in MocaApp.

## Proposed Changes

### [Eventos Component]

#### [NEW] [EditarEventoScreen.kt](file:///C:/Users/ozzys/AndroidStudioProjects/App/feature/src/main/java/com/cadev/mocaapp/feature/eventos/ui/EditarEventoScreen.kt)

- Create a new screen that mirrors `CrearEventoScreen.kt` but for editing.
- Initialize with `viewModel.cargarEvento(eventoId)`.
- Use `viewModel.actualizarEvento(context)` on save.
- Add ownership check: if `evento.creadoPor != usuarioId`, show an error or unauthorized state.
- Handle navigation back to `DetalleEventoScreen` after successful update.

#### [MocaNavGraph.kt](file:///C:/Users/ozzys/AndroidStudioProjects/App/app/src/main/java/com/cadev/mocaapp/MocaNavGraph.kt)

- Register `NavRoutes.EditarEvento.route`.
- Extract `eventoId` from navigation arguments.
- Inject `EventoViewModel` using the shared factory.

---

## Verification Plan

### Manual Verification
- **Navigate to Edit**: Verify that clicking the "Edit" (pencil) icon in `DetalleEventoScreen` opens `EditarEventoScreen`.
- **Pre-fill Check**: Confirm that all fields (title, description, date, time, type, reminder) are correctly pre-filled with the event's current data.
- **Edit and Save**: Modify several fields and click "Guardar cambios". Verify that:
    - The app returns to `DetalleEventoScreen`.
    - The details shown reflect the new values.
    - (If possible via logs) The old `EventoWorker` is cancelled and a new one is scheduled.
- **Unauthorized Access**: Attempt to access the edit screen for an event created by another user (if test data allows). Verify the edit button is hidden or an error is shown.
- **Cancel Edit**: Click the back button and verify no changes were saved.
