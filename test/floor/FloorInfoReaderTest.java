package floor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

class FloorInfoReaderTest {
    static FloorInfoReader reader, emptyReader;
    //Test cases
    static final String[] requestExpected = {"2:432up3", "3:124down2", "5:151up5", "11:152up4", "12:257down3", "6:163down1", "7:451up5", "8:264down2"};
    static final String testValues = "2:43  2  up  3 \n3:12  4  down  2\n5:15  1  up  5\n11:15  2  up  4\n  12:25  7  down  3\n6:16   3   down   1\n 7:45  1   up   5\n 8:26  4  down  2  pow";
    static File dataFile = new File("./test/FloorInfoReaderTest.txt");
    static File emptyFile = new File("./test/FloorInfoReaderTestEmpty.txt");

    /**
     * Before running the test create 2 files with data, one if empty and the other is filled with sample requests
     */
    @BeforeAll
    static void setUp() throws FileNotFoundException {
        try {
            //Writes the data file
            FileWriter myWriter = new FileWriter(dataFile);
            myWriter.write(testValues);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            //Writes the empty file
            FileWriter myWriter = new FileWriter(emptyFile);
            myWriter.write("");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        reader = new FloorInfoReader(dataFile);
    }

    /**
     * Testing the data reading with expected values, values with extra spaces, and with extra values
     */
    @Test
    void readText() {
        //Gets the list of requests from the file
        Iterator<FloorInfoReader.Data> requests = reader.getRequestQueue();
        int i = 0;
        //While there are still values left
        while(requests.hasNext()) {
            //Creates a new string for each block
            StringBuilder sb = new StringBuilder();
            FloorInfoReader.Data currentData = requests.next();
            //Adds the values from the Data ArrayList to a StringBuilder
            sb.append(currentData.time());
            sb.append(currentData.serviceFloor());
            sb.append(currentData.direction());
            sb.append(currentData.requestFloor());

            //Compares to the expected values
            Assertions.assertEquals(requestExpected[i], sb.toString(), "File did not read correctly");
            i++;
        }
    }

    /**
     * Testing that an empty file throws an exception like it is supposed to
     */
    @Test
    void readEmpty() {
        boolean flag = false;
        //This is excepted to throw an exception
        try {
            emptyReader = new FloorInfoReader(emptyFile);
        //If it throws the correct exception
        } catch(IllegalArgumentException e) {
            flag = true;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        Assertions.assertTrue(flag, "Did not throw exception");
    }
}