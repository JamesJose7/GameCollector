package com.jeeps.gamecollector.remaster.utils.extensions

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun FirebaseUser.getToken(forceRefresh: Boolean = true): String {
    return suspendCancellableCoroutine { continuation ->
        getIdToken(forceRefresh)
            .addOnCompleteListener { task ->
                val token = if (task.isSuccessful) {
                    task.result.token ?: ""
                } else {
                    ""
                }
                continuation.resume(token)
            }
            .addOnFailureListener {
                continuation.resume("")
            }
    }
}
