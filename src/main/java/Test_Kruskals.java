import processor.KruskalsOMST;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @auther lianmeng
 * @create 21.07.23
 */
public class Test_Kruskals {
    public static void main (String[] args) throws FileNotFoundException {
        LocalDateTime start = LocalDateTime.now();
        System.out.println("Program Starts at: "+ DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(start));

//        RSMT rsmt = new RSMT("input_baseCases/case1/case1_I2C4_SCL", "result_baseCases/case1_I2C4_SCL");
//        KruskalsOMST smt = new KruskalsOMST("input_baseCases/test", "result_baseCases");
        KruskalsOMST smt = new KruskalsOMST("input_baseCases/case1/case1_I2C4_SCL", "result_baseCases/KMST/case1_I2C4_SCL");

        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start,end);
        LocalDateTime duration_formated = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(duration.toMillis()), ZoneId.of("UTC"));
        System.out.println("Program Ends at: "+ DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(end));
        System.out.println("Program Run Time is: "+ DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(duration_formated));
    }


}
