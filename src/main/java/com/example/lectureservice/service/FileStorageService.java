//package com.example.lectureservice.service;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class FileStorageService {
//
//    private final S3Client s3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucketName;
//
//    // 기존 메서드: UUID 기반 랜덤 파일명
//    public String saveFile(MultipartFile file, String folder) throws IOException {
//        String originalFilename = file.getOriginalFilename();
//        String uniqueName = UUID.randomUUID() + "_" + originalFilename;
//        return saveFile(file, folder, uniqueName);
//    }
//
//    // 새 메서드: 파일명 직접 지정
//    public String saveFile(MultipartFile file, String folder, String fileName) throws IOException {
//        String key = folder + "/" + fileName;
//        System.out.println("key :: " + key);
//
//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(file.getContentType())
//                .build();
//
//        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
//
//        return s3Client.utilities()
//                .getUrl(builder -> builder.bucket(bucketName).key(key))
//                .toExternalForm();
//    }
//}
//
