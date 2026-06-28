package com.bankapp.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Online Banking System.
 *
 * Run with: mvn spring-boot:run
 * Or build a jar: mvn clean package && java -jar target/banking-1.0.0.jar
 */
@SpringBootApplication
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}
