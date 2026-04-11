package net.forgecraft.mods.bridge.structs;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public record TimestampedData<T>(
    int timestamp,
    T data
) {
    public String asReadableDate() {
        var instant = Instant.ofEpochSecond(this.timestamp);

        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
