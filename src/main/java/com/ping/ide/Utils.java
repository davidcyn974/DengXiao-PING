package com.ping.ide;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Utils {

    public static TreeItem<File> createNode(final File file, ImageView folderIcon, ImageView fileIcon) {
        return new TreeItem<File>(file) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    isLeaf = file.isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> treeItem) {
                File f = treeItem.getValue();
                if (f != null && f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                        for (File childFile : files) {
                            children.add(createNode(childFile, folderIcon, fileIcon));
                        }
                        return children;
                    }
                }
                return FXCollections.emptyObservableList();
            }
        };
    }

    public static void refreshTreeView(TreeView<File> fileTreeView, File rootDirectory, ImageView folderIcon, ImageView fileIcon) {
        TreeItem<File> root = createNode(rootDirectory, folderIcon, fileIcon);
        fileTreeView.setRoot(root);
    }



    // Create a simple notification
    public static void notif(String title, String headerText, String contentText, String imagePath, int ImageWidth, int ImageHeight) {
        Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
        aboutDialog.setTitle(title);
        aboutDialog.setHeaderText(headerText);
        aboutDialog.setContentText(contentText);
        // Chargez l'image à partir d'un fichier ou d'une ressource
        Image image = new Image(Utils.class.getResourceAsStream(imagePath));
        // Créez un ImageView contenant l'image
        ImageView imageView = new ImageView(image);
        // Définissez la taille de l'image (facultatif)
        imageView.setFitWidth(ImageWidth); // 500 for deng
        imageView.setFitHeight(ImageHeight); // 300 for deng
        // Définissez l'image sur la boîte de dialogue
        aboutDialog.setGraphic(imageView);
        aboutDialog.showAndWait();
    }


    // open a web page
    public static void openWebPage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // set dimensions of an image
    public static void setDimensions(ImageView image, int height, int width) {
        image.setFitHeight(height);
        image.setFitWidth(width);
    }

}
