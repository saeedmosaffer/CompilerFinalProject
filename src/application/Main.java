package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    private TextArea resultTextArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Parser FX");

        Label label = new Label("Select a file:");
        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);

        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> browseFile(primaryStage));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, browseButton, resultTextArea);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void browseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                String filePath = selectedFile.getAbsolutePath();
                CompilerTokenizer.tokenizeFile(filePath);
                ArrayList<Token> tokens = CompilerTokenizer.tokensList;
                SyntaxAnalyzer parser = new SyntaxAnalyzer(tokens);
                parser.analyzeSyntax();

                // Display result in the TextArea
                displayResult("Parsing completed successfully! with no syntax errors");
            } catch (IOException e) {
                displayResult("Error reading the file: " + e.getMessage());
            }
        }
    }

    private void displayResult(String message) {
        resultTextArea.setText(message);
    }
}
