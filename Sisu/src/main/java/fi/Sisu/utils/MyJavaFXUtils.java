package fi.Sisu.utils;

import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import fi.Sisu.model.Course;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Adds a listener to the textProperty() of the given TextField that only allows
 * characters matching the provided regular expression to be entered.
 *
 * @param textField the TextField to which to add the listener
 * @param regex     the regular expression to use for filtering the entered
 *                  characters
 */
public class MyJavaFXUtils {

    /**
     * Adds a listener to the textProperty() of the given TextField that only allows
     * characters matching the provided regular expression to be entered.
     * 
     * @param textField TextField to add the listener to
     * @param regex     String regular expression
     */
    public static void addCharacterConstraint(TextField textField, String regex) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(regex)) {
                textField.setText(oldValue);
            }
        });
    }

    /**
     * Displays an alert with the given type, title and content.
     * 
     * @param type      AlertType type
     * @param title     String title
     * @param content   String content
     */
    public static void displayAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }


    /**
     * Displays an alert with the given type, title, header and content.
     * 
     * @param type      AlertType type
     * @param title     String title
     * @param header    String header
     * @param content   String content
     */
    public static void displayAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    /**
     * Creates an alert with more info button, so we can hide the  
     * error details from the user behind an extra button.
     * 
     * @param type AlertType type
     * @param title String title
     * @param header String header
     * @param errorMessage String errorMessage 
     * @param detailedErrorInfo String detailedErrorInfo
     */
    public static void displayAlertWithMoreInfo(
        AlertType type,
        String title,
        String header,
        String errorMessage,
        String detailedErrorInfo
        ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(errorMessage);
    
        ButtonType moreInfoButton = new ButtonType("More Info");
        ButtonType playDespacitoButton = new ButtonType("I am so sad");
        alert.getButtonTypes().add(moreInfoButton);
        alert.getButtonTypes().add(playDespacitoButton);
    
        alert.showAndWait().ifPresent(response -> {
            if (response == moreInfoButton) {
                Alert moreInfoAlert = new Alert(AlertType.INFORMATION);
                moreInfoAlert.setTitle("Detailed Error Information");
    
                TextArea textArea = new TextArea(detailedErrorInfo);
                textArea.setEditable(false);
                textArea.setWrapText(true);
    
                moreInfoAlert.getDialogPane().setContent(textArea);
                moreInfoAlert.showAndWait();
            } 
            else if (response == playDespacitoButton) {
                displayYouTubeVideo();
            }
        });
    }


    /**
     * Comforts users by playing a pleasant video in a webview
     */
    public static void displayYouTubeVideo() {
        WebView webView = new WebView();
        String iframe = "<iframe width=\"780\" height=\"580\" src=\"https://www.youtube.com/embed/9bZkp7q19f0?autoplay=1\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>";
        webView.getEngine().loadContent("<!DOCTYPE html><html><body>" + iframe + "</body></html>");

    
        Scene scene = new Scene(webView, 800, 600); // Set preferred WebView size
        Stage videoStage = new Stage();
        videoStage.setScene(scene);                
        videoStage.setOnCloseRequest((WindowEvent event) -> {
            webView.getEngine().load(null);
        });        
        videoStage.show();
    }


    public static void checkInternetConnection() throws IOException {
        try {
            final URL url = new URL("https://sis-tuni.funidata.fi");
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            final int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException();
            }
        } catch (IOException e) {
            System.err.println("No internet connection");                  
        }
    }

    /**
     * Formats the course's HTML content and loads it to the WebView.
     * Common method used by at least DegreeProgramme and SearchCourse views.
     * 
     * @param newCourse Course to be formatted and loaded
     */
    public static String formatCourseHTML(Course newCourse) {
        StringBuilder htmlContent = new StringBuilder();

        if (newCourse == null) {
            htmlContent.append("<p>No information available for the selected item.</p>");
            return htmlContent.toString();
        }        

        htmlContent.append("<html><head><style>body {font-family: Arial, Helvetica, sans-serif;}</style></head><body>"); 
        htmlContent.append("<h1>").append(newCourse.getName()).append("</h1>");
        htmlContent.append("<p>Opintopisteet: " + newCourse.getTargetCredits() + "</p>");
        htmlContent.append("<p>Lyhenne: " + newCourse.getAbbreviation() + "</p>");

        if (newCourse.getGrade() != null) {
            htmlContent.append("<p>Arvosana: ").append(newCourse.getGrade()).append("</p>");
        }

        if (newCourse.isGraded()) {
            htmlContent.append("<p>Arviointiasteikko: 0-5</p>");
        } else {
            htmlContent.append("<p>Arviointiasteikko: hyväksytty/hylätty</p>");
        }

        htmlContent.append("<h2>Kurssin sisältökuvaus</h2>");
        if (newCourse.getContentDescription() != null) {
            htmlContent.append(newCourse.getContentDescription());
        }   
        else {
            htmlContent.append("<p>Ei sisältökuvausta saatavilla.</p>");
        }     

        htmlContent.append("<h2>Oppimistavoitteet</h2>");
        if (newCourse.getLearningOutcomes() != null) {            
            htmlContent.append(newCourse.getLearningOutcomes());
        }
        else {
            htmlContent.append("<p>Ei oppimistavoitteita saatavilla.</p>");
        }
        htmlContent.append("</body></html>");

        return htmlContent.toString();        
    }
}
