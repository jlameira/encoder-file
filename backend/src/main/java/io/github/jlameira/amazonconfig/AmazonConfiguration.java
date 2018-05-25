package io.github.jlameira.amazonconfig;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PropertiesS3.class)
public class AmazonConfiguration {

    private final PropertiesS3 propertiesS3 ;

    @Autowired
    public AmazonConfiguration(PropertiesS3 s3_properties){
        this.propertiesS3 = s3_properties;

    }

//    private static final String ACCESS_KEY="AKIAJMPPS2SNWFX5H2OQ";
//    private static final String SECRET_KEY="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";
//    private static final String REGION="us-east-2";


    @Bean
    public BasicAWSCredentials basicAWSCredentials() {
        return new BasicAWSCredentials(this.propertiesS3.getKey(), this.propertiesS3.getPrivateKey());
    }

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard().withRegion(this.propertiesS3.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials())).build();
    }
}
