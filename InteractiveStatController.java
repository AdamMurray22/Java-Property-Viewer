import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.fxml.FXML;
import java.util.stream.Collectors;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.Arrays;
/**
 * InteractiveStatController controls the interactive statistics where the user can find the closest destinations depending on the selected borough and property name as a reference point
 * 
 * It populates the comboboxes with the boroughs and properties from the price range selected in the main pane combo boxes, allowing the user to find the five closest pubs or tourist
 * attractions to the selected porperty. 
 * 
 *
 * @author Adam Murray (K21003575)
 * @author Augusto Favero (K21059800)
 * @author Mathew Tran (K21074020)
 * @author Tony Smith (K21064940)
 * @version (v1)
 */
public class InteractiveStatController extends Controller
{
    @FXML public HBox comboBoxContainer;
    
    @FXML public ComboBox<String> boroughs;
    @FXML public ComboBox<String> propertyName;
    @FXML public ComboBox<String> price;
    
    @FXML public TableView<InteractiveStatsTableValues> locationsResult;
    @FXML private TableColumn<InteractiveStatsTableValues, String> nameColumn;
    @FXML private TableColumn<InteractiveStatsTableValues, String> addressColumn;
    @FXML private TableColumn<InteractiveStatsTableValues, String> distanceColumn;
    
    private ArrayList<DestinationListing> destinations;
    private ArrayList<DistanceDestinationPair> fiveClosestDestinations;
    private DestinationDistances desCalculator;
    private ArrayList<AirbnbListing> filteredListing;
    
    private DestinationType desType;
    
    /**
     * Creates the table.
     */
    @FXML
    public void initialize()
    {
        createTable();
        setOnRowClicked();
    }
    
    /*
     * Assigns what fields will go in which row.
     */
    private void createTable()
    {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
    }
    
    /*
     * Defines what happens when a row is clicked in the row factory
     */
    private void setOnRowClicked()
    {
        locationsResult.setRowFactory(e -> tableClicked());
    }

    /*
     * Defines what happens when a row is clicked.
     */
    private TableRow<InteractiveStatsTableValues> tableClicked()
    {
        TableRow<InteractiveStatsTableValues> row = new TableRow<>();
        row.setOnMouseClicked(event -> rowClicked(row));
        return row;
    }

    /*
     * Creates a destination window for the destination in the row clicked.
     */
    private void rowClicked(TableRow<InteractiveStatsTableValues> row)
    {
        if (! row.isEmpty()) {
            InteractiveStatsTableValues listing = row.getItem();
            DestinationDetailsFactory.getDestinationDetailFactory().newDestinationDetail(listing.getDistanceDestinationPair().getDestination());
        }
    }
    
    /*
     * Update of the combo boxes' contents when a user interaction occurs that requires the values for the combo boxes to be altered
     */
    public void updateBoxes(List<AirbnbListing> filteredListing, List<DestinationListing> typesDestinations, DestinationType desType)
    {
        this.desType = desType;
        createNewComboBoxes();
        locationsResult.getItems().clear();
        //reset boxes first to be sure
        
        this.filteredListing = new ArrayList<>(filteredListing);
        destinations = new ArrayList<>(typesDestinations);
        
        List<String> boroughsList = ListingProcessor.getBoroughs(filteredListing);
        
        boroughs.getItems().addAll(boroughsList);
        boroughs.setPromptText("Select Borough Name:");
        propertyName.setPromptText("Select Property:");
        setUpPriceBox(desType);    
        propertyName.setDisable(true);
        price.setDisable(true);
    }
    
    /*
     * Sets up the price combo box values, this depends on the DestinationType, for pubs the price is evaluated using the � metric whilst for the tourist attractions the ticket prices are used
     * @param DestinationType , either .PUB or .ATTRACTION
     */
    private void setUpPriceBox(DestinationType desType)
    {
         if(desType.equals(DestinationType.PUB)){
            price.setPromptText("Pub Price Range");
            PubPrice pubPricings = new PubPrice();
            price.getItems().addAll(pubPricings.getPrices());
        }else if(desType.equals(DestinationType.ATTRACTION)){
            price.setPromptText("Ticket Price");
            AttractionPrice attractionPrices = new AttractionPrice();
            price.getItems().addAll(attractionPrices.getPrices());
        }
    }
    
    /*
     * Creates the new combo boxes.
     */
    private void createNewComboBoxes()
    {
        createNewBoroughComboBox();
        createNewPropertyComboBox();
        createNewPriceComboBox();
    }
    
    /*
     * Creates a new Combo box for the borough.
     */
    private void createNewBoroughComboBox()
    {
        comboBoxContainer.getChildren().remove(boroughs);
        boroughs = new ComboBox<String>();
        boroughs.setOnAction(e -> processBoroughsBox());
        boroughs.setPrefWidth(150);
        comboBoxContainer.getChildren().add(0, boroughs);
    }
    
    /*
     * Creates a new Combo box for the property.
     */
    private void createNewPropertyComboBox()
    {
        comboBoxContainer.getChildren().remove(propertyName);
        propertyName = new ComboBox<String>();
        propertyName.setOnAction(e -> processPropertiesBox());
        propertyName.setPrefWidth(150);
        comboBoxContainer.getChildren().add(1, propertyName);
    }

    /*
     * Creates a new Combo box for the price.
     */
    private void createNewPriceComboBox()
    {
        comboBoxContainer.getChildren().remove(price);
        price = new ComboBox<String>();
        price.setOnAction(e -> processPriceBox());
        price.setPrefWidth(150);
        comboBoxContainer.getChildren().add(2, price);
    }

    /*
     * method called when the boroughs comboboxes' values are changed, the relevant data is added to the property combo box which holds all the properties that exist under the borough name selected and in the price range
     * selected in the main pane
     */
    @FXML
    private void processBoroughsBox()
    {
       if(boroughs.getValue() != null){
           List<String> properties = ListingProcessor.getPropertiesNameInBorough(filteredListing, boroughs.getValue());
                                                                                          
        createNewPropertyComboBox();
        propertyName.setPromptText("Select Property");
        propertyName.getItems().addAll(properties);
        propertyName.setDisable(false);
        createNewPriceComboBox();
        price.setDisable(true);
        setUpPriceBox(desType);
        locationsResult.getItems().clear();
    
        checkBoxes(boroughs.getValue(), propertyName.getValue(), price.getValue());
       }
    }
    
    /*
     * method called when the properites comboboxes' values are changed, the relevant data is added to the price combo box which holds valid prices that can be selected depedending on the type of destinations the interactive statistic
     * represents
     */
    @FXML 
    private void processPropertiesBox()
    {
        if(propertyName.getValue() != null){
            price.setDisable(false);
            checkBoxes(boroughs.getValue(), propertyName.getValue(), price.getValue());
        }
    }
    
    /*
     * method called when the price combo box values are changed/ selected. displaying the destinations that fit the user requested preferences
     */
    @FXML 
    private void processPriceBox()
    {
        checkBoxes(boroughs.getValue(), propertyName.getValue(), price.getValue()); 
    }
    
    /*
     * main method that retrieves the closest destinations based on the selected values from the three combo boxes
     * @param String boroughSelected, the borough selected in the first combo box
     * @param String propertySelected, the property selected by the user in the second combo box
     * @param String priceSelected, the price selected by the user in the third combo box
     */
    private void checkBoxes(String boroughSelected, String propertySelected,String priceSelected)
    {
        if(boroughSelected != null && propertySelected != null && priceSelected != null){
           List<DestinationListing> filteredDestinations = ListingProcessor.filterDestinations(destinations,boroughSelected,priceSelected);
                       
           AirbnbListing selectedProperty = ListingProcessor.getPropertyListingByNames(filteredListing, propertySelected, boroughSelected);
        
           //calculator to calculate all the distances for all destinations that have been filtered in relation to the selectedProperty
           desCalculator = new DestinationDistances(filteredDestinations, selectedProperty);
           //method to retrieve all the five closest destinations from all the destinations that fit the user preferences
           fiveClosestDestinations = new ArrayList<DistanceDestinationPair>();
           fiveClosestDestinations = desCalculator.getFiveSmallest();
           displayResult();
        }
    }
    
    /*
     * Displays the 5 closest destinations in a table
     */
    private void displayResult()
    {
        locationsResult.getItems().clear();
        for(DistanceDestinationPair eachDestination: fiveClosestDestinations){
            InteractiveStatsTableValues value = new InteractiveStatsTableValues(eachDestination);
            locationsResult.getItems().add(value);
        } 
    }
    
    /**
     * Resets the comboxes and the table using the values already stored.
     */
    @FXML
    public void updateBoxesButtonPressed()
    {
        updateBoxes(filteredListing, destinations, desType);
    }
} 

