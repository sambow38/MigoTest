package com.appwellteam.library.network.new

import android.os.Build
import com.appwellteam.library.common.AWTEventBus
import com.appwellteam.library.network.BuildConfig
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
@Suppress("unused")
open class AWTRequestManager : AWTEventBus() {
    private var nextTaskID = 0
    private var taskIDs: MutableMap<String, Int> = ConcurrentHashMap()

    fun updateApiTaskID(api: String, replace: Boolean): Int {
        synchronized(this) {
            ++nextTaskID
            if (nextTaskID >= java.lang.Short.MAX_VALUE) {
                nextTaskID = 1
            }

            if (!replace) {
                val integer = taskIDs[api]
                if (integer != null) {
                    return -integer
                }
            }
        }
        taskIDs[api] = nextTaskID
        return nextTaskID
    }

    fun containsApiTaskID(api: String, taskID: Int): Boolean {
        var ret = false
        val integer = taskIDs[api]
        if (integer != null) {
            ret = integer == taskID
        }
        return ret
    }

    fun clearApiTaskID(api: String?) {
        if (api == null) {
            return
        }
        taskIDs.remove(api)
    }

    @Suppress("ProtectedInFinal")
    protected fun checkQuery(query: String?): String? {
        return if (query?.length == 0) {
            null
        } else query
    }

    companion object {
        private var okHttpClientInstance: OkHttpClient? = null

        @JvmOverloads
        fun getRetrofit(url: String, gson: Gson? = null): Retrofit {
            val factory: Converter.Factory = if (gson == null) {
                GsonConverterFactory.create()
            } else {
                GsonConverterFactory.create(gson)
            }
            return Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(factory)
                    .client(getNewOkHttpClient(url))
                    .build()
        }

        @Suppress("ProtectedInFinal")
        protected fun getNewOkHttpClient(url: String): OkHttpClient {
            if (okHttpClientInstance == null) {
                val builder = OkHttpClient.Builder()
                builder.connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)

                builder.addInterceptor(AWTServerInterceptor(url))
                //            builder.addInterceptor(new RefreshTokenInterceptor());

                if (BuildConfig.DEBUG) {
                    builder.addNetworkInterceptor(StethoInterceptor())
                            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                }

                val specBuilder = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    specBuilder.tlsVersions(TlsVersion.TLS_1_2)
                            .cipherSuites(
                                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                                    //
                                    CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                                    CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,

                                    CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
                                    CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                                    CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                                    CipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,
                                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                                    CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
                                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA)
                } else {
                    // It should Larger than Build.VERSION_CODES.HONEYCOMB (SDK 11)
                    specBuilder.tlsVersions(TlsVersion.TLS_1_0)
                            .cipherSuites(
                                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA)
                }
                builder.connectionSpecs(Arrays.asList(specBuilder.build(), ConnectionSpec.CLEARTEXT))
                //            builder.sslSocketFactory()
                okHttpClientInstance = builder.build()
            }
            return okHttpClientInstance!!
        }
    }
}//    public static Retrofit getRetrofit() {
//        return getRetrofit(AWTWebCommon.urlDomain);
//    }