package com.ping.ide;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class HelpMenu {
    public static void AlarmHydratation() {
        // Code à exécuter à chaque itération du timer
        // Afficher la notification
        // Récuperer la date acutelle complète avec l'heure et les secondes
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = dateFormat.format(date);
        Platform.runLater(() -> Utils.notif("Toujours rester hydraté !", "Il est " + formattedDate, "Il faut boire de l'eau ! ", "drink.jpg", 500, 370));
    }
    public static void KeyboardFeature()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("KeyBoard Shortcuts");
        alert.setHeaderText("Here are the keyboard shortcuts for the IDE");
        alert.setContentText("CTRL+Z : Undo \nCTRL+Y : Redo \nESCAPE : Exit the pop up alerts \nCTRL Then SPACE : Auto Completion \nCTRL+T: Open New Terminal Tab");
        alert.showAndWait();
    }

    public static void UpdateFeature()
    {
        Stage updatesStage = new Stage();
        updatesStage.setTitle("Version 9.7.4. There are no updates available at this time.");
        // Chargez le fichier GIF animé à partir d'un fichier ou d'une ressource
        Image gifImage = new Image(Objects.requireNonNull(HelpMenu.class.getResourceAsStream("tenor.gif")));

        // Créez un ImageView contenant le GIF animé
        ImageView gifImageView = new ImageView(gifImage);

        // Créez un conteneur pour le contrôle ImageView
        StackPane stackPane = new StackPane(gifImageView);

        // Créez une scène avec le conteneur StackPane
        Scene scene = new Scene(stackPane);

        updatesStage.setScene(scene);
        updatesStage.showAndWait();
    }
    public static void TechnicalSupportFeature()
    {
        // opens HTML page with all contacts
        String htmlPagePath = "src/main/resources/com/ping/ide/technical_support_contacts.html";
        // Chemin vers le fichier HTML contenant les contacts de support technique
        try {
            File htmlFile = new File(htmlPagePath);
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void ColorPickerFeature(Stage primaryStage)
    {
        // Create a new stage for the color theme dialog
        Stage colorThemeStage = new Stage();
        colorThemeStage.setTitle("Color Theme");
        // Create a list of color themes
        ObservableList<String> colorThemes = FXCollections.observableArrayList("Light", "Dark");
        // Create a combo box to select the color theme
        ComboBox<String> colorThemeComboBox = new ComboBox<>(colorThemes);
        colorThemeComboBox.setValue("Light"); // Default color theme is light
        // Create a button to apply the selected color theme
        javafx.scene.control.Button applyButton = new javafx.scene.control.Button("Apply");
        // Create a button to cancel the color theme dialog
        javafx.scene.control.Button cancelButton = new Button("Cancel");
        // Create a horizontal box to contain the buttons
        HBox buttonBox = new HBox(applyButton, cancelButton);
        // Create a vertical box to contain the combo box and the button box
        VBox colorThemeBox = new VBox(colorThemeComboBox, buttonBox);
        // Create a scene for the color theme dialog
        Scene colorThemeScene = new Scene(colorThemeBox);
        // Set the scene to the color theme stage
        colorThemeStage.setScene(colorThemeScene);
        // Show the color theme stage
        colorThemeStage.show();
        // Define what happens when the "Apply" button is clicked
        applyButton.setOnAction(e2 -> {
            // Get the selected color theme
            String colorTheme = colorThemeComboBox.getValue();
            // Change the color theme of the IDE
            if (colorTheme.equals("Light")) {
                JMetro jMetro = new JMetro(Style.LIGHT);
                jMetro.setScene(primaryStage.getScene());
            } else if (colorTheme.equals("Dark")) {
                JMetro jMetro = new JMetro(Style.DARK);
                jMetro.setScene(primaryStage.getScene());
            }
            // Close the color theme stage
            colorThemeStage.close();
        });
        // Define what happens when the "Cancel" button is clicked
        cancelButton.setOnAction(e2 -> {
            // Close the color theme stage
            colorThemeStage.close();
        });
    }

    public static void FontSizeFeature(TabPane tabPane)
    {
        Slider fontSizeSlider = new Slider(10, 30, 12); // Valeur initiale : 12, plage : 10-30
        fontSizeSlider.setBlockIncrement(1);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setMajorTickUnit(5);

        // Créer une boîte de dialogue
        javafx.scene.control.Dialog<Double> fontSizeDialog = new javafx.scene.control.Dialog<>();
        fontSizeDialog.setTitle("Taille de la police");
        fontSizeDialog.setHeaderText("Sélectionnez la taille de la police");

        // Ajouter le contrôle Slider à la boîte de dialogue
        fontSizeDialog.getDialogPane().setContent(fontSizeSlider);

        // Ajouter des boutons OK et Annuler
        ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        fontSizeDialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

        // Récupérer la valeur sélectionnée lorsque l'utilisateur clique sur OK
        fontSizeDialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOK) {
                return fontSizeSlider.getValue();
            }
            return null;
        });

        // Afficher la boîte de dialogue et récupérer la taille de la police sélectionnée
        Double selectedFontSize = fontSizeDialog.showAndWait().orElse(null);
        // Utiliser la taille de la police sélectionnée
        if (selectedFontSize != null) {
            // Sur la même logique utilisée pour Zoom in et Zoom out
            // on récupère le contenu de l'onglet sélectionné et on change la taille de la police
            javafx.scene.control.TextArea textArea = (TextArea) tabPane.getSelectionModel().getSelectedItem().getContent();
            textArea.setStyle("-fx-font-size: " + selectedFontSize + "pt;");
        }
    }
}
