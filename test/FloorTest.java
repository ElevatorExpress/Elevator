import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Iterator;

/**
 * A class to test if FloorSystem can read FloorInfoReader data
 * @author Joshua Braddon
 */
class FloorTest {

    static FloorInfoReader reader;
    static final String[] requestExpected = {"2:432up3", "3:124down2", "5:151up5", "11:152up4", "6:163down1", "7:451up5", "8:264down2"};

    @BeforeAll
    static void createFloorInfoReader(){
        reader = new FloorInfoReader(new File("./FloorTest"));
    }

    @Test
    static void readText(FloorInfoReader reader) {
        Iterator<FloorInfoReader.Data> requests = reader.getRequestQueue();
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while(requests.hasNext()) {
            //Adds the values from the Data ArrayList to a StringBuilder
            sb.append(requests.next().time());
            sb.append(requests.next().serviceFloor());
            sb.append(requests.next().direction());
            sb.append(requests.next().requestFloor());

            //Compares to the expected values
            Assertions.assertEquals(sb.toString(), requestExpected[i], "File did not read correctly, expected: " + requestExpected[i] + " actual: ");
            i++;
        }
    }
}