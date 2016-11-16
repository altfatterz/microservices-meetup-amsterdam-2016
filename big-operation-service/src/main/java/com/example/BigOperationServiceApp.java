package com.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @author Zoltan Altfatter
 */
@SpringBootApplication
@IntegrationComponentScan
@EnableBinding(Source.class)
public class BigOperationServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(BigOperationServiceApp.class, args);
    }
}


@MessagingGateway
interface BigOperationGateway {

    @Gateway(requestChannel = Source.OUTPUT)
    void calculate(String content);

}

@RestController
@Slf4j
class BigOperationController {

    @Autowired
    BigOperationGateway bigOperationGateway;

    @PostMapping("/big-operations")
    void handleBigOperation(@RequestBody BigOperation bigOperation) throws InterruptedException {
        log.info("dispatch calculation of {} to a worker", bigOperation);
        Thread.sleep(100 + new Random().nextInt(1000));
        bigOperationGateway.calculate(bigOperation.getContent());
    }
}

@Setter
@Getter
@ToString
class BigOperation {
    String content;
}