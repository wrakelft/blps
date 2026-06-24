package ru.itmo.blps1.config;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CamundaEngineConfig {

    private final DataSource dataSource;
    private final ResourcePatternResolver resourcePatternResolver;
    private final ApplicationContext applicationContext;

    @Value("${camunda.bpm.database-schema-update:true}")
    private boolean databaseSchemaUpdate;

    @Value("${camunda.bpm.deployment-resource-pattern:classpath*:/processes/*.bpmn}")
    private String deploymentResourcePattern;

    @Bean
    public ProcessEngine processEngine() {
        StandaloneProcessEngineConfiguration configuration =
                new StandaloneProcessEngineConfiguration();

        configuration.setDataSource(dataSource);

        configuration.setDatabaseSchemaUpdate(
                databaseSchemaUpdate
                        ? StandaloneProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
                        : StandaloneProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE
        );

        configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
        configuration.setJobExecutorActivate(false);

        configuration.setBeans(camundaBeans());

        ProcessEngine engine = configuration.buildProcessEngine();

        deployProcesses(engine.getRepositoryService());

        return engine;
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    private Map<Object, Object> camundaBeans() {
        Map<Object, Object> beans = new HashMap<>();
        applicationContext.getBeansOfType(JavaDelegate.class)
                .forEach(beans::put);
        return beans;
    }

    private void deployProcesses(RepositoryService repositoryService) {
        try {
            Resource[] resources = resourcePatternResolver.getResources(deploymentResourcePattern);

            if (resources.length == 0) {
                return;
            }

            var deploymentBuilder = repositoryService
                    .createDeployment()
                    .name("blps-camunda-processes");

            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream()) {
                    deploymentBuilder.addInputStream(resource.getFilename(), inputStream);
                }
            }

            deploymentBuilder.deploy();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deploy Camunda BPMN processes", e);
        }
    }
}