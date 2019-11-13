package com.vc.model;

import org.immutables.value.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value.Immutable
public interface Account {
    BigDecimal currentBalance();
    LocalDateTime lastUpdatedAt();
}
