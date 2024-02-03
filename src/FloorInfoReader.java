import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class for the data structure to process elevator service requests.
 * @author Joshua Braddon 101182605
 */
public class FloorInfoReader {
    //Holds the data from the file
    ArrayList<String> floorInfo = new ArrayList<>();

    /**
     * Creates a new FloorInfo object using the input file
     * @param requestDetails the input file containing the request
     */
    public FloorInfoReader(File requestDetails) {
        try {
            Scanner scanner = new Scanner(requestDetails);
            //Assigns the values from the file
            parseFloorInfo(scanner);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the input file for values
     * @param parser the object which will parse the file input
     */
    private void parseFloorInfo(Scanner parser) {
        //Checks for the whitespace between each value
        parser.useDelimiter("  ");
        //Fills an arraylist with the values
        while(parser.hasNext()) {
            floorInfo.add(parser.next());
            //System.out.println(parser.next());
        }
    }

    /*
    public void createCSVFile() throws IOException {
        File output = new File("Name");
        FileWriter fileWriter = new FileWriter(output);
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < floorInfo.size(); i++) {
            info.append(floorInfo.get(i));
            if (i != floorInfo.size() - 1) {
                info.append("  ");
            }
        }
        info.append("\n");
        fileWriter.write(info.toString());
    }*/


    public String getTime() {
        return floorInfo.get(0);
    }

    public String getServiceFloor() {
        return floorInfo.get(1);
    }

    public String getDirection() {
        return floorInfo.get(2);
    }

    public String getRequestFloor() {
        return floorInfo.get(4);
    }

}
