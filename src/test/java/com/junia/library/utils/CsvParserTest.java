package com.junia.library.utils;

import com.junia.library.exception.DataLoadException;
import com.junia.library.model.Book;
import com.junia.library.model.Dvd;
import com.junia.library.model.LibraryItem;
import com.junia.library.model.Member;
import com.junia.library.model.MemberType;
import com.junia.library.repository.LibraryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    @TempDir
    Path tempDir;

    private Path write(String name, String content) throws IOException {
        return Files.writeString(tempDir.resolve(name), content);
    }

    @Test
    void parsesBooksAndDvdsIntoTheRightSubclasses() throws IOException {
        Path file = write("items.csv", "type,id,title,creator\nBOOK,B1,Dune,Herbert\nDVD,D1,Inception,Nolan\n");

        List<LibraryItem> items = new ItemCsvParser().parse(file);

        assertEquals(2, items.size());
        assertInstanceOf(Book.class, items.get(0));
        assertInstanceOf(Dvd.class, items.get(1));
        assertEquals("Dune", items.get(0).getTitle());
    }

    @Test
    void blankLinesAreIgnored() throws IOException {
        Path file = write("items.csv", "type,id,title,creator\n\nBOOK,B1,Dune,Herbert\n\n\n");

        assertEquals(1, new ItemCsvParser().parse(file).size());
    }

    @Test
    void unknownItemTypeIsRefused() throws IOException {
        Path file = write("items.csv", "type,id,title,creator\nVINYL,V1,Thriller,MJ\n");

        assertThrows(DataLoadException.class, () -> new ItemCsvParser().parse(file));
    }

    @Test
    void lineWithMissingColumnIsRefused() throws IOException {
        Path file = write("items.csv", "type,id,title,creator\nBOOK,B1,Dune\n");

        assertThrows(DataLoadException.class, () -> new ItemCsvParser().parse(file));
    }

    @Test
    void missingFileIsReportedAsDataLoadException() {
        Path missing = tempDir.resolve("does-not-exist.csv");

        assertThrows(DataLoadException.class, () -> new ItemCsvParser().parse(missing));
    }

    @Test
    void parsesMembersWithTheirType() throws IOException {
        Path file = write("members.csv", "id,name,type\nM1,Alice,student\nM2,Bob,STAFF\n");

        List<Member> members = new MemberCsvParser().parse(file);

        assertEquals(MemberType.STUDENT, members.get(0).getType());
        assertEquals(MemberType.STAFF, members.get(1).getType());
    }

    @Test
    void unknownMemberTypeIsRefused() throws IOException {
        Path file = write("members.csv", "id,name,type\nM1,Alice,VIP\n");

        assertThrows(DataLoadException.class, () -> new MemberCsvParser().parse(file));
    }

    @Test
    void duplicateIdsAreRefusedByTheLoader() throws IOException {
        Path items = write("items.csv", "type,id,title,creator\nBOOK,B1,Dune,Herbert\nBOOK,B1,Other,Someone\n");
        Path members = write("members.csv", "id,name,type\nM1,Alice,STUDENT\n");

        assertThrows(DataLoadException.class,
                () -> new DataLoader().load(new LibraryRepository(), items, members));
    }

    @Test
    void loaderFillsTheRepositoryFromFiles() throws IOException {
        Path items = write("items.csv", "type,id,title,creator\nBOOK,B1,Dune,Herbert\n");
        Path members = write("members.csv", "id,name,type\nM1,Alice,STUDENT\n");
        LibraryRepository repository = new LibraryRepository();

        new DataLoader().load(repository, items, members);

        assertTrue(repository.findItem("B1").isPresent());
        assertTrue(repository.findMember("M1").isPresent());
    }
}
