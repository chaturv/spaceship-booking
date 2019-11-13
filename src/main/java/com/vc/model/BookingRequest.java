package com.vc.model;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface BookingRequest {
    String id();
    Passenger passenger();
    Optional<Captain> optionalCaptain();
    Location pickup();
    Location destination();
    BookingStatus bookingStatus();
}
