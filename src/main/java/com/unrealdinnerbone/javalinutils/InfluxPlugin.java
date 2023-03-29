package com.unrealdinnerbone.javalinutils;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.unrealdinnerbone.unreallib.LogHelper;
import io.javalin.Javalin;
import io.javalin.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.Objects;

public class InfluxPlugin implements Plugin {

    private static final Logger LOGGER = LogHelper.getLogger();
    private final WriteApiBlocking writeApiBlocking;
    private final InfluxConfig influxConfig;
    public InfluxPlugin(InfluxConfig influxConfig) {
        this.influxConfig = influxConfig;
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
            String doDebugHeaders = ctx.queryParam("doDebugHeaders");
            if(doDebugHeaders != null && doDebugHeaders.equalsIgnoreCase("true")) {
                StringBuilder headerBuilder = new StringBuilder("Headers: \n");
                ctx.headerMap().forEach((s, strings) -> headerBuilder.append("\t").append(s).append(": ").append(strings).append("\n"));
                LOGGER.info(headerBuilder.toString());
            }
            String ip = ctx.header("Cf-Connecting-Ip");
            if(ip == null || ip.isEmpty()) {
                ip = ctx.ip();
            }
            String country = ctx.header("Cf-Ipcountry");
            String continent = ctx.header("Cf-Continent");
            String userAgent = ctx.header("User-Agent");
            Point point = Point.measurement("requests")
                    .addField("execution_time", executionTimeMs)
                    .addTag("ip", String.valueOf(Objects.hashCode(ip)))
                    .addTag("country", country)
                    .addTag("continent", continent)
                    .addTag("user_agent", userAgent)
                    .addTag("path", ctx.path())
                    .addTag("status", String.valueOf(ctx.status().getCode()))
                    .addTag("method", ctx.method().name().toLowerCase())
                    .time(Instant.now(), WritePrecision.MS);
            if(influxConfig.isEnableAdvancedLocation()) {
                String latitude = ctx.header("Cf-Iplatitude");
                String longitude = ctx.header("Cf-Iplongitude");
                String city = ctx.header("Cf-Ipcity");
                if(latitude != null && longitude != null && city != null) {
                    point = point.addField("latitude", latitude)
                            .addField("longitude", longitude)
                            .addField("city", city);
                }
            }
            writeApiBlocking.writePoint(point);
        });
    }
}
