package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
        primaryStage.setTitle("JavaFX File Processor");

        TextField filePathField = new TextField();
        filePathField.setPromptText("Enter file path");

        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> browseFile(primaryStage, filePathField));

        Button processButton = new Button("Process File");
        processButton.setOnAction(e -> processFile(filePathField.getText()));

        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setWrapText(true);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.getChildren().addAll(filePathField, browseButton, processButton, resultTextArea);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void browseFile(Stage primaryStage, TextField filePathField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void processFile(String filePath) {
        try {
            CompilerTokenizer.processFile(filePath);
            ArrayList<Token> tokens = CompilerTokenizer.customTokenList;
            SyntaxAnalyzer parser = new SyntaxAnalyzer(tokens);
            parser.parse();
            resultTextArea.setText("Parsing completed successfully! with no syntax errors");
        } catch (IOException e) {
            resultTextArea.setText("Error processing file: " + e.getMessage());
        } catch (RuntimeException e) {
            resultTextArea.setText("Syntax Error: " + e.getMessage());
        }
    }
}
