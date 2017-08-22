package org.drools.examples.learner.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.drools.examples.learner.models.Nursery;

public class CsvFileReader {

    public static List<Object> readObjects( String filename, Function<CSVRecord, Object> mapper ) throws IOException {
        List<Object> objects = new ArrayList<>();
        Reader in = new FileReader( filename );
        CSVParser parser = CSVFormat.EXCEL.withHeader().parse( in );
        parser.forEach( record -> objects.add( mapper.apply( record ) ) );
        return objects;
    }
}
