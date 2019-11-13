package com.vc.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.vc.model.BookingRequest;
import com.vc.model.Captain;
import com.vc.model.ImmutableBookingRequest;
import com.vc.model.Location;
import com.vc.msg.ImmutableBookingBid;
import com.vc.msg.ImmutableRegisterCaptain;
import com.vc.msg.Message;

import java.util.Optional;

public class CaptainActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef bookingEngineActor;
    private final Captain captain;
    private Location location;

    public CaptainActor(ActorRef bookingEngineActor, Captain captain, Location location) {
        this.bookingEngineActor = bookingEngineActor;
        this.captain = captain;
        this.location = location;
    }

    public static Props props(ActorRef bookingEngineActor, Captain captain, Location location) {
        return Props.create(CaptainActor.class, bookingEngineActor, captain, location);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        //the fact that this actor has been created implies it is ready and should register itself with booking engine
        this.bookingEngineActor.tell(ImmutableRegisterCaptain.builder().build(), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BookingRequest.class, bookingRequest -> {
                    //Booking Engine has determined this actor to be one of the nearest so a bid will be made
                    ImmutableBookingRequest updatedBookingRequest = ImmutableBookingRequest.builder()
                            .from(bookingRequest)
                            .optionalCaptain(Optional.of(this.captain))
                            .build();
                    Message.BookingBid bookingBid = ImmutableBookingBid.builder()
                            .bookingRequest(updatedBookingRequest)
                            .build();

                    //send bid to Booking Engine
                    getSender().tell(bookingBid, getSelf());
                    log.info("Sent a booking bid for request {}", bookingRequest);
                })
                .match(Message.BookingAccepted.class, accepted -> {
                    //start a trip. complete is after t seconds and send completion message for fare computation
                    log.info("Accepted booking for request {}", accepted.bookingRequest());
                })
                .match(Message.BookingAlreadyAccepted.class, alreadyAccepted -> {
                    log.info("Received {} message", Message.BookingAlreadyAccepted.class.getSimpleName());
                }).build();
    }
}
