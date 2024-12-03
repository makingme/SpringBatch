package org.danal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FileWatcherService {
    private static final int SPLIT_SIZE = 1000; // 분리 기준 행 수
    private static final String PROCESSED_DIR = "processed"; // 처리 완료 디렉토리
    private static final String COMPLETED_DIR = "completed"; // 완료 디렉토리

    public void watchDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            log.info("Watching directory: {}", directoryPath);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = path.resolve((Path) event.context());
                        log.info("New file detected: {}", filePath);
                        splitCSVFile(filePath.toString());
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            log.error("Error watching directory", e);
        }
    }

    private void splitCSVFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int fileIndex = 1;
            List<String> lines = new ArrayList<>();
            String header = reader.readLine(); // CSV 헤더 저장

            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() == SPLIT_SIZE) {
                    writeToFile(filePath, lines, fileIndex++, header);
                    lines.clear();
                }
            }
            if (!lines.isEmpty()) {
                writeToFile(filePath, lines, fileIndex++, header);
            }
            moveToProcessed(filePath); // 원본 파일 이동
        } catch (IOException e) {
            log.error("Error splitting file: {}", filePath, e);
        }
    }

    private void writeToFile(String originalFilePath, List<String> lines, int fileIndex, String header) throws IOException {
        String splitFilePath = originalFilePath.replace(".csv", "_" + fileIndex + ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(splitFilePath))) {
            writer.write(header); // 헤더 추가
            writer.newLine();
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            log.info("Created split file: {}", splitFilePath);
        }
    }

    private void moveToProcessed(String filePath) throws IOException {
        Path sourcePath = Paths.get(filePath);
        Path targetPath = sourcePath.resolveSibling(PROCESSED_DIR).resolve(sourcePath.getFileName());
        Files.createDirectories(targetPath.getParent());
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Moved original file to processed directory: {}", targetPath);
    }

    public void moveToCompleted(String filePath) throws IOException {
        Path sourcePath = Paths.get(filePath);
        Path targetPath = sourcePath.resolveSibling(COMPLETED_DIR).resolve(sourcePath.getFileName());
        Files.createDirectories(targetPath.getParent());
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Moved file to completed directory: {}", targetPath);
    }
}
