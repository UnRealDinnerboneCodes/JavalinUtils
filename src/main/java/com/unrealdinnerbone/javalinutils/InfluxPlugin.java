package com.unrealdinnerbone.javalinutils;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import io.javalin.Javalin;
import io.javalin.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class InfluxPlugin implements Plugin {

    private final WriteApiBlocking writeApiBlocking;
    public InfluxPlugin(InfluxConfig influxConfig) {
        InfluxDBClientOptions builder = InfluxDBClientOptions
                .builder()
                .url(influxConfig.getUrl())
                .authenticateToken(influxConfig.getToken().toCharArray())
                .org(influxConfig.getOrg())
                .bucket(influxConfig.getBucket())
                .build();
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(builder);
        writeApiBlocking = influxDBClient.getWriteApiBlocking();
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        javalin.cfg.requestLogger.http((ctx, executionTimeMs) -> {
            Point point = Point.measurement("requests")
                    .addField("execution_time", executionTimeMs)
                    .addTag("path", ctx.path())
                    .addTag("status", String.valueOf(ctx.status().getCode()))
                    .addTag("method", ctx.method().name().toLowerCase())
                    .time(Instant.now(), WritePrecision.MS);
            writeApiBlocking.writePoint(point);
        });
    }
}
