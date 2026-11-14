package com.junia.library.repository;

import com.junia.library.model.LibraryItem;
import com.junia.library.model.Loan;
import com.junia.library.model.Member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory storage. Services never touch the collections directly. */
public class LibraryRepository {

    private final Map<String, LibraryItem> items = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    public void addItem(LibraryItem item) {
        if (items.containsKey(item.getId())) {
            throw new IllegalArgumentException("Duplicate item id: " + item.getId());
        }
        items.put(item.getId(), item);
    }

    public void addMember(Member member) {
        if (members.containsKey(member.getId())) {
            throw new IllegalArgumentException("Duplicate member id: " + member.getId());
        }
        members.put(member.getId(), member);
    }

    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    public Optional<LibraryItem> findItem(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public Optional<Member> findMember(String id) {
        return Optional.ofNullable(members.get(id));
    }

    public Optional<Loan> findActiveLoanForItem(String itemId) {
        return loans.stream()
                .filter(loan -> loan.isActive() && loan.getItem().getId().equals(itemId))
                .findFirst();
    }

    public List<Loan> findActiveLoansOf(String memberId) {
        return loans.stream()
                .filter(loan -> loan.isActive() && loan.getMember().getId().equals(memberId))
                .toList();
    }

    public Collection<LibraryItem> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public List<Loan> getAllLoans() {
        return Collections.unmodifiableList(loans);
    }
}
