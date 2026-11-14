package com.junia.library.model;

public class Book extends LibraryItem {

    public Book(String id, String title, String author) {
        super(id, title, author);
    }

    @Override
    public int getLoanDurationDays() {
        return 21;
    }

    @Override
    public double getDailyLateFee() {
        return 0.50;
    }

    @Override
    public String getTypeLabel() {
        return "Book";
    }
}
