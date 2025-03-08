package com.EFlyer.Bookings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.function.Predicate;

@SpringBootApplication
public class BookingsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingsApplication.class, args);
		System.out.println("Eflyer Service Running ... ");
	}

}
