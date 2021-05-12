package com.example.miniodemo;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhujh
 * @date 2021/5/8
 */
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     */
    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件上传
     */
    public Boolean upload(MultipartFile file, String bucketName, String dir) {
        //是否有上级目录
        String path = dir.isBlank() ? file.getOriginalFilename() : dir + "/" + file.getOriginalFilename();

        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(path)
                    .stream(file.getInputStream(), -1, 10485760).contentType(file.getContentType()).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件下载
     */
    public void download(String bucketName, String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                .object(fileName).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            res.setCharacterEncoding("utf-8");
            res.setContentType("application/octet-stream");
            res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            ServletOutputStream stream = res.getOutputStream();
            IOUtils.copy(response, stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 列出所有的桶
     */
    public List<String> listBuckets() throws Exception {
        List<Bucket> list = minioClient.listBuckets();
        List<String> names = new ArrayList<>();
        list.forEach(b -> {
            names.add(b.name());
        });
        return names;
    }

    /**
     * 列出一个桶中的所有文件和目录
     */
    public List<Map<String, Object>> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build());
        List<Map<String, Object>> objectItems = new ArrayList<>();
        results.forEach(r -> {
            Map<String, Object> objectItem = new HashMap<>();
            try {
                Item item = r.get();
                objectItem.put("name", item.objectName());
                objectItem.put("size", item.size());
                objectItem.put("directory", item.isDir());
                objectItems.add(objectItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return objectItems;
    }

    /**
     * 删除存储bucket
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除一个对象
     */
    public void removeObject(String bucket, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
    }

    /**
     * 批量删除文件对象
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).collect(Collectors.toList());
        return minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
    }
}
