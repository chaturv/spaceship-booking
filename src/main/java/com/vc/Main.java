package com.vc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.vc.actor.BookingEngineActor;
import com.vc.actor.CaptainActor;
import com.vc.actor.PassengerActor;
import com.vc.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.vc.util.Utils.captainIdToActorId;
import static com.vc.util.Utils.passengerIdToActorId;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem = ActorSystem.create("main");
        //account
        ImmutableAccount account = ImmutableAccount.builder()
                .currentBalance(BigDecimal.valueOf(100L))
                .lastUpdatedAt(LocalDateTime.now()).build();
        Passenger passenger = ImmutablePassenger.builder()
                .id("1")
                .account(account)
                .build();
        BookingRequest bookingRequest = ImmutableBookingRequest.builder()
                .id("booking-req-1")
                .passenger(passenger)
                .bookingStatus(BookingStatus.OPEN)
                .pickup(ImmutableLocation.builder().x(BigDecimal.valueOf(1)).y(BigDecimal.valueOf(1)).z(BigDecimal.valueOf(1)).build())
                .destination(ImmutableLocation.builder().x(BigDecimal.valueOf(1)).y(BigDecimal.valueOf(1)).z(BigDecimal.valueOf(1)).build())
                .build();
        Captain captain = ImmutableCaptain.builder()
                .id("1")
                .account(account)
                .build();
        Captain captain2 = ImmutableCaptain.builder()
                .id("2")
                .account(account)
                .build();
        Location location = ImmutableLocation.builder().x(BigDecimal.valueOf(1)).y(BigDecimal.valueOf(1)).z(BigDecimal.valueOf(1)).build();


        ActorRef bookingEngineActor = actorSystem.actorOf(BookingEngineActor.props(), BookingEngineActor.class.getSimpleName());
        log.info("Created actor: {}", bookingEngineActor);

        ActorRef captainActor = actorSystem.actorOf(CaptainActor.props(bookingEngineActor, captain, location), captainIdToActorId.apply(captain.id()));
        log.info("Created actor: {}", captainActor);
        ActorRef captainActor2 = actorSystem.actorOf(CaptainActor.props(bookingEngineActor, captain2, location), captainIdToActorId.apply(captain2.id()));
        log.info("Created actor: {}", captainActor2);

        ActorRef passengerActor = actorSystem.actorOf(PassengerActor.props(bookingRequest, bookingEngineActor), passengerIdToActorId.apply(passenger.id()));
        log.info("Created actor: {}", passengerActor);

        Thread.sleep(5 * 1000);
        actorSystem.terminate();
    }
}