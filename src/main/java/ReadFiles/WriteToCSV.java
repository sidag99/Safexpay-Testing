package ReadFiles;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToCSV {
    // first create file object for file placed at location
    // specified by filepath
    private static File file;
    // create FileWriter object with file as parameter

    public static void writeDataToCSV(String filePath) throws IOException {

        file = new File(filePath);
        FileWriter outputFile = new FileWriter(file);
        // create CSVWriter object fileWriter object as parameter
        CSVWriter writer = new CSVWriter(outputFile);

        // adding header to csv
        String[] header = {"Name", "Class", "Marks"};
        writer.writeNext(header);

        // add data to csv
        String[] data1 = {"Aman", "10", "620"};
        writer.writeNext(data1);
        String[] data2 = {"Suraj", "10", "630"};
        writer.writeNext(data2);

        // closing writer connection
        writer.close();

    }
}
