package ru.sovcombank.petbackendtransfers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class PetBackendTransfersApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetBackendTransfersApplication.class, args);
	}

}
