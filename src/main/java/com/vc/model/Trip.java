package com.vc.model;

import org.immutables.value.Value;

@Value.Immutable
public interface Trip {
    BookingRequest bookingRequest();
    Captain captain();
    TripStatus tripStatus();
}