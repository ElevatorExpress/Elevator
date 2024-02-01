Files included:

./3303A1Sequence.pdf -> Assignment 1 Sequence Diagram
./3303A1UML.pdf -> Assignment 1 UML Diagram

./src/Main.java -> Main class and entry point
./src/Scheduler.java -> agent, runs in main thread
./src/Floor.java -> runnable class, runs in its own thread(s), tries to grab ingredients from the beanmaster agent
./src/ThreadSafeResourceBuffer.java -> class that represents a resource that can be shared between threads, one of each type is held by
the beanmaster agent, the actual synchronization is done by the resource itself

./src/ResourceType.java -> enum that represents the different types of resources


TO RUN:
JDK version: 20

ensure the working dir is: <Path>/Assignment1_101208030 and hit run or compile and run in your IDE of choice.
