Files included:

./src/elevator/ElevatorSubsystem -> class representing an elevator which will process service requests
./src/floor/FloorInfoReader.java -> reads input text file and converts them into a queue of requests
./src/floor/FloorSystem.java -> tries to grab requests from message buffers and sends confirmation messages to the scheduler
./src/util/MessageBuffer.java -> the synchronized data structure to pass requests between subsystems
./src/Messages/MessageTypes.java -> enum for differentiating between elevator and floor messages
./src/util/ElevatorLogger.java -> Wrapper class for the builtin Logger.java, made easier to use for this project
./src/scheduler/Scheduler.java -> processes requests for the floors and elevators
./FloorData.txt -> this is the input file that gets read by main, it contains 10 elevator service requests

TO RUN:
JDK version: 21
ensure the working dir is:
    <Path>/Elevator and hit run on the run configuration named "Scheduler" in the run configuration dropdown menu,
     after that hit run on the run configuration named "Elevator" in the run configuration dropdown menu, finally hit
     run on the run configuration named "Floor" in the run configuration dropdown menu. This should start the system.

     Tests can be found under ./test. They can be run by clicking the Test run configuration named "All in Elevator", or
     can be run from the individual files themselves.

