package com.junia.library.model;

public class Member {

    private final String id;
    private final String name;
    private final MemberType type;

    public Member(String id, String name, MemberType type) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Member id must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Member name must not be blank.");
        }
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MemberType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " (" + id + ", " + type + ")";
    }
}
