package io.github.jlameira.amazonconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("storage.s3")
public class PropertiesS3 {
    private String bucket_name = "unknown";
    private String key = "unknown";
    private String private_key = "unknown";
    private String region = "unknown";

    public String getRegion() {
        return this.region;
    }
    public void setRegion(String name) {
        this.region = name;
    }

    public String getKey() {
        return this.key;
    }
    public void setKey(String name) {
        this.key = name;
    }

    public String getPrivateKey() {
        return this.private_key;
    }

    public void setPrivateKey(String name) {
        this.private_key = name;
    }

    public String getBucketName() {
        return this.bucket_name;
    }

    public void setBucketName(String name) {
        this.bucket_name = name;
    }

}
