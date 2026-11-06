package com.example.appmovimientos.data.repository
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun signInWithEmailAndPassword(email: String, pass: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await()
    }

    suspend fun createUserWithEmailAndPassword(email: String, pass: String): AuthResult {
        return auth.createUserWithEmailAndPassword(email, pass).await()
    }

    /** Envía el correo de Firebase para restablecer la contraseña. */
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun changePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")
        val email = user.email ?: throw IllegalStateException("Correo de usuario no disponible")

        // 1. Reauthenticate
        val credential = EmailAuthProvider.getCredential(email, currentPass)
        user.reauthenticate(credential).await()

        // 2. Update password
        user.updatePassword(newPass).await()
    }

    suspend fun saveUserData(uid: String, userData: Map<String, Any>) {
        database.child("users").child(uid).setValue(userData).await()
    }

    suspend fun updateUserProfile(uid: String, nombre: String, usuario: String) {
        val updates = mapOf(
            "nombre" to nombre,
            "usuario" to usuario
        )
        database.child("users").child(uid).updateChildren(updates).await()
    }

    suspend fun getUserProfile(uid: String): Map<String, String> {
        val snapshot = database.child("users").child(uid).get().await()
        val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
        val correo = snapshot.child("correo").getValue(String::class.java) ?: (auth.currentUser?.email ?: "")
        val usuario = snapshot.child("usuario").getValue(String::class.java) ?: ""
        return mapOf("nombre" to nombre, "correo" to correo, "usuario" to usuario)
    }

    suspend fun checkUserAcceptance(uid: String): Boolean {
        val snapshot = database.child("users").child(uid).child("aceptacionTerminos").get().await()
        val aceptado = snapshot.child("aceptado").getValue(Boolean::class.java) ?: false
        val versionTerminos = snapshot.child("versionTerminos").getValue(String::class.java)
        val versionPrivacidad = snapshot.child("versionPrivacidad").getValue(String::class.java)
        return aceptado && versionTerminos == "1.0" && versionPrivacidad == "1.0"
    }

    suspend fun updateTerminosAcceptance(uid: String) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val aceptacionMap = mapOf(
            "aceptado" to true,
            "versionTerminos" to "1.0",
            "versionPrivacidad" to "1.0",
            "fechaAceptacion" to fechaActual
        )
        database.child("users").child(uid).child("aceptacionTerminos").setValue(aceptacionMap).await()
    }

    suspend fun getEmailByUsername(username: String): String? {
        val snapshot = database.child("users").orderByChild("usuario").equalTo(username).get().await()
        for (child in snapshot.children) {
            val email = child.child("correo").getValue(String::class.java)
            if (email != null) return email
        }
        return null
    }

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    fun signOut() {
        auth.signOut()
    }
}
