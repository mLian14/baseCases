
import processor.RSMT;
import shapes.Master;
import shapes.Slave;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Test_DW {

    public static void main (String[] args) throws FileNotFoundException {
        LocalDateTime start = LocalDateTime.now();
        System.out.println("Program Starts at: "+ DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(start));

//        RSMT rsmt = new RSMT("input_baseCases/case1/case1_I2C4_SCL", "result_baseCases/case1_I2C4_SCL");
        RSMT rsmt = new RSMT("input_baseCases/case5/case5_I2C2_SCL", "result_baseCases/case5_I2C2_SCL");

        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start,end);
        LocalDateTime duration_formated = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(duration.toMillis()), ZoneId.of("UTC"));
        System.out.println("Program Ends at: "+ DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(end));
        System.out.println("Program Run Time is: "+ DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(duration_formated));
    }
}
