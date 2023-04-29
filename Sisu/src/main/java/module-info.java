module fi.Sisu {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    opens fi.Sisu.app to javafx.fxml;
    opens fi.Sisu.datasource to org.junit.platform.commons, org.junit.jupiter.params;
    opens fi.Sisu.view to javafx.fxml;
    opens fi.Sisu.model to com.fasterxml.jackson.databind;

    exports fi.Sisu.app;
    exports fi.Sisu.datasource;
    exports fi.Sisu.model;
    exports fi.Sisu.navigation;
    exports fi.Sisu.view;
    exports fi.Sisu.viewmodel;
}
