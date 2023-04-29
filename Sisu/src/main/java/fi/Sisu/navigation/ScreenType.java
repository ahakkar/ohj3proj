package fi.Sisu.navigation;

import fi.Sisu.view.Controller;
import fi.Sisu.view.DegreeProgrammeController;
import fi.Sisu.view.MainController;
import fi.Sisu.view.SearchCourseController;
import fi.Sisu.view.SelectDegreeProgrammeController;
import fi.Sisu.view.SelectStudentController;
import fi.Sisu.view.StudentController;
import fi.Sisu.viewmodel.DegreeProgrammeViewModel;
import fi.Sisu.viewmodel.MainViewModel;
import fi.Sisu.viewmodel.SearchCourseViewModel;
import fi.Sisu.viewmodel.SelectDegreeProgrammeViewModel;
import fi.Sisu.viewmodel.SelectStudentViewModel;
import fi.Sisu.viewmodel.StudentViewModel;
import fi.Sisu.viewmodel.ViewModel;

/**
 * ScreenType is an enum that contains all the screens in the application.
 * Each screen has a path to the .fxml file, a reference to the ViewModel and
 * a reference to the Controller. 
 * 
 * Add a new enum when adding a new screen. * 
 *
 * EXAMPLE_SCREEN(
 *    "/fi/Sisu/view/ExampleScreen.fxml",   // path to the fxml view file
 *    ExampleViewModel.class,       // reference to the associated ViewModel
 *    ExampleController.class),     // reference to the associated Controller
 *
 * @author Antti Hakkarainen
 */

public enum ScreenType {
    
    PREVIOUS_SCREEN(null, null, null),
    MAIN_SCREEN(
        "/fi/Sisu/view/MainScreen.fxml", 
        MainViewModel.class,
        MainController.class
        ),       

    CHOOSE_STUDENT_SCREEN(
        "/fi/Sisu/view/SelectStudentScreen.fxml",
        SelectStudentViewModel.class,
        SelectStudentController.class
        ),

    SEARCH_COURSE_SCREEN(
        "/fi/Sisu/view/SearchCourseScreen.fxml",
        SearchCourseViewModel.class,
        SearchCourseController.class
        ),

    STUDENT_SCREEN(
        "/fi/Sisu/view/StudentScreen.fxml",
        StudentViewModel.class,
        StudentController.class
        ),

    CURRICULUM_VIEW_SCREEN(
        "/fi/Sisu/view/DegreeProgrammeScreen.fxml",
        DegreeProgrammeViewModel.class,
        DegreeProgrammeController.class
        ), 
    SELECT_STUDY_PROGRAM_SCREEN(
        "/fi/Sisu/view/SelectDegreeProgrammeScreen.fxml",
        SelectDegreeProgrammeViewModel.class,
        SelectDegreeProgrammeController.class
        ), 
    // ADD NEW SCREEN ENUMS BELOW THIS LINE
    
    ; // AND ABOVE THIS LINE

    private final String viewPath;
    private final Class<? extends ViewModel> viewModelClass;
    private final Class<? extends Controller> controllerClass;

    ScreenType(String viewPath, Class<? extends ViewModel> viewModelClass, Class<? extends Controller> controllerClass) {
        this.viewPath = viewPath;
        this.viewModelClass = viewModelClass;
        this.controllerClass = controllerClass;
    }

    public String getViewPath() {
        return viewPath;
    }

    public Class<? extends ViewModel> getViewModelClass() {
        return viewModelClass;
    }

    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }
}