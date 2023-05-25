package pcd.assignment2.executors;

import pcd.assignment2.common.SourceLineParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestParserMain {
    public static void main(String[] args) throws IOException {
        System.out.println("".length());
//        System.out.println(Paths.get("TestParserClass.java").toAbsolutePath().toString());
//        System.out.println(Paths.get(System.getProperty("user.dir")).toString());
        Path testClass = Paths.get("src\\main\\java\\pcd\\assignment2\\executors\\TestParserClass.java");
        SourceLineParser parser = new SourceLineParser();
        long nLines;
        try (Stream<String> stream = Files.lines(testClass)) {
            nLines = stream.filter(l -> parser.parseLine(l)).count();
        }
        System.out.println(nLines);
    }
}
