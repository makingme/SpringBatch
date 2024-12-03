package org.danal;

import org.danal.config.BatchConfiguration;
import org.danal.service.FileWatcherService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);
    }
}
