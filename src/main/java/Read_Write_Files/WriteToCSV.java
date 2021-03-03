package Read_Write_Files;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WriteToCSV {
    // first create file object for file placed at location
    static private File file;
    static private CSVWriter writer;
    static private List<String> temp;
    public static void initializeCsvWriter(String filePath) throws IOException {
        file = new File(filePath);
        FileWriter outputFile = new FileWriter(file, true);
        // create CSVWriter object fileWriter object as parameter
        writer = new CSVWriter(outputFile,',',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
    }
    public static void deleteContentsOfCsv(String filePath, String[] header) throws IOException {
        file = new File(filePath);
        if (file.exists() && file.isFile())
        {
            file.delete();
        }
        file.createNewFile();
        FileWriter outputFile = new FileWriter(file, true);
        // create CSVWriter object fileWriter object as parameter
        writer = new CSVWriter(outputFile,',',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        writer.writeNext(header);
        writer.close();
    }
    public static void writeNextLineCsv(String [] data) throws IOException {
        writer.writeNext(data);
        writer.close();
    }
}