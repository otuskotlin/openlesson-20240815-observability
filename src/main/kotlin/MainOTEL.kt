package ru.otus.otuskotlin.marketplace.app.tmp

import com.otus.otuskotlin.marketplace.api.v1.models.MkplLogModel
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock


suspend fun main() {
    var counter = 0
//    val openTelemetry = openTelemetryAuto()
    val openTelemetry = openTelemetryManual()
    while (true) {
        val tracer = openTelemetry.getTracer("MainOTL", "0.1.0")
        counter++
        val span: Span = tracer.spanBuilder("test-span").startSpan()
        try {
            span.makeCurrent()
            if(counter % 3 == 0) {
                throw RuntimeException("test-exception")
            }
            span.setAttribute("log-att", "attribute-value")
            span.addEvent(
                "log",
                Attributes.of(AttributeKey.longKey("messageTime"), Clock.System.now().toEpochMilliseconds())
            )
            span.addEvent(
                "log",
                Attributes.of(AttributeKey.stringKey("operation"), MkplLogModel.Operation.INIT.value)
            )
        } catch (e: Throwable) {
            span.recordException(e)
        } finally {
            span.end()
        }
        delay(500)
    }
}

@Suppress("unused")
fun openTelemetryAuto(): OpenTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk

fun openTelemetryManual(): OpenTelemetry {
    val resource = Resource.getDefault().toBuilder()
            .put(SERVICE_NAME, "my-server")
            .put(ServiceAttributes.SERVICE_VERSION, "0.1.0")
            .build()

    val sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(
                BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build()
            )
            .setResource(resource)
            .build()

    val sdkMeterProvider = SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build()
            )
            .setResource(resource)
            .build()

    val sdkLoggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(OtlpGrpcLogRecordExporter.builder().build())
                    .build()
            )
            .setResource(resource)
            .build()

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setMeterProvider(sdkMeterProvider)
            .setLoggerProvider(sdkLoggerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal()

    return openTelemetry
}
