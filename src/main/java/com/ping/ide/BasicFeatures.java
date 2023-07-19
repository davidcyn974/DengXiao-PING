package com.ping.ide;

import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class BasicFeatures {

    // Run
    public static void runScript(String code, TextArea console) {
        try (Context context = Context.newBuilder().option("engine.WarnInterpreterOnly", "false").build()) {
            Value result = context.eval("js", code);
            console.setText(result.asString() + "\n");
        } catch (PolyglotException ex) {
            console.setText("Error: " + ex.getMessage() + "\n");
        }
    }

    // Print
    public static void printFeature(TextArea console)
    {
        try {
            // create a temporary file
            File tempFile = File.createTempFile("consoleOutput", ".txt");
            tempFile.deleteOnExit();

            // write the console text to the file
            PrintWriter writer = new PrintWriter(tempFile);
            writer.write(console.getText());
            writer.close();

            // print the file
            Desktop desktop = Desktop.getDesktop();
            desktop.print(tempFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void setButtonProperties(Button button, String iconName, String tooltipText) {
        ImageView icon = new ImageView(new Image(BasicFeatures.class.getResourceAsStream(iconName)));
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        button.setGraphic(icon);
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(button, tooltip);
    }
}
