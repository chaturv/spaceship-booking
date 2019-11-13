package com.vc.model;

import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
public interface Location {
    BigDecimal x();
    BigDecimal y();
    BigDecimal z();
}
