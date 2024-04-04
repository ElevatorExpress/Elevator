package gui;

import elevator.ElevatorControlSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.ElevatorStateUpdate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ElevatorDisplayApp extends Application implements ECSListener {

    private static final String WELCOME = "ELEVATOR MONITOR";
    private ElevatorControlSystem elevatorControlSystem;
    private int elevatorsCount;
    private HashMap<Integer, Label> currentElevatorFloor;
    private HashMap<Integer, Label> currentElevatorStatus;
    private HashMap<Integer, Label> currentElevatorDestination;
    private HashMap<Integer, Label> currentElevatorDirection;
    private volatile boolean guiSetUpDone = false;

    private void ecsStart() {
        try {
            while (true) {
                elevatorControlSystem.runSystem();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startUp(Stage mainWindow){
        // Set Stage Attributes
        mainWindow.setOnCloseRequest(this::onCloseAction);
        mainWindow.setTitle("Elevator Monitor");
        mainWindow.setResizable(false);

        StackPane tSide = new StackPane();
        Text mainText = new Text(WELCOME);
        mainText.setTextAlignment(TextAlignment.CENTER);
        mainText.setFont(Font.font("Roboto Mono", FontWeight.BOLD, 18));
        mainText.setFill(Color.web("#198754"));

        tSide.setAlignment(Pos.CENTER);
        tSide.getChildren().add(mainText);

        GridPane centerGridPane = new GridPane();
        centerGridPane.setPadding(new Insets(10, 10, 10, 10));
        centerGridPane.setVgap(8);
        centerGridPane.setHgap(10);

        Label descriptionLabel;
        for (int i = 0; i < elevatorsCount; i++){
            int rowPos = 0;
            // Create Elevator ID Display
            descriptionLabel = new Label("ElevatorID");
            GridPane.setConstraints(descriptionLabel, i, rowPos++);
            centerGridPane.getChildren().add(descriptionLabel);

            Label elevatorID = new Label(String.valueOf(i + 1));
            GridPane.setConstraints(elevatorID, i, rowPos++);
            centerGridPane.getChildren().add(elevatorID);

            // Create current floor display
            descriptionLabel = new Label("Current Floor");
            GridPane.setConstraints(descriptionLabel, i, rowPos++);
            centerGridPane.getChildren().add(descriptionLabel);

            Label currentFloor = new Label("0");
            GridPane.setConstraints(currentFloor, i, rowPos++);
            centerGridPane.getChildren().add(currentFloor);
            currentElevatorFloor.put(i + 1, currentFloor);

            // Create current status display
            descriptionLabel = new Label("Current Status");
            GridPane.setConstraints(descriptionLabel, i, rowPos++);
            centerGridPane.getChildren().add(descriptionLabel);

            Label currentState = new Label("-");
            GridPane.setConstraints(currentState, i, rowPos++);
            centerGridPane.getChildren().add(currentState);
            currentElevatorStatus.put(i + 1, currentState);

            // Create current destination display
            descriptionLabel = new Label("Current Destination");
            GridPane.setConstraints(descriptionLabel, i, rowPos++);
            centerGridPane.getChildren().add(descriptionLabel);

            Label currentDestination = new Label("-");
            GridPane.setConstraints(currentDestination, i, rowPos++);
            centerGridPane.getChildren().add(currentDestination);
            currentElevatorDestination.put(i + 1, currentDestination);

            // Create current direction display
            descriptionLabel = new Label("Current Direction");
            GridPane.setConstraints(descriptionLabel, i, rowPos++);
            centerGridPane.getChildren().add(descriptionLabel);

            Label currentDirection = new Label("-");
            GridPane.setConstraints(currentDirection, i, rowPos);
            centerGridPane.getChildren().add(currentDirection);
            currentElevatorDirection.put(i + 1, currentDirection);

        }
        centerGridPane.setAlignment(Pos.CENTER);

        BorderPane mainBorderLayout = new BorderPane();
        mainBorderLayout.setTop(tSide);
        mainBorderLayout.setCenter(centerGridPane);

        Scene scene = new Scene(mainBorderLayout);
        try {
            String css = this.getClass().getResource("./stylesheet/elevatorGUIStyle.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (NullPointerException pointerException) {
            System.err.println("COULD NOT FIND STYLESHEET!");
        }

        mainWindow.setScene(scene);
        mainWindow.sizeToScene();
        guiSetUpDone = true;
    }

    private void onCloseAction(WindowEvent windowEvent) {

    }

    /**
     * Event is launched when an elevator is potentially updated
     *
     * @param updateHashMap The updated elevator states associated with their IDs'
     */
    @Override
    public void updateElevators(Map<Integer, ElevatorStateUpdate> updateHashMap) {
        Platform.runLater(() -> {
            for (int i = 1; i < elevatorsCount + 1; i++){
                ElevatorStateUpdate state = updateHashMap.get(i);
                if (state != null && guiSetUpDone) {
                    currentElevatorFloor.get(i).setText(String.valueOf(state.getFloor()));
                    currentElevatorDirection.get(i).setText(state.getDirection().toString());
                    currentElevatorDestination.get(i).setText(String.valueOf(state.getDestinationFloor()));
                    if (state.getStateSignal() != null)
                        currentElevatorStatus.get(i).setText(String.valueOf(state.getStateSignal()));
                }
            }
        });
    }

    /*
    TODO:
     - REPLACE current state with internal elevator State, redo ElevatorStateUpdate to add this.
     - Make content reactive and look better dont use labels only
     */

    @Override
    public void start(Stage stage) throws Exception {
        currentElevatorFloor = new HashMap<>();
        currentElevatorStatus = new HashMap<>();
        currentElevatorDestination = new HashMap<>();
        currentElevatorDirection = new HashMap<>();

        elevatorsCount = ElevatorStateUpdate.getElevatorCount();
        elevatorControlSystem = new ElevatorControlSystem(elevatorsCount);
        elevatorControlSystem.subscribeElevatorEvent(this);
        Thread ecsThread = new Thread(this::ecsStart);

        ecsThread.start();
        startUp(stage);
        stage.show();
    }

}
