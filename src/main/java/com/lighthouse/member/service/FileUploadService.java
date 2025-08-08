package com.lighthouse.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.lighthouse.member.util.ValidateUtil.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${FILE_UPLOAD_DIR}") private String relativeUploadDir;

    /**
     * 프로필사진 업로드
     * @param file 업로드할 파일
     * @param memberId 회원 ID
     * @return 업로드된 파일의 URL
     */
    public String uploadProfileImg(MultipartFile file, int memberId) throws Exception {
        String userHome = System.getProperty("user.home");
        Path profileDir = Paths.get(userHome, relativeUploadDir, "profile");
        log.info("FileUploadService.uploadProfileImg() 실행 ========");
        log.info("relativeUploadDir: {}", relativeUploadDir);
        log.info("uploadDir: {}", profileDir.toString());
        try {
            // 파일명 구성
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = "profile_" + memberId + "_" + System.currentTimeMillis() + fileExtension;
            log.info("originalFileName: {}", originalFileName);
            log.info("fileExtension: {}", fileExtension);
            log.info("uniqueFileName: {}", uniqueFileName);

            // 경로 없을 경우 생성
            try {
                Files.createDirectories(profileDir);
                log.info("폴더 생성됨: {}", profileDir.toAbsolutePath());
            } catch (IOException e) {
                log.error("폴더 생성 실패: {}", profileDir.toAbsolutePath(), e);
                throw new RuntimeException("업로드 폴더 생성 실패", e);
            }
            // 파일 저장
            Path destinationPath = profileDir.resolve(uniqueFileName);
            file.transferTo(destinationPath.toFile());
            // 회원에게 반환할 URL 경로 생성
//            String fileUrl = profileDir.resolve(uniqueFileName).toString().replace("\\", "/");
            String fileUrl = "/" + relativeUploadDir + "/profile/" + uniqueFileName;
            log.info("프로필사진 업로드 완료 - 파일: {}, URL: {}", uniqueFileName, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            throw new Exception("파일 업로드에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("파일 업로드 중 예외 발생", e);
            throw new Exception("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 프로필사진 삭제
     * @param fileUrl 삭제할 파일 URL
     * @return void
     */
    public void deleteFile(String fileUrl) {
        try {
            if (isEmpty(fileUrl)) {
                return;
            }
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String userHome = System.getProperty("user.home");
            Path filePath = Paths.get(userHome, relativeUploadDir, "profile", fileName);
            File file = filePath.toFile();

            if (file.exists() && file.delete()) {
                log.info("파일 삭제 완료: {}", filePath);
            } else {
                log.error("파일 삭제 실패 또는 존재하지 않음: {}", filePath);
            }
        } catch (Exception e) {
            log.error("파일 살제 중 오류 발생: {}", fileUrl, e);
        }
    }

    /**
     * 파일 확장자 추출
     * @param fileName 파일명
     * @return 파일 확장자
     */
    public String getFileExtension(String fileName) {
        if (isEmpty(fileName)) {
            return ".jpg"; // 기본 확장자
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".jpg"; // 기본 확장자
        }
        return fileName.substring(lastDotIndex);
    }
}
