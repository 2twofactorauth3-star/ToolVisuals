package ru.he3hauka.toolvisuals.cosmetic.sync;

import java.util.UUID;

public interface CosmeticState<T> {
    UUID server();
    UUID actor();
    long epoch();
    long revision();
    T design();
}
