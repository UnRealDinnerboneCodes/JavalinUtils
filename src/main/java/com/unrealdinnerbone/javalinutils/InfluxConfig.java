package com.unrealdinnerbone.javalinutils;

import com.unrealdinnerbone.config.ConfigCreator;
import com.unrealdinnerbone.config.config.BooleanConfig;
import com.unrealdinnerbone.config.config.StringConfig;

public class InfluxConfig {

    private final StringConfig url;
    private final StringConfig token;
    private final StringConfig org;
    private final StringConfig bucket;

    private final BooleanConfig enableAdvancedLocation;

    public InfluxConfig(ConfigCreator creator) {
        this.url = creator.createString("url", "");
        this.token = creator.createString("token", "");
        this.org = creator.createString("org", "");
        this.bucket = creator.createString("bucket", "");
        this.enableAdvancedLocation = creator.createBoolean("enableAdvancedLocation", false);
    }

    public String getUrl() {
        return url.getValue();
    }

    public String getToken() {
        return token.getValue();
    }

    public String getOrg() {
        return org.getValue();
    }

    public String getBucket() {
        return bucket.getValue();
    }

    public boolean isEnableAdvancedLocation() {
        return enableAdvancedLocation.getValue();
    }
}
