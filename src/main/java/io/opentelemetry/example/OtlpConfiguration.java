package io.opentelemetry.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * All SDK management takes place here, away from the instrumentation code,
 * which should only access the OpenTelemetry APIs.
 */
public class OtlpConfiguration {

    private static final String OTEL_SERVICE_NAME = "otel-otlp-example";

    /**
     * Adds a BatchSpanProcessor initialized with OtlpGrpcSpanExporter to the
     * TracerSdkProvider.
     *
     * @return a ready-to-use {@link OpenTelemetry} instance.
     */
    static OpenTelemetry initOpenTelemetry() {
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .setTimeout(2, TimeUnit.SECONDS)
                .build();

        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
                .setScheduleDelay(100, TimeUnit.MILLISECONDS).build();

        Resource serviceNameResource = Resource
                .create(Attributes.of(ResourceAttributes.SERVICE_NAME, OTEL_SERVICE_NAME));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(spanProcessor)
                .setResource(Resource.getDefault().merge(serviceNameResource)).build();
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::shutdown));

        return openTelemetrySdk;
    }

    /**
     * Initializes a Metrics SDK with a OtlpGrpcMetricExporter and an
     * IntervalMetricReader.
     *
     * @return a ready-to-use {@link MeterProvider} instance
     */
    static MeterProvider initOpenTelemetryMetrics() {
        // set up the metric exporter and wire it into the SDK and a timed reader.
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.getDefault();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();
        IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder().setMetricExporter(metricExporter)
                .setMetricProducers(Collections.singleton(meterProvider)).setExportIntervalMillis(1000).build();

        Runtime.getRuntime().addShutdownHook(new Thread(intervalMetricReader::shutdown));

        return meterProvider;
    }
}