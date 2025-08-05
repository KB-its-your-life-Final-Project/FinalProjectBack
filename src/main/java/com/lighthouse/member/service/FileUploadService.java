package com.lighthouse.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.lighthouse.member.util.ValidateUtil.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${PROFILE_UPLOAD_PATH}") private String uploadPath;
    @Value("${PROFILE_UPLOAD_URL}") private String uploadUrl;

    /** 프로필사진 업로드
     * @param file 업로드할 파일
     * @param memberId 회원 ID
     * @return 업로드된 파일의 URL
     */
    public String uploadProfileImg(MultipartFile file, int memberId) throws Exception {
        try {
            // 파일 생성
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = "profile_" + memberId + "_" + System.currentTimeMillis() + fileExtension;
            // 업로드 디렉토리 생성
            String profileDir = uploadPath + "/profile/";
            File uploadDir = new File(profileDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            // 파일 저장
            File destinationFile = new File(profileDir + uniqueFileName);
            file.transferTo(destinationFile);
            // 업로드된 파일 url 반환
            String fileUrl = uploadUrl + "/profile/" + uniqueFileName;
            log.info("프로필사진 업로드 완료 - 파일: {}, URL: {}", uniqueFileName, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            throw new Exception("파일 업로드에 실패했습니다.", e);
        }
    }

    /** 프로필사진 삭제
     * @param fileUrl 삭제할 파일 URL
     * @return void
     */
    public void deleteFile(String fileUrl) {
        try {
            if (isEmpty(fileUrl)) {
                return;
            }
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String filePath = uploadPath + "/profiles/" + fileName;

            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                log.info("파일 삭제 완료: {}", filePath);
            } else {
                log.error("파일 삭제 실패 또는 존재하지 않음: {}", filePath);
            }
        } catch (Exception e) {
            log.error("파일 살제 중 오류 발생: {}", fileUrl, e);
        }
    }

    /** 파일 확장자 추출
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
