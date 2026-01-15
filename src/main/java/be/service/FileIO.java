package be.service;


import be.domain.exception.*;
import be.domain.exception.FileNotFoundException;
import be.util.*;
import jakarta.annotation.*;
import java.io.*;
import java.nio.file.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.*;

@Slf4j
@Component
public class FileIO {

    private final String baseDir;
    private final FileUtil files;
    private Path baseDirPath;

    public FileIO(
            @Value("${common-secret.file.main-directory}")
            String baseDir,
            FileUtil files
    ) {
        this.baseDir = baseDir;
        this.files = files;
    }

    @PostConstruct
    private void init() {
        try {
            baseDirPath = Paths.get(baseDir);
            if (!files.exists(baseDirPath)) {
                files.createDirectories(baseDirPath);
            }
        } catch (Exception e) {
            log.warn("Failed to create storage directory", e);
            throw new RuntimeException(e);
        }
    }

    public byte[] getFileData(String fileName) {
        Path target = this.getExistingPath(fileName);

        try {
            return files.readAllBytes(target);
        } catch (IOException e) {
            log.warn("Failed to read file data", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteFileData(String fileName) {
        Path target = this.getExistingPath(fileName);

        try {
            files.delete(target);
        } catch (IOException e) {
            log.warn("Failed to delete file", e);
            throw new RuntimeException(e);
        }
    }

    public void transferMultipartFile(MultipartFile multipartFile, String newFileName) {
        Path target = baseDirPath.resolve(newFileName);

        if (files.exists(target)) {
            String errMsg = String.format(
                    "File %s already exists in directory %s",
                    newFileName, baseDir
            );

            FileAlreadyExistException ex = new FileAlreadyExistException(errMsg);
            log.warn(errMsg, ex);
            throw ex;
        }

        if (multipartFile.isEmpty()) {
            log.warn("Multipart file is empty.");
        }

        try {
            multipartFile.transferTo(target);
        } catch (IOException e) {
            log.warn("Failed to transfer multipart file", e);
            throw new RuntimeException(e);
        }
    }

    private Path getExistingPath(String fileName) {
        Path target = baseDirPath.resolve(fileName);

        if (!files.exists(target)) {
            String errMsg = String.format(
                    "File %s does not exist in directory %s",
                    fileName, baseDir
            );

            FileNotFoundException ex = new FileNotFoundException();
            log.warn(errMsg, ex);
            throw ex;
        }

        return target;
    }
}


