package ReadFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
ReadFromCSV(String path)  //Constructor, Stores all the rows in a list, with columns separated by a ","

String[] ReadLineNumber(int line)  //Reads the row number, separates columns by "," and stores in array
 */
public class ReadFromCSV {
    static List<String> StringArrayList = new ArrayList<>();

    public ReadFromCSV(String path) throws IOException {
        StringArrayList.clear();
        List<String> credentials = Files.readAllLines(Paths.get(path));
        for (String s : credentials) {
            StringArrayList.add(s);
        }
    }

    public String[] ReadLineNumber(int line) throws Exception {
        String login = StringArrayList.get(line);
        String[] StringArray = login.split(",");
        return StringArray;
    }

}
