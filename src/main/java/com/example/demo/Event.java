package com.example.demo;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

public class Event {
    public String id;
    public String title;
    public String category;
    public Instant start;
    public Instant end;
    public Instant peak;
    public String visibility;
    public boolean nakedEye;
    public String description;
    public String source;

    public Event(String id, String title, String category, Instant start, Instant end, Instant peak,
                 String visibility, boolean nakedEye, String description, String source) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.start = start;
        this.end = end;
        this.peak = peak;
        this.visibility = visibility;
        this.nakedEye = nakedEye;
        this.description = description;
        this.source = source;
    }

    public ZonedDateTime getStartZoned(ZoneId zone) { return ZonedDateTime.ofInstant(start, zone); }
    public ZonedDateTime getEndZoned(ZoneId zone) { return end == null ? null : ZonedDateTime.ofInstant(end, zone); }
    public ZonedDateTime getPeakZoned(ZoneId zone) { return peak == null ? null : ZonedDateTime.ofInstant(peak, zone); }

    @Override
    public String toString() {
        return title + " (" + category + ")";
    }
}
