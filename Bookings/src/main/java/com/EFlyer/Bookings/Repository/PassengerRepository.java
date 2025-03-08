package com.EFlyer.Bookings.Repository;

import com.EFlyer.Bookings.Entities.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger,Long> {
}
