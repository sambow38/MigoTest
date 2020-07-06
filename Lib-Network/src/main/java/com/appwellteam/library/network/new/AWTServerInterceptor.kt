package com.appwellteam.library.network.new

import android.os.Build
import android.text.TextUtils
import com.appwellteam.library.common.AWTAppInfo
import com.appwellteam.library.common.AWTCommon
import com.appwellteam.library.common.AWTLog
import com.appwellteam.library.network.new.util.AWTWebCommon
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("unused")
class AWTServerInterceptor(private val urlDomain: String) : Interceptor {

    private val userAgent: String = String.format(Locale.US, "%s (%s/%s at %s; AndroidApiLevel/%d/%s)",
            "okHttp3", AWTAppInfo.packageName, AWTAppInfo.versionName, Build.MODEL, Build.VERSION.SDK_INT, Build.VERSION.RELEASE)
//    private val packageName: String
//    private val versionName: String

    init {
//        this.versionName = AWTAppInfo.versionName
//        this.packageName = AWTAppInfo.packageName
        // Keep "AndroidApiLevel" string for Data team search, ask data team before remove it
        // okHttp3 (packageName/1.0.0 at Nexus One; AndroidApiLevel/19/4.4)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //        >"< 檢察網路
        if (!AWTWebCommon.isNetworkConnect) {
            AWTCommon.showToast("請檢察網路狀況！")
            throw RuntimeException(chain.request().url().toString())
            //            throw new NoConnectionException(chain.request().url().toString());
        }

        val origin = chain.request()
        val originHeaders = origin.headers()

        val originUserAgent = originHeaders.get(headerUserAgent)
        val existUserAgent = !TextUtils.isEmpty(originUserAgent)
//        val needAcceptLanguage = originHeaders.get(headerLanguage) == null
//        val needAuth = originHeaders.get(HEADER_AUTHORIZATION) == null
        val needContentType = originHeaders.get(headerContentType) == null
        val needAccept = originHeaders.get(headerAccept) == null
        //        boolean needPlatform = originHeaders.get(HEADER_X_PLATFORM) == null;
        //        boolean needAppVersion = originHeaders.get(HEADER_X_APP_VERSION) == null;

        val is3rd = !origin.url().toString().contains(urlDomain)

        val builder = origin.newBuilder()
        if (is3rd) {
            val request = builder.build()
            return chain.proceed(request)
        } else {
            // Driver server
            val finalAgent = if (existUserAgent) {
                ", $userAgent"
            } else {
                userAgent
            }
            builder.addHeader(headerUserAgent, finalAgent)


            //            if (needPlatform) {
            //                builder.addHeader(HEADER_X_PLATFORM, "android");
            //            }
            //>"< temp
            //            if (needAcceptLanguage) {
            //                builder.addHeader(headerLanguage, AppEnv.getAppLanguage().getCode());
            //            }
            //            if (needAuth && !TextUtils.isHeader(AccountManager.getInstance().getJwtToken())) {
            //                builder.addHeader(HEADER_AUTHORIZATION, AccountManager.getInstance().getJwtToken());
            //            }

            if (needContentType) {
                builder.addHeader(headerContentType, "application/json")
            }
            if (needAccept) {
                builder.addHeader(headerAccept, "application/json")
            }
            //            if (needAppVersion) {
            //                builder.addHeader(HEADER_X_APP_VERSION, versionName);
            //            }

            val request = builder.build()
            val target = request.method() + " " + request.url()
            var payloadSize = -1L
            val startNs = System.nanoTime()
            val response: Response
            try {
                response = chain.proceed(request)
            } catch (ex: Exception) {
                throw ex
            }

            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (HttpHeaders.hasBody(response)) {
                    val source = responseBody!!.source()
                    source.request(Long.MAX_VALUE) // Buffer the entire body.
                    val buffer = source.buffer()
                    payloadSize = buffer.size()
                }
            }

//            val finalTookMs = tookMs
//            val finalPayloadSize = payloadSize
            AWTLog.d(TAG,"target [", target, "] took [", tookMs.toString(), " ms] with payload [", payloadSize.toString(), "]")

            return response
        }
    }

    companion object {
        private const val TAG = "RequestManager-AWTServerInterceptor"
        const val HEADER_AUTHORIZATION = "Authorization"
        private const val headerLanguage = "Accept-Language"
        private const val headerUserAgent = "User-Agent"
        private const val headerContentType = "Content-Type"
        private const val headerAccept = "Accept"
    }
}