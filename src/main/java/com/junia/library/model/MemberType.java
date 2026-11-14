package com.junia.library.model;

public enum MemberType {
    STUDENT(3),
    STAFF(5);

    private final int maxActiveLoans;

    MemberType(int maxActiveLoans) {
        this.maxActiveLoans = maxActiveLoans;
    }

    public int getMaxActiveLoans() {
        return maxActiveLoans;
    }
}
