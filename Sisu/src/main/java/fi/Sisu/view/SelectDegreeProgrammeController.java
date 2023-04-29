package fi.Sisu.view;

import fi.Sisu.model.SisuNode;
import fi.Sisu.viewmodel.SelectDegreeProgrammeViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

/**
 * Controller for Study Program screen.
 * 
 * @author Antti Hakkarainen
 */
public class SelectDegreeProgrammeController extends Controller {

    // Links .fxml elements to the .fxml View
    @FXML
    Label studyProgramsLabel;

    @FXML 
    Label sortLabel;

    @FXML 
    Label filterLabel;

    @FXML 
    TextField filterTextField;

    @FXML 
    ListView<SisuNode> dpListView;

    @FXML 
    ToggleButton sortToggleButton;

    @FXML 
    Button selectStudyProgramButton;

    @FXML 
    Button clearFilterButton;

    @FXML
    Button cancelButton; 

    @FXML
    Button refreshButton;

    // To pass events to and receive new data from sisuApiModel.
    private SelectDegreeProgrammeViewModel viewModel;

    /**
     * Construcor to store private variables and set the binds and listening
     * actions to this controller.
     * 
     * @param viewModel
     */
    public SelectDegreeProgrammeController(SelectDegreeProgrammeViewModel viewModel) {
        this.viewModel = viewModel;    

    }


    /**
     * Initialize listeners and setters between controller and viewmodel, so
     * the changes in viewmodel's data are reflected in the view.
     */
    @FXML
    public void initialize() {

        // Set the cell factory for the listview to display only the name of the DegreeProgramme
        dpListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SisuNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        refreshList();
           
        // add listeners
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.filterItems(newValue);
        });
        
        sortToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.setSortingOrder(newValue);
            sortToggleButton.setText(newValue ? "A-Z" : "Z-A");
        });

        // Listen for item selection
        dpListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {     
                viewModel.handleSelectedDegreeProgramme(newValue);
            }
        });  
        
        // register refreshButton to refresh the degree programme list
        refreshButton.setOnAction(event -> refreshList());

        // register clearFilterButton to clear the text in filterTextField
        clearFilterButton.setOnAction(event -> filterTextField.clear());

        // cancel button returns to the previous screen
        cancelButton.setOnAction(event -> viewModel.onCancelButtonPressed());

        // select button tells the viewmodel which degreeprogramme was selected
        selectStudyProgramButton.setOnAction(event -> viewModel.onSelectStudyProgramButtonPressed());

        // TODO display some kind of loading screen while the degreeprogrammes are being fetched from the API
        
    }

    public void refreshList() {
        viewModel.loadDegreeProgrammes();
        // set the items in the listview to the items in the model
        dpListView.setItems(viewModel.sortedItemsProperty());
    }


}