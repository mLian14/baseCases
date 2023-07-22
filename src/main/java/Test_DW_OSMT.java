
import processor.OSMT;
import processor.RSMT;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Test_DW_OSMT {

    public static void main (String[] args) throws FileNotFoundException {
        LocalDateTime start = LocalDateTime.now();
        System.out.println("Program Starts at: "+ DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(start));

//        RSMT rsmt = new RSMT("input_baseCases/case1/case1_I2C4_SCL", "result_baseCases/case1_I2C4_SCL");
//        OSMT osmt = new OSMT("input_baseCases/case5/case5_I2C2_SCL", "result_baseCases/OMST/case5_I2C2_SCL");

        OSMT osmt = new OSMT("input_baseCases/case1/case1_I2C4_SCL", "result_baseCases/OMST/case1_I2C4_SCL");
//        OSMT osmt = new OSMT("input_baseCases/test", "result_baseCases/test");


        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start,end);
        LocalDateTime duration_formated = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(duration.toMillis()), ZoneId.of("UTC"));
        System.out.println("Program Ends at: "+ DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(end));
        System.out.println("Program Run Time is: "+ DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(duration_formated));
    }
}
