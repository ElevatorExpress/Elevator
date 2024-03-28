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
    static final String[] requestExpected = {"12up30", "23down21", "42up90", "109down10", "161up82", "168up90", "169down10", "201up110", "2511down10", "301up120"};
    static final String testValues = "1  2  up  3  0 \n2  3  down  2  1\n4  2  up  9  0\n 10  9  down  1  0\n  16  1  up  8  2\n 16  8  up  9  0\n 16  9  down  1  0\n 20  1  up  11  0\n 25  11  down  1  0\n 30  1  up  12  0";
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
            sb.append(currentData.error());

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