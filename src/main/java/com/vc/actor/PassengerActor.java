package com.vc.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.vc.model.BookingRequest;
import com.vc.msg.Message;


public class PassengerActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private BookingRequest bookingRequest;
    private ActorRef bookingEngineActor;

    public PassengerActor(BookingRequest bookingRequest, ActorRef bookingEngineActor) {
        this.bookingRequest = bookingRequest;
        this.bookingEngineActor = bookingEngineActor;
    }

    public static Props props(BookingRequest bookingRequest, ActorRef bookingEngineActor) {
        return Props.create(PassengerActor.class, bookingRequest, bookingEngineActor);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        //the fact that this actor has been created implies there is a booking request.
        getSelf().tell(this.bookingRequest, getSelf());
        log.info("preStart hook: Sent the bookingRequest to self");
    }

    @Override
    public Receive createReceive() {
        return uninitialized;
    }

    //TODO: How to handle a timeout?
    private Receive initialized = receiveBuilder()
            .match(Message.BookingAccepted.class, accepted -> {
                BookingRequest updatedBookingRequest = accepted.bookingRequest();
                if (updatedBookingRequest.optionalCaptain().isPresent()) {
                    log.info("Booking accepted by {} for request: {}", updatedBookingRequest.optionalCaptain().get(), updatedBookingRequest);
                } else {
                    log.error("Received an updated booking request without Captain: {}", updatedBookingRequest);
                    throw new RuntimeException(); //TODO: What is the proper way of handling errors?
                }}
            ).build();

    private Receive uninitialized = receiveBuilder()
            .match(BookingRequest.class, request -> {
                bookingEngineActor.tell(request, getSelf());
                log.info("uninitialized receive: Sent the bookingRequest to bookingEngineActor");

                getContext().become(initialized);
                log.info("uninitialized receive: Switched receive to initialized");}
            ).build();


}
