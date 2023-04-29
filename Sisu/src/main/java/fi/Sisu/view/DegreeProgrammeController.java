package fi.Sisu.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import fi.Sisu.app.Constants;
import fi.Sisu.model.Course;
import fi.Sisu.model.SisuNode;
import fi.Sisu.utils.MyJavaFXUtils;
import fi.Sisu.viewmodel.DegreeProgrammeViewModel;

import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Controller for curriculum screen.
 * 
 * @author Antti Hakkarainen
 */
public class DegreeProgrammeController extends Controller {

    // Used to determine if the label is a course or a module
    public static enum LabelType {
        NONE, COURSE, MODULE
    }

    // Custom HBox class used to store the groupids of the course in the ListView
    protected class ListItemHBox extends HBox {
        private CheckBox checkBox;
        private Label label;
        private String moduleGroupId;
        private String courseGroupId;
        private Boolean isCourseMandatory;

        /**
         * Constructor for ListItemHBox
         * 
         * @param checkBox CheckBox for the course
         * @param label Label for the course's name
         * @param moduleGroupId String moduleGroupId for the course
         * @param courseGroupId String course's groupId
         * @param isCourseMandatory Boolean is the course mandatory part of the module
         */
        public ListItemHBox(
            CheckBox checkBox,
            Label label,
            String moduleGroupId,
            String courseGroupId,
            Boolean isCourseMandatory
        ) {
            this.checkBox = checkBox;
            this.label = label;
            this.moduleGroupId = moduleGroupId;
            this.courseGroupId = courseGroupId;
            this.isCourseMandatory = isCourseMandatory;

            this.getChildren().addAll(checkBox, label);
        }

        public boolean isCheckBoxSelected() {
            return checkBox.isSelected();
        }

        public boolean isCourseMandatory() {
            return isCourseMandatory;
        }  

        public Label getLabel() {
            return label;
        }

        public String getModuleGroupId() {
            return moduleGroupId;
        }

        public String getCourseGroupId() {
            return courseGroupId;
        }
    }

    // Custom HBox class used to store the groupIds of the course in the TreeView
    protected class TreeNodeHBox extends HBox {

        private String courseGroupId;  
        private String moduleGroupId;
        private LabelType type;
        private Label label;

        /**
         * Custom HBox class used to construct a HBox containing an Optional image
         * and the course name Label. Also stores other information which is needed
         * when user interacts with the TreeView.
         * 
         * @param text String course name
         * @param moduleGroupId String moduleGroupId 
         * @param courseGroupId String course's groupId
         * @param type LabelType is the label a course or a module
         * @param courseGrade Optional<String> course Grade
         * @param imageView Optional<ImageView> image for completed course 
         */
        public TreeNodeHBox(
            String text, 
            String moduleGroupId,
            String courseGroupId,
            LabelType type,
            Optional<String> courseGrade
        ) {
            this.courseGroupId = courseGroupId;
            this.moduleGroupId = moduleGroupId;
            this.type = type;
            this.label = new Label();
            
            // Try to make the labels fit the parent node's width, and wrap text to multiple rows
            label.setWrapText(true);
            label.maxWidthProperty().bind(degreeProgrammeTreeView.widthProperty().subtract(120));
            label.setText(courseGrade.map(grade -> "[" + grade + "] " + text).orElse(text));     

            // "Achievement" image for completed courses
            if (courseGrade.isPresent()) {
                Image image = new Image(getClass().getResourceAsStream("/fi/Sisu/view/achievement.png"));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                this.getChildren().add(imageView);
            }            
            this.getChildren().add(label);                       
        }
    
        public String getCourseGroupId() {
            return courseGroupId;
        }

        public Label getLabel() {
            return label;
        }

        public String getModuleGroupId() {
            return moduleGroupId;
        }

        public LabelType getType() {
            return type;
        }
    }

    private static final String HBOX_STYLE = 
        "-fx-background-color: %s;" +
        " -fx-padding: 5;" +
        " -fx-border-color: black;" +
        " -fx-border-width: 1;" +
        " -fx-border-radius: 5;" +
        " -fx-background-radius: 5;";

    // Links .fxml elements to the Controller
    @FXML
    VBox mainContainer;

    @FXML
    TreeView<HBox> degreeProgrammeTreeView;

    @FXML
    ListView<ListItemHBox> moduleCoursesListView;

    @FXML
    WebView courseDetailsWebView;

    @FXML
    ComboBox<String> courseGradeComboBox;

    @FXML
    Button selectCoursesButton;

    @FXML
    Button saveModuleChangesButton;

    @FXML
    Button addCourseButton;

    @FXML
    Button removeCourseButton;   

    @FXML
    Button saveCourseGradeButton;

    @FXML
    Button removeCourseGradeButton;

    private String selectedModule;
    private DegreeProgrammeViewModel viewModel;

    /**
     * * Construcor to store private variables and set the binds and listening
     * actions
     * to this controller.
     * 
     * @param model
     */
    public DegreeProgrammeController(DegreeProgrammeViewModel viewModel) {
        this.viewModel = viewModel;    

    }

    @FXML
    public void initialize() { 
        // Create a treeview from the StudyProgramme's data
        populateTreeView();

        // Set what happens when user presses buttons
        setButtonOnAction();   

        // Bind listenrs between view and (viewmodel, treeview, listview)
        setListeners();

        // Initially diable buttons, because user has made no selections
        addCourseButton.setDisable(true);
        removeCourseButton.setDisable(true);  
        selectCoursesButton.setDisable(true);              
    }


    private void setListeners() {
        // Bind the courseDescription property to the WebView
        viewModel.courseProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                WebEngine webEngine = courseDetailsWebView.getEngine();             
                webEngine.loadContent(MyJavaFXUtils.formatCourseHTML(newValue)); 
                populateGradeComboBoxValues(newValue);
            }
        });   

        /** 
         * Bind the selected treeview label's groupId to the viewModel
         * Additionally set the addCourseButton's visibility based on the
         * selected label's type. 
         */
        degreeProgrammeTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String courseGroupId = extractCourseGroupIdFromSelectedTreeItem();
            viewModel.handleSelectedCourseGroupId(courseGroupId);
        
            if (newValue != null) {
                TreeNodeHBox selectedLabel = (TreeNodeHBox) newValue.getValue();
                LabelType labelType = selectedLabel.getType();
        
                if (labelType == LabelType.MODULE) {
                    addCourseButton.setDisable(false);
                    removeCourseButton.setDisable(true);
                    selectCoursesButton.setDisable(false);      
                } else if (labelType == LabelType.COURSE) {
                    addCourseButton.setDisable(true);
                    removeCourseButton.setDisable(false);
                    selectCoursesButton.setDisable(true);      
                }
            } else {
                addCourseButton.setDisable(true);
                removeCourseButton.setDisable(true);
                selectCoursesButton.setDisable(true);      
            }
        });


        // Update treeview when student's degreeprogramme data changes
        viewModel.studentDataChangedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {
                populateTreeView();
                viewModel.isStudentDataChanged(false); // TODO maybe use appstate's student data changed for this..
            }
        });

        // When listview comes visible, populate it with module's courses
        moduleCoursesListView.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    populateModuleCoursesListView(selectedModule);
                } else {
                    moduleCoursesListView.getItems().clear();
                }
            }
        });

        // Asks the viewmodel to change the webview's course description to the selected course
        moduleCoursesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String courseGroupId = extractCourseGroupIdFromListItem();
            viewModel.handleSelectedCourseGroupId(courseGroupId);
        });
    }


    /**
     * Iterate through listview's checkboxes and add the selected courses,
     * which are not mandatory coures to the studymodule, to the hashset.
     * 
     * @return HashSet<String> of selected courseGroupIds
     */
    private HashSet<String> getChosenCourseGroupIds() {
        HashSet<String> chosenCourseGroupIds = new HashSet<String>();

        for (ListItemHBox listItem : moduleCoursesListView.getItems()) {
            if (listItem.isCheckBoxSelected() && listItem.isCourseMandatory() == false) {
                chosenCourseGroupIds.add(listItem.getCourseGroupId());
            }
        }

        return chosenCourseGroupIds;        
    }


    private void setButtonOnAction() {
        // Toggle between degreeprogramme's treeview and studymodule's listview 
        selectCoursesButton.setOnAction(
            event -> {
                // Order of the execution here is important!   
                selectedModule = extractModuleGroupIdFromSelectedTreeItem();
                selectCoursesButton.setVisible(false);
                saveModuleChangesButton.setVisible(true); 
                degreeProgrammeTreeView.setVisible(false);
                moduleCoursesListView.setVisible(true);  
                courseGradeComboBox.setDisable(true);
                removeCourseButton.setDisable(true);
                saveCourseGradeButton.setDisable(true);
                removeCourseGradeButton.setDisable(true);    
             });

        saveModuleChangesButton.setOnAction(
            event -> {   
                // Order of the execution here is important!             
                selectCoursesButton.setVisible(true);
                saveModuleChangesButton.setVisible(false);                              
                viewModel.onSaveModuleChangesButtonPressed(
                    selectedModule,
                    getChosenCourseGroupIds()
                );
                selectedModule = null;
                moduleCoursesListView.setVisible(false);
                degreeProgrammeTreeView.setVisible(true);  
                courseGradeComboBox.setDisable(false);
                removeCourseButton.setDisable(false);
                saveCourseGradeButton.setDisable(false);
                removeCourseGradeButton.setDisable(false);  
            });

        // Tells viewmodel that user pressed addCourseButton with the selected groupIds
        addCourseButton.setOnAction(
            event -> viewModel.onAddCourseButtonPressed(
                extractModuleGroupIdFromSelectedTreeItem(),
                extractCourseGroupIdFromSelectedTreeItem()
                )
            );

        // Tells viewmodel that user pressed removeCourseButton with the selected groupIds
        removeCourseButton.setOnAction(
            event -> viewModel.onRemoveCourseButtonPressed(
                extractModuleGroupIdFromSelectedTreeItem(),
                extractCourseGroupIdFromSelectedTreeItem()
                )
            );   

        // Tells viewmodel that user pressed removeCourseGradeButton with the selected groupIds
        removeCourseGradeButton.setOnAction(
            event -> viewModel.onRemoveCourseGradeButtonPressed(
                extractModuleGroupIdFromSelectedTreeItem(),
                extractCourseGroupIdFromSelectedTreeItem()
                )            
            );

        // Tells viewmodel that user pressed saveCourseGradeButton with the selected groupIds and grade
        saveCourseGradeButton.setOnAction(
            event -> viewModel.onSaveCourseGradeButtonPressed(
                extractModuleGroupIdFromSelectedTreeItem(),
                extractCourseGroupIdFromSelectedTreeItem(),
                courseGradeComboBox.getValue()
                )
            );   
    }


    /**
     * Generic method for extracting the group id from the selected item.
     * 
     * @param <T> Type of the selected item
     * @param selectedItem Selected item from which the group id is extracted
     * @param groupIdExtractor Function for extracting the group id from the selected item
     * @return String extracted groupId
     */
    private <T> String extractGroupId(T selectedItem, Function<T, String> groupIdExtractor) {
        if (selectedItem != null) {
            return groupIdExtractor.apply(selectedItem);
        }
        return "";
    }
    
    private String extractCourseGroupIdFromSelectedTreeItem() {
        TreeItem<HBox> selectedItem = degreeProgrammeTreeView.getSelectionModel().getSelectedItem();
        return extractGroupId(selectedItem, item -> ((TreeNodeHBox) item.getValue()).getCourseGroupId());
    }
    
    private String extractCourseGroupIdFromListItem() {
        ListItemHBox selectedItem = moduleCoursesListView.getSelectionModel().getSelectedItem();
        return extractGroupId(selectedItem, ListItemHBox::getCourseGroupId);
    }
    
    private String extractModuleGroupIdFromSelectedTreeItem() {
        TreeItem<HBox> selectedItem = degreeProgrammeTreeView.getSelectionModel().getSelectedItem();
        return extractGroupId(selectedItem, item -> ((TreeNodeHBox) item.getValue()).getModuleGroupId());
    }


    /**
     * Creates a recursive tree consisting of Labels which hold the module/course
     * name and module/course groupId. Name is displayed and groupId can be used
     * to display info about the course in the WebView.
     * 
     * @param rootItem TreeItem<Label> to be used as the parent of the node
     * @param node
     * @return
     */
    private TreeItem<HBox> createTreeRecursive(TreeItem<HBox> parentItem, SisuNode node) {
        HashSet<String> chosenCourses = node.getChosenCourses();
        HashSet<String> mandatoryCourses = node.getMandatoryCourses();

        // Iterate over the module's courses
        for (Course course : node.getChildCourses().values()) {
            if (chosenCourses.contains(course.getGroupId()) || mandatoryCourses.contains(course.getGroupId())) {
                
                // Create a new TreeItem for the course
                TreeItem<HBox> courseItem = createTreeItem(
                    course.getTargetCredits() + "op, " + course.getName(),
                    node.getGroupId(),
                    course.getGroupId(),
                    Constants.COLOR_MAUVE,
                    LabelType.COURSE,
                    course.getGrade() != null ? course.getGrade() : null
                );
                
                // Add the course item to the root item
                parentItem.getChildren().add(courseItem);
            }
        }

        // Iterate over child modules
        for (SisuNode childModule : node.getChildModules()) {
            // Create a new TreeItem for the child module
            TreeItem<HBox> childItem = 
                createTreeItem(
                    childModule.getTargetCredits() + "op, " + childModule.getName(),
                    childModule.getGroupId(),
                    "0", // module does not have a coursegroupid
                    Constants.COLOR_NEWS,
                    LabelType.MODULE,
                    null
                    );
    
            // Add the child item to the root item
            parentItem.getChildren().add(childItem);
    
            // Recursively create tree items for the child module's child nodes
            createTreeRecursive(childItem, childModule);   
        }

        return parentItem;
    }


    /**
     * Creates a TreeItem<Label> with the given name, groupId and color. Uses
     * custom Label class so we can store the groupId too.
     * 
     * @param name Name of the module/course
     * @param moduleGroupId String groupId of the module
     * @param courseGroupId String groupId of the course
     * @param color String hbox background color
     * @param type LabelType is this module or course?
     * @param grade String grade of the course
     * 
     * @return
     */
  
    private TreeItem<HBox> createTreeItem(
        String name,
        String moduleGroupId,
        String courseGroupId,
        String color,
        LabelType type,
        String grade
    ) {
        Optional<String> gradeOptional = Optional.ofNullable(grade);
    
        TreeNodeHBox hbox = 
            new TreeNodeHBox(
                name, 
                moduleGroupId, 
                courseGroupId, 
                type, 
                gradeOptional);

        TreeNodeHBox.setHgrow(hbox.getLabel(), Priority.ALWAYS);
        hbox.setStyle(String.format(HBOX_STYLE, color));        

        TreeItem<HBox> item = new TreeItem<>(hbox);
        item.setExpanded(true);        

        return item;
    }
    

    /**
     * Called during formatAndLoadCourseHTML, sets the grade combobox values,
     * and sets the default value to the course's grade if it exists.
     * 
     * @param course Course to be used to populate the combobox
     */
    private void populateGradeComboBoxValues(Course course) {         
        if (course.getGraded()) {
            courseGradeComboBox.setItems(FXCollections.observableArrayList(
                "5", "4", "3", "2", "1", "0"
            ));  
        }
        else {
            courseGradeComboBox.setItems(FXCollections.observableArrayList(
                "pass", "fail"
            ));
        }
    }


    /**
     * Populate the listview with selected module's all courses. User can 
     * toggle checkboxes to select which courses they want to include in their
     * degree programme.
     * 
     * @param moduleGroupId String of the module's groupId
     */
    private void populateModuleCoursesListView(String moduleGroupId) {
        SisuNode dp = viewModel.studyProgrammeProperty().get();
        SisuNode module = dp.findNodeInTree(moduleGroupId);

        HashMap<String, Course> courses = module.getChildCourses();
        HashSet<String> chosenCourses = module.getChosenCourses();
        HashSet<String> mandatoryCourses = module.getMandatoryCourses();

        // Iterate through the courses and add them to the listview
        for (Map.Entry<String, Course> entry : courses.entrySet()) {            
            Course course = entry.getValue();

            CheckBox checkBox = new CheckBox();             
            Label label = new Label(course.getTargetCredits() + "op " + course.getName());  
            Boolean isCourseMandatory = mandatoryCourses.contains(course.getGroupId());         

            if (chosenCourses.contains(course.getGroupId()) 
                || isCourseMandatory)
                {
                checkBox.setSelected(true);    
            }  
            /**
             * Some courses are a mandatory part of a studymodule. They can not
             * be deselected by the user, so they must be always selected and
             * always disabled so they can not be changed.
             */
            if (isCourseMandatory) {
                checkBox.setDisable(true);
            }                      

            ListItemHBox hbox = 
                new ListItemHBox(
                    checkBox,
                    label,
                    moduleGroupId, 
                    course.getGroupId(),
                    isCourseMandatory
                    );

            moduleCoursesListView.getItems().add(hbox);
        }
    }


    /**
     * Checks if the node has info, and then creates the root item for the tree.
     * Then recursively fills the tree with createTreeRecursive()
     * 
     * @param node SisuNode to be used as the root of the tree
     * @return TreeItem<Label> a full tree of the study programme
     */
    private void populateTreeView() {
        SisuNode dp = viewModel.studyProgrammeProperty().getValue();
        TreeItem<HBox> rootItem = new TreeItem<HBox>();

        if (dp == null) {
            rootItem = 
                createTreeItem(
                    "No Study programme selected for student.",
                    "0", 
                    "0",
                    "white",
                    LabelType.NONE,
                    null
                ); 
            degreeProgrammeTreeView.setRoot(rootItem);
            return;
        }
        
        // If the node is not null, create a root item and populate tree recursively
        rootItem = 
            createTreeItem(
                dp.getName(),
                dp.getGroupId(),
                "0", // module does not have a coursegroupid
                Constants.COLOR_ELECTRIC_VIOLET,
                LabelType.MODULE,
                null
                );    

        createTreeRecursive(rootItem, dp);        
        rootItem.setExpanded(true);
        degreeProgrammeTreeView.setRoot(rootItem);
    }
}
