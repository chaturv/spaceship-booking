package com.vc.msg;

import com.vc.model.BookingRequest;
import org.immutables.value.Value;

public class Message {

    @Value.Immutable
    public interface RegisterCaptain {}

    @Value.Immutable
    public interface BookingBid {
        BookingRequest bookingRequest();
    }

    @Value.Immutable
    public interface BookingAccepted {
        BookingRequest bookingRequest();
    }

    @Value.Immutable
    public interface BookingAlreadyAccepted { }

}
