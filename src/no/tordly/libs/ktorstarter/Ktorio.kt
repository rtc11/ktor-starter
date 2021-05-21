package no.tordly.libs.ktorstarter

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.core.instrument.Clock.SYSTEM
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig.DEFAULT
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

private var collector = CollectorRegistry.defaultRegistry

fun Application.postgres(operations: (HikariDataSource) -> Unit) {
    val env = environment.config
    val dbConfig = PostgresConfig.create(env)
    val datasource = Postgres.init(dbConfig)
    datasource.connect()
    operations(datasource)
}

fun Application.httpClient(clientWrapper: (HttpClient) -> Unit) {
    val httpClient = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    clientWrapper(httpClient)
}

fun Application.jacksonContent() {
    install(ContentNegotiation) {
        jackson {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerModule(JavaTimeModule())
        }
    }
}

fun Application.healthProbe(endpoint: String = "actuator/health") {
    install(Routing)
    routing {
        get(endpoint) {
            call.respondText("healthy!")
        }
    }
}

fun Application.micrometer() {
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(DEFAULT, collector, SYSTEM)
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            LogbackMetrics(),
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    routing {
        get("/actuator/metrics") {
            val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                TextFormat.write004(this, collector.filteredMetricFamilySamples(names))
            }
        }
    }
}
