package floor;

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
    public FloorInfoReader(File requestDetails) throws FileNotFoundException {
        Scanner lineGrabber;
        Scanner lineReader;
        //The information about each request
        ArrayList<String> floorInfo = new ArrayList<>();

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
            while (lineReader.hasNext()) {
                //Adds the piece of information to the current floor's request Data list
                //also trims any extra whitespace
                floorInfo.add(lineReader.next().trim());
            }
            //If the list is too small throw exception
            if (floorInfo.size() < 5) {
                throw new IllegalArgumentException();
            }
            //Adds the completed request to the request list
            requestQueue.add(new Data(floorInfo.get(0), floorInfo.get(1), floorInfo.get(2), floorInfo.get(3), floorInfo.get(4)));
            //If the requestQueue is empty throw exception
        }
        //If there are no entries in the queue throw an exception
        if (requestQueue.isEmpty()) {
            throw new IllegalArgumentException();
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
     * @param error used to inject errors into the system
     */
    public record Data(String time, String serviceFloor, String direction, String requestFloor, String error) implements Serializable{}
}
