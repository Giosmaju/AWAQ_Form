package com.example.backend_read.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {

    private const val HOSTNAME = "ekgcss8ww8o4ok480g08soo4.91.98.193.75.sslip.io"
    private const val IP_ADDRESS = "91.98.193.75"
    private const val BASE_URL = "https://$HOSTNAME/"

    // Custom deserializer for ZonedDateTime.
    private val zonedDateTimeAdapter = object : TypeAdapter<ZonedDateTime>() {
        private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

        override fun write(out: JsonWriter, value: ZonedDateTime?) {
            out.value(value?.format(formatter))
        }

        override fun read(input: JsonReader): ZonedDateTime? {
            return ZonedDateTime.parse(input.nextString(), formatter)
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(ZonedDateTime::class.java, zonedDateTimeAdapter)
        .create()

    // Custom DNS resolver to handle unreliable sslip.io DNS.
    private val customDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return if (hostname.equals(HOSTNAME, ignoreCase = true)) {
                listOf(InetAddress.getByName(IP_ADDRESS))
            } else {
                Dns.SYSTEM.lookup(hostname)
            }
        }
    }

    // WARNING: Insecure trust manager for development only. Trusts all certificates.
    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, SecureRandom())
    }

    private val sslSocketFactory = sslContext.socketFactory

    // Create a logging interceptor for debugging.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .dns(customDns)
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .addInterceptor(loggingInterceptor) // Add the logger here
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            // Add the Authorization token to every authenticated request.
            SessionManager.authToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofit.create(ApiService::class.java)
    }
}
