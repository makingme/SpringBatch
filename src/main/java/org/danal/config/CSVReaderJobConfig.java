package org.danal.config;

import org.danal.listener.FileProcessingListener;
import org.danal.model.FoodStore;
import org.danal.service.FileWatcherService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;

@Configuration
public class CSVReaderJobConfig extends DefaultBatchConfiguration {
    @Bean
    public FlatFileItemReader<FoodStore> csvItemReader() {
        FlatFileItemReader<FoodStore> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("path_to_split_csv_directory"));
        reader.setLinesToSkip(1); // Skip header
        reader.setLineMapper(new DefaultLineMapper<FoodStore>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("seq", "name", "businessNumber", "phoneNumber");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<FoodStore>() {{
                setTargetType(FoodStore.class);
            }});
        }});
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<FoodStore> csvItemWriter(DataSource dataSource) {
        JdbcBatchItemWriter<FoodStore> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("INSERT INTO business_table (seq, name, business_number, phone_number) VALUES (:seq, :name, :businessNumber, :phoneNumber)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }


    @Bean
    public Job csvFileToDatabaseJob(JobRepository jobRepository, Step csvFileToDatabaseStep) {
        return new JobBuilder("csvFileToDatabaseJob", jobRepository)
                .start(csvFileToDatabaseStep)
                .build();
    }

    @Bean
    public Step csvFileToDatabaseStep(JobRepository jobRepository, FileWatcherService fileWatcherService, DataSource dataSource) {
        return new StepBuilder("csvFileToDatabaseStep", jobRepository)
                .<FoodStore, FoodStore>chunk(100, null)
                .reader(csvItemReader())
                .writer(csvItemWriter(dataSource))
                .listener(new FileProcessingListener(fileWatcherService))  // 리스너 추가
                .build();
    }

}
