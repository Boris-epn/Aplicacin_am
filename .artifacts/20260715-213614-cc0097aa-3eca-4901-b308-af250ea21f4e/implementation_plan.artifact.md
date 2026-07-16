# Implementation Plan - App Enhancements and Validation

This plan outlines the changes to improve data validation, prevent appointment conflicts, enhance the UI, and update the app branding.

## Proposed Changes

### [Data Layer]

#### [AppointmentDao.kt](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/java/com/example/interfaces/data/local/dao/AppointmentDao.kt)
- Add a query to count active appointments for a specific user, date, and time to prevent overlaps.

```kotlin
@Query("SELECT COUNT(*) FROM appointments WHERE user_id = :userId AND appointment_date = :appointmentDate AND appointment_time = :appointmentTime AND status = 'ACTIVA'")
fun countUserAppointmentsAt(userId: Long, appointmentDate: String, appointmentTime: String): Int
```

#### [VitusRepository.kt](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/java/com/example/interfaces/data/repository/VitusRepository.kt)
- Expose the conflict check method.

```kotlin
suspend fun hasAppointmentAt(userId: Long, date: String, time: String): Boolean {
    ensureSeedData()
    return withContext(Dispatchers.IO) {
        appointmentDao.countUserAppointmentsAt(userId, date, time) > 0
    }
}
```

---

### [UI Logic]

#### [RegisterActivity.kt](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/java/com/example/interfaces/RegisterActivity.kt)
- Implement robust validation for the registration form:
    - Email format check.
    - Full name check (at least two names).
    - ID (Cédula) length and numeric check.
    - Phone number length and numeric check.

#### [SlotSelectionActivity.kt](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/java/com/example/interfaces/SlotSelectionActivity.kt)
- In `createAppointment()`, add a check for user conflicts using the new repository method.
- Add an `AlertDialog` to confirm the appointment before proceeding.

#### [HomeActivity.kt](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/java/com/example/interfaces/HomeActivity.kt)
- Remove handling of the "Perfil" menu item which is being removed.

---

### [Resources and Configuration]

#### [activity_home.xml](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/res/layout/activity_home.xml)
- Adjust `ImageButton` sizes in the "Especialidades" section to be uniform and centered.

#### [menu_main.xml](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/res/menu/menu_main.xml)
- Remove the "PERFIL" item.
- Rename "Cita" to "Agendar Cita".

#### [AndroidManifest.xml](file:///D:/DANIEL/EPN/SEPTIMO%20SEMESTRE/AM/PROYECTO/Aplicacin_am/app/src/main/AndroidManifest.xml)
- Update `android:icon` and `android:roundIcon` to use `@drawable/logo`.

## Verification Plan

### Automated Tests
- I will perform a `gradle build` to ensure no syntax errors were introduced.

### Manual Verification
1.  **Registration Validation**:
    - Try to register with invalid email, short ID, or single name. Verify Toast messages appear.
2.  **Appointment Conflict**:
    - Book an appointment at a specific time.
    - Try to book another appointment at the same time (even with a different doctor). Verify the conflict message appears.
3.  **Confirmation Alert**:
    - Select a slot and click confirm. Verify the "Are you sure?" dialog appears.
4.  **UI Checks**:
    - Open the Home screen and check that the icons in the specialties section are uniform.
    - Open the overflow menu and verify "Agendar Cita" is present and "Perfil" is gone.
    - Check the app icon in the launcher (if possible via screenshot or just by build verification).
