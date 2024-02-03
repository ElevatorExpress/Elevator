Files included:

./3303A1Sequence.pdf -> Assignment 1 Sequence Diagram
./3303A1UML.pdf -> Assignment 1 UML Diagram

./src/Main.java -> Main class and entry point
<<<<<<< HEAD
./src/NotScheduler.java -> agent, runs in main thread
./src/Floor.java -> runnable class, runs in its own thread(s), tries to grab ingredients from the beanmaster agent
./src/MessageBuffer.java -> class that represents a resource that can be shared between threads, one of each type is held by
the beanmaster agent, the actual synchronization is done by the resource itself
=======
/src/FloorInfoReader.java -> reads input text file and converts them into a queue of requests
./src/FloorSystem.java -> runs in its own thread(s), tries to grab requests from message buffers and sends confirmation messages to the scheduler
./src/MessageBuffer.java -> the synchronized data structure to pass requests between subsystems

./src/Scheduler.java -> sends and receives requests to move elevators
./src/SubSystem.java -> interface which allows threads to read and write to a MessageBuffer
./src/ThreadSafeCounterUtility.java -> a thread safe counter to keep count requests
./src/
>>>>>>> majorRefac

./src/ResourceType.java -> enum that represents the different types of resources


TO RUN:
JDK version: 21

ensure the working dir is: <Path>/Assignment1_101208030 and hit run or compile and run in your IDE of choice.
