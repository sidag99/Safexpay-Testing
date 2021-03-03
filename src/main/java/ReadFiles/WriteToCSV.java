package ReadFiles;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToCSV {
    // first create file object for file placed at location
    private File file;

    public void initializeCsvWriter(String filePath) throws IOException {
        file = new File(filePath);
    }
    public void deleteContentsOfCsv(String filePath, String[] header) throws IOException {
        file = new File(filePath);
        if (file.exists() && file.isFile())
        {
            file.delete();
        }
        file.createNewFile();
        FileWriter outputFile = new FileWriter(file, true);
        // create CSVWriter object fileWriter object as parameter
        CSVWriter writer = new CSVWriter(outputFile);
        writer.writeNext(header);
        writer.close();
    }
    public void writeCsv(String [] data) throws IOException {
        FileWriter outputFile = new FileWriter(file, true);
        // create CSVWriter object fileWriter object as parameter
        CSVWriter writer = new CSVWriter(outputFile);
        writer.writeNext(data);
        writer.close();
    }

}
