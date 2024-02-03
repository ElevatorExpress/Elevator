import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Class for the data structure to process elevator service requests.
 * @author Joshua Braddon 101182605
 */
public class FloorInfoReader {
    //Holds the requests in a list of Data records
    ArrayList<Data> requestQueue = new ArrayList<>();

    /**
     * Creates a new FloorInfo object using the input file
     * @param requestDetails the input file containing the request
     */
    public FloorInfoReader(File requestDetails) {
        Scanner lineGrabber;
        Scanner lineReader;
        //The information about each request
        ArrayList<String> floorInfo = new ArrayList<>();
        try {
            //Parses line-by-line
            lineGrabber = new Scanner(requestDetails);
            //Keeps going until no more lines of text are left
            while(lineGrabber.hasNextLine()) {
                //Empty the list
                floorInfo.clear();
                //Each line will need its own scanner to parse the text, the delimiter will be two spaces
                lineReader = new Scanner(lineGrabber.nextLine());
                lineReader.useDelimiter("  ");

                //While there is still text on the line yet to be processed
                while(lineReader.hasNext()) {
                    //Adds the piece of information to the current floor's request Data list
                    //also trims any extra whitespace
                    floorInfo.add(lineReader.next().trim());
                }
                //Adds the completed request to the request list
                requestQueue.add(new Data(floorInfo.get(0), floorInfo.get(1), floorInfo.get(2), floorInfo.get(3)));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an iterator of the requestQueue list
     * @return the iterator
     */
    public Iterator<Data> getRequestQueue() {
        return requestQueue.iterator();
    }

    /**
     * Record for holding data from elevator service requests
     * @param time the time of the request
     * @param serviceFloor the floor the request will be serviced on (where the call button was pressed)
     * @param direction does the elevator need to travel up or down
     * @param requestFloor the floor button that will process the request
     */
    public record Data(String time, String serviceFloor, String direction, String requestFloor) {}
}
