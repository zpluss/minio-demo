package com.example.miniodemo;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhujh
 * @date 2021/5/8
 */
@Configuration
public class MinioClientConfig {

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(MinioConstants.URL)
                .credentials(MinioConstants.ACCESS_KEY, MinioConstants.SECRET_KEY)
                .build();
    }

}
