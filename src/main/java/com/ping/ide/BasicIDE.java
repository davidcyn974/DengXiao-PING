package com.ping.ide;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;

import static com.ping.ide.BasicFeatures.setButtonProperties;
import static com.ping.ide.SyntaxHighlighter.computeHighlighting;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javafx.application.Platform;


public class BasicIDE extends Application {
    double fontSize;



    // Method to redirect the input and output streams of the process to the TextArea
    private void redirectStreams(Process process, TextArea textArea) {
        // Redirect the output stream of the process to the TextArea
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> textArea.appendText(finalLine + System.lineSeparator()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Redirect the error stream of the process to the TextArea
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> textArea.appendText(finalLine + System.lineSeparator()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Redirect the input stream of the process to the TextArea
        OutputStream processOutputStream = process.getOutputStream();
        StringBuilder inputBuffer = new StringBuilder();
        String consolePrefix = "> "; // Specify the console prefix here

        textArea.setOnKeyTyped(keyEvent -> {
            String character = keyEvent.getCharacter();
            inputBuffer.append(character);
        });

        textArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                try {
                    // Append the console prefix and input command to the TextArea
                    String inputCommand = inputBuffer.toString();
                    textArea.appendText(consolePrefix + inputCommand + "\n");

                    // Send the input command to the process
                    processOutputStream.write((inputCommand + "\n").getBytes());
                    processOutputStream.flush();

                    inputBuffer.setLength(0);

                    // Scroll to the end of the TextArea to display the output in the correct order
                    textArea.positionCaret(textArea.getLength());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }
    private File currentRootDirectory;

    SyntaxHighlighter highlighter = new SyntaxHighlighter();

    @Override
    public void start(Stage primaryStage) {

        // Create a text area for the code
        CodeArea codeArea = new CodeArea();
        codeArea.setStyle("-fx-font-size: 12pt;"); // Initial font size is 12



        // Create all the buttons for the toolbar
        Button zoomInButton = new Button();
        Button zoomOutButton = new Button();
        Button refreshButton = new Button();
        Button saveFileButton = new Button();
        Button runButton = new Button();
        Button printButton = new Button();
        Button Terminal = new Button();

        // Load all the different icons


        // Utilisation des méthodes réfactorisées pour configurer les boutons
        setButtonProperties(zoomInButton, "ic_zoom_in.png", "Zoom In");
        setButtonProperties(zoomOutButton, "ic_zoom_out.png", "Zoom Out");
        setButtonProperties(Terminal, "ic_term.png", "Term");
        setButtonProperties(refreshButton, "ic_refresh.png", "Refresh file tree");
        setButtonProperties(saveFileButton, "ic_save.png", "Save current file");
        setButtonProperties(runButton, "ic_run.png", "Run current file");
        setButtonProperties(printButton, "ic_print.png", "Print the console");


        // Configuration des icônes spécifiques aux dossiers et fichiers (si disponibles)
        ImageView folderIcon = new ImageView(new Image(getClass().getResourceAsStream("ic_folder.png")));
        Utils.setDimensions(folderIcon, 20, 20);

        ImageView fileIcon = new ImageView(new Image(getClass().getResourceAsStream("ic_file.png")));
        Utils.setDimensions(fileIcon, 20, 20);

        // Create a tab pane to contain all the files' tabs
        TabPane tabPane = new TabPane();
        // Define what happens when the "Zoom In" and "Zoom Out" buttons are clicked
        zoomInButton.setOnAction(e -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                CodeArea textArea = (CodeArea) selectedTab.getContent();
                String style = textArea.getStyle();
                double size = 12;  // Default font size
                if (style.matches(".*-fx-font-size: ([0-9]*).*")) {
                    size = Double.parseDouble(style.replaceAll(".*-fx-font-size: ([0-9]*).*", "$1"));
                }
                textArea.setStyle("-fx-font-size: " + (size + 2) + "pt;");
                fontSize = size + 2;
            }
        });

        zoomOutButton.setOnAction(e -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            CodeArea textArea = (CodeArea) selectedTab.getContent();
            String style = textArea.getStyle();
            double size = 12;  // Default font size
            if (style.matches(".*-fx-font-size: ([0-9]*).*")) {
                size = Double.parseDouble(style.replaceAll(".*-fx-font-size: ([0-9]*).*", "$1"));

                if (size > 4) { // Prevent font size from getting too small
                    textArea.setStyle("-fx-font-size: " + (size - 2) + "pt;");
                }
                fontSize = size - 2;
            }
        });


        //terminal

        Terminal.setOnAction( e  -> {
                try {
                    ProcessBuilder processBuilder;
                    if (System.getProperty("os.name").contains("Windows")) {
                        // For Windows, use "cmd.exe" as the terminal application
                        processBuilder = new ProcessBuilder("cmd.exe");
                    } else {
                        // For other operating systems, use "xterm" as the terminal application
                        processBuilder = new ProcessBuilder("xterm");
                    }
                    Process process = processBuilder.start();

                    // Create a new terminal tab
                    Tab terminalTab = new Tab("Terminal");
                    // Use a custom TextArea or a WebView to display the terminal output
                    TextArea terminalTextArea = new TextArea();
                    // Redirect the input and output streams of the terminal process to the TextArea
                    redirectStreams(process, terminalTextArea);
                    terminalTab.setContent(terminalTextArea);
                    tabPane.getTabs().add(terminalTab);
                    tabPane.getSelectionModel().select(terminalTab);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

        });

        /*
         * Code utilisé pour afficher une notification toute les XX secondes
         */
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                HelpMenu.AlarmHydratation();
            }
        }, 50000, 1800000);

        /*
         * Code pour la barre de menu 'Help'
         */
        // Create a help bar and a "Help" menu
        MenuBar helpBar = new MenuBar();
        Menu helpMenu = new Menu("Help");
        MenuItem gettingStarted = new MenuItem("Getting Started");
        MenuItem keyboardShortcuts = new MenuItem("Keyboard Shortcuts");
        MenuItem about = new MenuItem("About");
        MenuItem updates = new MenuItem("Updates");
        MenuItem technicalSupport = new MenuItem("Technical Support");
        // todo : Dav
        MenuItem faq = new MenuItem("FAQ");
        MenuItem documentation = new MenuItem("Documentation");

        // Créer un sous-menu pour les options d'accessibilité
        Menu accessibilityOptions = new Menu("Accessibility Options");
        // Ajouter les sous-éléments au sous-menu
        MenuItem fontSizeItem = new MenuItem("Font Size");
        MenuItem colorThemeItem = new MenuItem("Color Theme");
        // Ajouter les sous-éléments au menu "Accessibility Options"
        accessibilityOptions.getItems().addAll(fontSizeItem, colorThemeItem);

        // Color Theme menu item
        // By clicking this menu, change the theme color to dark or light
        colorThemeItem.setOnAction(e -> {
            HelpMenu.ColorPickerFeature(primaryStage);
        });

        // Technical Support menu item
        // Open HTML file with contacts
        technicalSupport.setOnAction(e -> {
            HelpMenu.TechnicalSupportFeature();
        });

        // Update menu item
        // Open a dialog with information about the IDE
        updates.setOnAction(e -> {
            HelpMenu.UpdateFeature();
        });
        // About menu item
        // Open a dialog with information about the IDE
        about.setOnAction(e -> {
            Utils.notif("About", "About this IDE", "This IDE was created for the sake of Deng Xiao Ping 邓小平.\n It was created in order to fulfill Michel's pleasure.\n It is surely not licensed under the MIT License.", "th.jpg", 500, 300);
        });

        // Accessibility Options menu item
        // Open a menu where you can change the font size
        fontSizeItem.setOnAction(e -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            CodeArea textArea = (CodeArea) selectedTab.getContent();
            //HelpMenu.FontSizeFeature(tabPane);

            Slider fontSizeSlider = new Slider(10, 30, fontSize); // Valeur initiale : 12, plage : 10-30
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
                textArea = (CodeArea) tabPane.getSelectionModel().getSelectedItem().getContent();
                textArea.setStyle("-fx-font-size: " + selectedFontSize + "pt;");
                fontSize = selectedFontSize;
            }

        });

        // Getting Started menu item
        gettingStarted.setOnAction(e -> {
            String url = "https://miro.com/welcomeonboard/RHRubkhGRlRtdGV5Rk9BWjNSODJ6ZVJMUHNVbnVPU2xRVmFGWHpTaWlOTFNKbFhnOXE1MzNHRmtUS01GN1g1ZnwzNDU4NzY0NTU2OTk0MTk0MTgxfDI=?share_link_id=995288361615";
            Utils.openWebPage(url);
        });

        // Keyboard Shortcuts menu item
        // Pop-up menu which says : nothing to see here
        keyboardShortcuts.setOnAction(e -> {
            HelpMenu.KeyboardFeature();
        });

        helpMenu.getItems().addAll(gettingStarted, accessibilityOptions, about, updates, technicalSupport, keyboardShortcuts);
                //documentation, keyboardShortcuts);
        helpBar.getMenus().addAll(helpMenu);

        /*
         * Fin du code pour barre de menu
         */

        // Create a menu bar and add a "File" menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openFileItem = new MenuItem("Open File...");
        MenuItem openDirectoryItem = new MenuItem("Open Directory...");
        MenuItem saveItem = new MenuItem("Save");


        fileMenu.getItems().addAll(openFileItem, openDirectoryItem, saveItem);
        menuBar.getMenus().addAll(fileMenu);

        // Create a tree view for the file system
        TreeView<File> fileTreeView = new TreeView<>();




        fileTreeView.setCellFactory(param -> new TreeCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    if (item.isDirectory()) {
                        setGraphic(new ImageView(folderIcon.getImage()));
                    } else {
                        setGraphic(new ImageView(fileIcon.getImage()));
                    }

                    setOnMouseClicked(event -> {
                        if (!item.isDirectory()) {
                            try {
                                String content = Files.readString(Path.of(item.getPath()));
                                CodeArea textArea = new CodeArea(content);
                                textArea.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                                textArea.setStyleSpans(0, computeHighlighting(textArea.getText(), item.getName()));
                                textArea.richChanges()
                                        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))  // filter out replacements of equal content
                                        .subscribe(change -> {
                                            textArea.setStyleSpans(0, computeHighlighting(textArea.getText(), item.getName()));
                                        });

                                // Ctrl + Y shortcut for redo
                                textArea.setOnKeyPressed(keyEvent -> {
                                    if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.Y) {
                                         textArea.redo();
                                    }
                                });

                                // Ctrl + T shortcut to open a new terminal
                                textArea.setOnKeyPressed(keyEvent -> {
                                    if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.T) {
                                        try {
                                            ProcessBuilder processBuilder;
                                            if (System.getProperty("os.name").contains("Windows")) {
                                                // For Windows, use "cmd.exe" as the terminal application
                                                processBuilder = new ProcessBuilder("cmd.exe");
                                            } else {
                                                // For other operating systems, use "xterm" as the terminal application
                                                processBuilder = new ProcessBuilder("xterm");
                                            }
                                            Process process = processBuilder.start();

                                            // Create a new terminal tab
                                            Tab terminalTab = new Tab("Terminal");
                                            // Use a custom TextArea or a WebView to display the terminal output
                                            TextArea terminalTextArea = new TextArea();
                                            // Redirect the input and output streams of the terminal process to the TextArea
                                            redirectStreams(process, terminalTextArea);
                                            terminalTab.setContent(terminalTextArea);
                                            tabPane.getTabs().add(terminalTab);
                                            tabPane.getSelectionModel().select(terminalTab);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });


                                Tab tab = new Tab(item.getName(), textArea);
                                // Save the File object in the tab's user data
                                tab.setUserData(item);
                                // Add a "*" to the tab's name if the text area's content is changed
                                textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                                    if (!tab.getText().endsWith("*")) {
                                        tab.setText(tab.getText() + "*");
                                    }
                                });
                                tabPane.getTabs().add(tab);
                                tabPane.getSelectionModel().select(tab);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });


        Pane treeViewContainer = new Pane(fileTreeView);
        treeViewContainer.setStyle("-fx-background-color: #f3f3f3;");

        // Define action for Refresh Button
        refreshButton.setOnAction(e -> Utils.refreshTreeView(fileTreeView, currentRootDirectory, folderIcon, fileIcon));
        openFileItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    String content = Files.readString(selectedFile.toPath());
                    // ici
                    // Create a new tab with the file content
                    CodeArea textArea = new CodeArea(content);
                    textArea.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                    textArea.setStyleSpans(0, computeHighlighting(textArea.getText(), selectedFile.getName()));
                    textArea.richChanges()
                            .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                            .subscribe(change -> {
                                textArea.setStyleSpans(0, computeHighlighting(textArea.getText(), selectedFile.getName()));
                            });

                    textArea.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.Y) {
                            textArea.redo();
                        }
                    });

                    // Ctrl + T shortcut to open a new terminal
                    textArea.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.T) {
                            try {
                                ProcessBuilder processBuilder;
                                if (System.getProperty("os.name").contains("Windows")) {
                                    // For Windows, use "cmd.exe" as the terminal application
                                    processBuilder = new ProcessBuilder("cmd.exe");
                                } else {
                                    // For other operating systems, use "xterm" as the terminal application
                                    processBuilder = new ProcessBuilder("xterm");
                                }
                                Process process = processBuilder.start();

                                // Create a new terminal tab
                                Tab terminalTab = new Tab("Terminal");
                                // Use a custom TextArea or a WebView to display the terminal output
                                TextArea terminalTextArea = new TextArea();
                                // Redirect the input and output streams of the terminal process to the TextArea
                                redirectStreams(process, terminalTextArea);
                                terminalTab.setContent(terminalTextArea);
                                tabPane.getTabs().add(terminalTab);
                                tabPane.getSelectionModel().select(terminalTab);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });



                    Tab tab = new Tab(selectedFile.getName(), textArea);
                    // Save the file object as the tab's user data
                    tab.setUserData(selectedFile);

                    textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!tab.getText().endsWith("*")) {
                            tab.setText(tab.getText() + "*");
                        }
                    });
                    tabPane.getTabs().add(tab);
                    tabPane.getSelectionModel().select(tab);

                    // Now make the file visible in the tree view
                    currentRootDirectory = selectedFile.getParentFile();
                    TreeItem<File> root = Utils.createNode(currentRootDirectory, folderIcon, fileIcon);
                    fileTreeView.setRoot(root);



                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        openDirectoryItem.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            if (selectedDirectory != null) {
                currentRootDirectory = selectedDirectory;
                TreeItem<File> rootItem = Utils.createNode(currentRootDirectory, folderIcon, fileIcon);
                fileTreeView.setRoot(rootItem);
            }
        });

        // MenuItem
        saveItem.setOnAction(e -> saveFile(tabPane));

        // Toolbar button
        saveFileButton.setOnAction(e -> saveFile(tabPane));




        // Define your toolbar as an HBox
        HBox toolbar = new HBox();
        HBox.setHgrow(toolbar, Priority.ALWAYS);

        // Add an expanding spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(refreshButton, saveFileButton, printButton, spacer, runButton, zoomOutButton, zoomInButton, Terminal);

        // Create a layout and add the toolbar, menu bar and text area
        BorderPane layout = new BorderPane();
        HBox menuContainer = new HBox(menuBar, helpBar);
        VBox topContainer = new VBox(menuContainer, toolbar);
        layout.setTop(topContainer);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(fileTreeView, tabPane);
        layout.setCenter(splitPane);

        TextArea console = new TextArea();
        console.setEditable(false);  // The user can't directly modify the console's text
        console.setFont(new Font("Courier New", 60));
        console.setOnMouseEntered(event -> {
            console.setFont(new Font("Courier New", 60));
        });

        layout.setBottom(console);

        // Run button action
        runButton.setOnAction(e -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                CodeArea currentCodeArea = (CodeArea) selectedTab.getContent();
                if (selectedTab.getText().endsWith(".html") || selectedTab.getText().endsWith(".html*"))
                {
                    Utils.openWebPage(selectedTab.getText());
                }
                else if (selectedTab.getText().endsWith(".js") || selectedTab.getText().endsWith(".js*"))
                {
                    BasicFeatures.runScript(currentCodeArea.getText(), console);
                }
            }
        });
        // Print button action
        printButton.setOnAction(e -> {
            BasicFeatures.printFeature(console);
        });


        // Create a scene and add the layout
        Scene scene = new Scene(layout, 800, 600);
         /*Auto Completion feature :
            When the user uses the key combination Ctrl, the code area will display a popup with all the possible completions
         */

        scene.setOnKeyPressed(event -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                // Assumes that the content node of the tab is a TextArea
                CodeArea textArea = (CodeArea) selectedTab.getContent();
                // If even is Ctrl + Space, show the completion popup
                if (event.isControlDown() && event.getCode() == KeyCode.SPACE) {
                    //Utils.notif("Ctrl + Space pressed", "Bla", "Bla", "th.jpg", 20 , 20);
                    int caretPos = textArea.getCaretPosition();
                    int wordStartPos = caretPos - 1;

                    while (wordStartPos >= 0 && !Character.isWhitespace(textArea.getText().charAt(wordStartPos))) {
                        wordStartPos--;
                    }

                    //String currentWord = textArea.getText().substring(wordStartPos + 1, caretPos).trim();
                    String currentWord = textArea.getText().substring(wordStartPos + 1, caretPos).trim().toLowerCase();

                    final List<String>[] listofTags = new List[]{new ArrayList<>()};
                    // Get the current file extension
                    if(selectedTab.getText().endsWith(".html") || selectedTab.getText().endsWith(".html*"))
                    {
                        listofTags[0] = new ArrayList<>(Arrays.asList(Constants.KEYWORDSHTML.clone()));
                    }
                    else if (selectedTab.getText().endsWith(".css") || selectedTab.getText().endsWith(".css*"))
                    {
                        listofTags[0] = new ArrayList<>(Arrays.asList(Constants.KEYWORDCSS.clone()));
                    }
                    else if (selectedTab.getText().endsWith(".php") || selectedTab.getText().endsWith(".php*"))
                    {
                        listofTags[0] = new ArrayList<>(Arrays.asList(Constants.KEYWORDPHP.clone()));
                    }
                    else if (selectedTab.getText().endsWith(".js") || selectedTab.getText().endsWith(".js*"))
                    {
                        listofTags[0] = new ArrayList<>(Arrays.asList(Constants.KEYWORDJS.clone()));
                    }
                    // End of code added

                    List<String> suggestions = listofTags[0].stream()
                            .filter(tag -> tag.startsWith(currentWord))
                            .collect(Collectors.toList());


                    Popup popup = new Popup();
                    VBox popupContent = new VBox();
                    popupContent.setAlignment(Pos.CENTER);
                    popupContent.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 3px;");

                    for (String suggestion : suggestions) {
                        Button suggestionButton = new Button(suggestion);
                        //suggestionLabel.setStyle("-fx-padding: 5px;");
                        popupContent.getChildren().add(suggestionButton);
                    }

                    popup.getContent().add(popupContent);
                    for(Node node : popupContent.getChildren()){
                        Button button = (Button) node;
                        int finalWordStartPos = wordStartPos;
                        button.setOnAction(e -> {
                            textArea.replaceText(finalWordStartPos + 1, caretPos, button.getText());
                            popup.hide();
                        });
                    }

                    // Positionnez la fenêtre contextuelle près du composant de saisie de texte
                    Bounds boundsInScene = textArea.localToScene(textArea.getBoundsInLocal());
                    Bounds boundsInScreen = textArea.localToScreen(textArea.getBoundsInLocal());
                    double posX = boundsInScreen.getMinX();
                    double posY = boundsInScreen.getMinY() + boundsInScene.getHeight();
                    popup.show(primaryStage, posX, posY);
                }
            }
        });

        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Basic IDE");
        primaryStage.show();
    }

    private void saveFile(TabPane tabPane) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            // Assumes that the content node of the tab is a TextArea
            CodeArea textArea = (CodeArea) selectedTab.getContent();
            File file = (File) selectedTab.getUserData();
            try {
                Files.write(file.toPath(), textArea.getText().getBytes());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            // remove the '*' from tab's name
            if (selectedTab.getText().endsWith("*")) {
                selectedTab.setText(selectedTab.getText().substring(0, selectedTab.getText().length() - 1));
            }
        }
    }
}
