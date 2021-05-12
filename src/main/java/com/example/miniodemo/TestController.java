package com.example.miniodemo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhujh
 * @date 2021/5/8
 */
@RequiredArgsConstructor
@RestController
public class TestController {

    private final MinioUtil minioUtil;

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, @RequestParam String bucketName,String dir) {
        if (bucketName.isBlank()) {
            System.out.println("存储bucket名称为空，无法上传");
            return "存储bucket名称为空，无法上传";
        }
        if (!minioUtil.upload(file, bucketName,dir)) {
            return "文件上传异常";
        }
        return "文件上传成功";
    }

    @GetMapping("/download")
    public void download(@RequestParam String bucket, @RequestParam String objectName, HttpServletResponse response) {
        minioUtil.download(bucket, objectName, response);
    }

}
