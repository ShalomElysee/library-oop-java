package com.junia.library.service;

import com.junia.library.model.Loan;

import java.time.LocalDate;

/** Fee = days late x the daily fee of the item type. */
public class StandardLateFeePolicy implements LateFeePolicy {

    @Override
    public double calculate(Loan loan, LocalDate returnDate) {
        return loan.daysLate(returnDate) * loan.getItem().getDailyLateFee();
    }
}
