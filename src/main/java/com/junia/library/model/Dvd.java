package com.junia.library.model;

public class Dvd extends LibraryItem {

    public Dvd(String id, String title, String director) {
        super(id, title, director);
    }

    // DVDs are lent for a shorter time and cost more when late: more demand, easier to lose.
    @Override
    public int getLoanDurationDays() {
        return 7;
    }

    @Override
    public double getDailyLateFee() {
        return 1.50;
    }

    @Override
    public String getTypeLabel() {
        return "DVD";
    }
}
