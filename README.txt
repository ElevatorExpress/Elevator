Files included:

./src/elevator/ElevatorSubsystem -> class representing an elevator which will process service requests
./src/floor/FloorInfoReader.java -> reads input text file and converts them into a queue of requests
./src/floor/FloorSystem.java -> tries to grab requests from message buffers and sends confirmation messages to the scheduler
./src/util/MessageBuffer.java -> the synchronized data structure to pass requests between subsystems
./src/Messages/MessageTypes.java -> enum for differentiating between elevator and floor messages
./src/util/ElevatorLogger.java -> Wrapper class for the builtin Logger.java, made easier to use for this project
./src/scheduler/Scheduler.java -> processes requests for the floors and elevators
./src/util/SubSystem -> interface which allows threads to read and write to a util.MessageBuffer

./FloorData.txt -> this is the input file that gets read by main, it contains 10 elevator service requests
TO RUN:
JDK version: 21
ensure the working dir is: <Path>/Elevator and hit run or compile and run in your IDE of choice.
