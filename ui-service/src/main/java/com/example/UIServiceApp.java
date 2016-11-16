package com.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

/**
 * @author Zoltan Altfatter
 */

@SpringBootApplication
@EnableConfigurationProperties(UIServiceProperties.class)
@EnableAsync
public class UIServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(UIServiceApp.class, args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Controller
@Slf4j
class TaskController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UIServiceProperties properties;

    @Autowired
    AsyncTaskService statisticsService;

    @Autowired
    OperationWithCustomTracingService customTracingService;

    @GetMapping
    public String bigOperationForm(Task task) {
        log.info("returning task form...");
        return "index";
    }

    @PostMapping("/tasks")
    public String handleTask(Task task) throws InterruptedException {
        log.info("received big operation: {}", task);
        Thread.sleep(100);
        restTemplate.postForEntity(properties.getBigOperation(), new BigOperation(task.getContent()), Void.class);

        // process task asynchronously
        statisticsService.processTask(task);

        // custom span
        customTracingService.process();

        return "result";
    }

}

@Service
@Slf4j
class AsyncTaskService {

    @Autowired
    SpanAccessor spanAccessor;

    @Async
    public void processTask(Task task) throws InterruptedException {
        Thread.sleep(1000);
        log.info("processed task {}", task);

        Span currentSpan = spanAccessor.getCurrentSpan();
        currentSpan.tag("myTag", "myTagValue");
        log.info("traceId: {}, spanId: {}", currentSpan.getTraceId(), currentSpan.getSpanId());
    }

}


@Service
@Slf4j
class OperationWithCustomTracingService {

    @Autowired
    Tracer tracer;

    public void process() throws InterruptedException {
        Span span = tracer.createSpan("myCustomSpan", new AlwaysSampler());
        int millis = new Random().nextInt(100);
        log.info(String.format("Sleeping for [%d] millis", millis));
        Thread.sleep(millis);
        tracer.addTag("random-sleep-millis", String.valueOf(millis));
        tracer.close(span);
    }
}

@Getter
@Setter
@ToString
@NoArgsConstructor
class BigOperation {
    String content;

    public BigOperation(String content) {
        this.content = content;
    }
}

@Getter
@Setter
@ToString
class Task {
    String content;
}

@ConfigurationProperties("collaborators")
@Getter
@Setter
class UIServiceProperties {

    String bigOperation;

}

