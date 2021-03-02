package Functions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CreateNameByTimestamp {
    public static String getTimestamp(String dateFormat) {
        String timeStamp = new SimpleDateFormat(dateFormat).format(new Date());
        return timeStamp;
    }
    public static String getTimestamp() {
        String dateFormat="yyyy-MM-dd_HH:mm:ss";
        String timeStamp = new SimpleDateFormat(dateFormat).format(new Date());
        return timeStamp;
    }
}