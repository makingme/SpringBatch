package org.danal.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyJobConfiguration {

    @Bean
    public Job job(JobRepository jobRepository) {
        return new JobBuilder("myJob", jobRepository).start(new Step() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public void execute(StepExecution stepExecution) throws JobInterruptedException {

            }
        }).build();

    }
}
