package com.jeeps.gamecollector.remaster.data.api.interceptors
import com.google.firebase.auth.FirebaseAuth
import com.jeeps.gamecollector.remaster.utils.extensions.bearer
import com.jeeps.gamecollector.remaster.utils.extensions.getToken
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class FirebaseTokenInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            firebaseAuth.currentUser?.getToken().orEmpty()
        }

        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .method(original.method, original.body)

        if (token.isNotEmpty()) {
            requestBuilder.header("Authorization", token.bearer())
        }

        return chain.proceed(requestBuilder.build())
    }
}
