package gui;

import elevator.ElevatorControlSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.Direction;
import util.ElevatorStateUpdate;

import java.io.IOException;
import java.util.HashMap;

public class ElevatorDisplayApp extends Application implements ElevatorListener {

    private static final String WELCOME = "ELEVATOR MONITOR";
    private ElevatorControlSystem elevatorControlSystem;
    private int elevatorsCount;
    private HashMap<Integer, TextField> currentElevatorFloor;
    private HashMap<Integer, TextField> currentElevatorMoving;
    private HashMap<Integer, TextField> currentElevatorCapacity;
    private HashMap<Integer, TextField> currentElevatorDirection;
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
        tSide.setPrefWidth(300);

        GridPane centerGridPane = new GridPane();
        centerGridPane.setPadding(new Insets(10, 10, 10, 10));
        centerGridPane.setVgap(8);
        centerGridPane.setHgap(10);

        int rowResetPos = 0;
        for (int i = 0; i < elevatorsCount; i++){
            int rowPos = rowResetPos;
            int colPos = i % 2;
            // Create Elevator ID Display
            HBox idPane = getGridHBoxPane();

            createAndAddDescriptionLabel(idPane, "ElevatorID - " + (i + 1));
            GridPane.setConstraints(idPane, colPos, rowPos++);
            centerGridPane.getChildren().add(idPane);


            // Create current floor display
            HBox floorPane = getGridHBoxPane();

            createAndAddDescriptionLabel(floorPane, "Floor");
            TextField currentFloor = getGridTextField(floorPane, "0");
            currentElevatorFloor.put(i + 1, currentFloor);

            GridPane.setConstraints(floorPane, colPos, rowPos++);
            centerGridPane.getChildren().add(floorPane);


            // Create current status display
            HBox statusPane = getGridHBoxPane();

            createAndAddDescriptionLabel(statusPane, "Status");
            TextField currentState = getGridTextField(statusPane, "-");
            currentElevatorMoving.put(i + 1, currentState);

            GridPane.setConstraints(statusPane, colPos, rowPos++);
            centerGridPane.getChildren().add(statusPane);


            // Create current destination display
            HBox capacityPane = getGridHBoxPane();

            createAndAddDescriptionLabel(capacityPane, "Capacity");
            TextField currentCapacity = getGridTextField(capacityPane, "-");
            currentElevatorCapacity.put(i + 1, currentCapacity);

            GridPane.setConstraints(capacityPane, colPos, rowPos++);
            centerGridPane.getChildren().add(capacityPane);


            // Create current direction display
            HBox directionPane = getGridHBoxPane();

            createAndAddDescriptionLabel(directionPane, "Direction");
            TextField currentDirection = getGridTextField(directionPane, "-");
            currentElevatorDirection.put(i + 1, currentDirection);

            GridPane.setConstraints(directionPane, colPos, rowPos);
            centerGridPane.getChildren().add(directionPane);

            // Calculate Row Pos Reset
            if (i % 2 != 0) {
                rowResetPos += 5;
            }
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

    private TextField getGridTextField(HBox pane, String defaultText) {
        TextField textField = new TextField(defaultText);
        textField.setEditable(false);
        textField.setStyle("-fx-border-color: #222222; border-color: #222222; -fx-background-color: #1A1A1A; background-color: #1A1A1A; -fx-text-fill: #479f76;");
        textField.selectedTextProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) textField.deselect();
        });
        pane.getChildren().add(textField);
        return textField;
    }

    private HBox getGridHBoxPane() {
        HBox pane = new HBox(3);
        pane.setAlignment(Pos.CENTER_RIGHT);
        return pane;
    }

    private void createAndAddDescriptionLabel(Pane labelPane, String description) {
        Label descriptionLabel;
        descriptionLabel = new Label(description);
        labelPane.getChildren().add(descriptionLabel);
    }

    private void onCloseAction(WindowEvent windowEvent) {

    }

    /**
     * Ran when the current floor is updated. Provides current floor.
     *
     * @param id    The elevator ID
     * @param floor The elevator's current floor
     */
    @Override
    public void updateCurrentFloor(int id, int floor) {
        if (guiSetUpDone){
            Platform.runLater(() -> currentElevatorFloor.get(id).setText(String.valueOf(floor)));
        }
    }

    /**
     * Ran when the moving state is updated. Provides moving state
     *
     * @param id     The elevator ID
     * @param moving The elevator's current moving state
     */
    @Override
    public void updateMovingState(int id, Moving moving) {
        if (guiSetUpDone){
            Platform.runLater(() -> currentElevatorMoving.get(id).setText(moving.name()));
        }
    }

    /**
     * Ran when the direction is updated. Provides the direction
     *
     * @param id        The elevator ID
     * @param direction The elevator's direction
     */
    @Override
    public void updateDirection(int id, Direction direction) {
        if (guiSetUpDone){
            Platform.runLater(() -> currentElevatorDirection.get(id).setText(direction.name()));
        }
    }

    /**
     * Ran when the capacity is updated. Provides the capacity
     *
     * @param id       The elevator ID
     * @param capacity The elevator's current capacity
     */
    @Override
    public void updateCapacity(int id, int capacity) {
        if (guiSetUpDone){
            Platform.runLater(() -> currentElevatorCapacity.get(id).setText(String.valueOf(capacity)));
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        currentElevatorFloor = new HashMap<>();
        currentElevatorMoving = new HashMap<>();
        currentElevatorCapacity = new HashMap<>();
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
