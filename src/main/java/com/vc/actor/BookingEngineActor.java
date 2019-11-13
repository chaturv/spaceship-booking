package com.vc.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.vc.model.BookingRequest;
import com.vc.msg.ImmutableBookingAccepted;
import com.vc.msg.ImmutableBookingAlreadyAccepted;
import com.vc.msg.Message;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BookingEngineActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final Map<String, ActorRef> availableCaptains = new ConcurrentHashMap<>();
    private final Map<String, ActorRef> busyCaptains = new ConcurrentHashMap<>();

    private final LinkedHashMap<String, Optional> acceptedBookingIds = new LinkedHashMap<>(1024, 0.75f, true);
    private final Map<String, ActorRef> openBookings = new ConcurrentHashMap<>();

    public static Props props() {
        return Props.create(BookingEngineActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.RegisterCaptain.class, registerCaptain -> {
                    this.availableCaptains.put(getSender().path().name(), getSender());
                    log.info("Registered captain actor: {}", getSender());
                })
                .match(BookingRequest.class, bookingRequest -> {
                    log.info("Received a booking request: {}", bookingRequest);
                    openBookings.put(bookingRequest.id(), getSender());

                    Collection<ActorRef> nearestCaptains = nearestCaptains(availableCaptains, bookingRequest);
                    nearestCaptains.forEach(captain -> captain.tell(bookingRequest, getSelf()));
                })
                .match(Message.BookingBid.class, bookingBid -> {
                    log.info("Received a booking bid for request: {}", bookingBid.bookingRequest());
                    String bookingId = bookingBid.bookingRequest().id();

                    //first acceptance of the booking
                    if (!acceptedBookingIds.containsKey(bookingId)) {
                        //generate accepted message
                        Message.BookingAccepted accepted = ImmutableBookingAccepted.builder()
                                .bookingRequest(bookingBid.bookingRequest())
                                .build();

                        log.info("Matched booking request: {}", bookingBid.bookingRequest());
                        //tell the passenger
                        ActorRef passengerActor = openBookings.get(bookingId);
                        passengerActor.tell(accepted, getSelf());
                        //tell the captain
                        getSender().tell(accepted, getSelf());

                        //move from available to busy
                        String captainActorName = getSender().path().name();
                        availableCaptains.remove(captainActorName);
                        busyCaptains.put(captainActorName, getSender());
                        //mark as accepted booking
                        acceptedBookingIds.put(bookingId, Optional.empty());
                        //remove from open bookings
                        openBookings.remove(bookingId);

                    } else {
                        //stale
                        log.info("Already matched booking: {}", bookingBid.bookingRequest());
                        Message.BookingAlreadyAccepted alreadyAccepted = ImmutableBookingAlreadyAccepted.builder().build();
                        getSender().tell(alreadyAccepted, getSelf());
                    }
                }).build();
    }

    private Collection<ActorRef> nearestCaptains(Map<String, ActorRef> availableCaptains,
                                                 BookingRequest bookingRequest)  {
        //TODO: Implement based on location proximity
        return availableCaptains.values();
    }
}
