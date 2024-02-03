import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Class for the data structure to process elevator service requests.
 * @author Joshua Braddon 101182605
 */
public class FloorInfoReader {
    //Holds the data from the file
    ArrayList<Data> requestQueue = new ArrayList<>();

    /**
     * Creates a new FloorInfo object using the input file
     * @param requestDetails the input file containing the request
     */
    public FloorInfoReader(File requestDetails) {
        Scanner lineGrabber;
        Scanner lineReader;
        ArrayList<String> floorInfo = new ArrayList<>();
        try {
            lineGrabber = new Scanner(requestDetails);
            while(lineGrabber.hasNextLine()) {
                floorInfo.clear();
                lineReader = new Scanner(lineGrabber.nextLine());
                lineReader.useDelimiter("  ");

                while(lineReader.hasNext()) {
                    floorInfo.add(lineReader.next());
                }
                requestQueue.add(new Data(floorInfo.get(0), floorInfo.get(1), floorInfo.get(2), floorInfo.get(3)));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            throw new RuntimeException(e);
        }
    }

    public Iterator<Data> getRequestQueue() {
        return requestQueue.iterator();
    }

//    private void testFileRead() {
//        for(Data d : requestQueue) {
//            System.out.println(d.time + " " + d.serviceFloor + " " + d.direction + " " + d.requestFloor);
//        }
//    }
    public record Data(String time, String serviceFloor, String direction, String requestFloor) {}

//    public static void main(String[] args) {
//        FloorInfoReader floorInfoReader = new FloorInfoReader(new File("C:\\Users\\jabra\\Desktop\\ENG year 3\\SYSC 3303\\Group project\\test"));
//        floorInfoReader.testFileRead();
//    }

}
