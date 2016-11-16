package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.util.Random;

/**
 * @author Zoltan Altfatter
 */
@SpringBootApplication
public class WorkerServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(WorkerServiceApp.class, args);
    }
}

@Slf4j
@EnableBinding(Sink.class)
class Processor {

    @StreamListener(value = Sink.INPUT)
    public void onBigOperation(String operation) throws InterruptedException {
        Thread.sleep(100 + new Random().nextInt(1000));
        log.info("processed operation: {}", operation);
    }

}
