package org.danal.listener;

import org.danal.service.FileWatcherService;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;


public class FileProcessingListener implements ChunkListener {
    private final FileWatcherService fileWatcherService;

    public FileProcessingListener(FileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    @Override
    public void afterChunk(ChunkContext context) {
        try {
            String processedFilePath = context.getStepContext().getJobExecutionContext().get("processedFilePath").toString();
            fileWatcherService.moveToCompleted(processedFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
