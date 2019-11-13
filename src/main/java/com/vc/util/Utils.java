package com.vc.util;

import java.util.function.Function;

public class Utils {

    public static Function<String, String> passengerIdToActorId = id -> "passenger-" + id;
    public static Function<String, String> captainIdToActorId = id -> "captain-" + id;

}
