package com.junia.library.utils;

import com.junia.library.exception.DataLoadException;
import com.junia.library.model.Member;
import com.junia.library.model.MemberType;

/** Format: id,name,type (type is STUDENT or STAFF). */
public class MemberCsvParser extends AbstractCsvParser<Member> {

    @Override
    protected Member parseLine(String line) {
        String[] columns = splitColumns(line, 3);

        try {
            return new Member(columns[0], columns[1], MemberType.valueOf(columns[2].toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Covers both a blank id/name and an unknown member type.
            throw new DataLoadException("Invalid member in line: " + line, e);
        }
    }
}
