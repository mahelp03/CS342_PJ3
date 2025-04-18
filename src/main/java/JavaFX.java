// JoonYoung Ma, Yebeen Seo
// CS342, Project 2
// Mar 21.2025

// Title: THE WEATEHR APP
// EXPLANATION:
//	This is app based on JAVAFX, and it displays main, daily weather, and three day forcast.
// Overally, this app consists with three parts, main, TheDayScene, TheThreeDayScene.
// 1. Main: 
//		Above the title there are two button, that lead to the rest of two Scenes.
//		And for Setting specific location, there is check box for typing the inputs(Grid X, Grid Y, WFO)
//		If user enters invalid input, or weather data is not existed, it will print Error message.
//		If user do not check the checkbox and choose the specific location, it wiil print Illinois chicago weather, as default value.
// 2. TheDayScene:
//		There are changable pictures, timeline , and temperagure, short description, long description. It also includes wind speed , direction, and precipation probabilty.
//		And as Background color is going to be changed depending on weather.
//
// 3. TheThreeDays:
//		In this part, it shows 6 boxes with 3 days and day & night each.
//		This part is quite similar with thedayscene overall, but its functions are more implemented than ThedayScene.
//		It contains more weather boxes and additionally contains the scrolling function to let users look at the scene easier.
//		
// In both Scenes, user can change the Specific Gridx, Gridy. But Region(WFO) is fixed, by using updated button.



import javafx.application.Application;

import javafx.scene.Scene;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import weather.Period;
import weather.WeatherAPI;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class JavaFX extends Application {
	// Labels
    private Label label1, label2, label3, label4, label5, label6, labela2, labela3, labela4, labela5, labela6;
	// 1 -> title, 2-> Specific Location, 3-> Grid X: , 4-> Grid Y, 5-.Error M, a6->long explanation
    private Button button1, button2, button4;
	// 1-> the day weather, 2-> the three day forcast 4-> rollback
    private TextField text1, text2, text3; // 1-> gridx, 2->gridY, a1-> cur_location in TheDayScene
    private CheckBox checkBox; // between default location or user specific location
    private HBox hbox1, hbox2, hbox3, hbox4, hboxa1, hboxa2; // 1->specific L, 2-> GridX, 3-> GridY 4-> WFO a1-> DayScene. data input session, a2-> DayScene.Image+rain,wind
    private VBox vbox1;
    private BorderPane pane, paneD, paneT; // pane-> main, paneD->Main, pandT -> Three days
	
	// given
	public static void main(String[] args) {

		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		//Title
		primaryStage.setTitle("CS342_Project2_JoonYoungMa, Yebeen Seo");

		// objects
		pane= new BorderPane();
		pane.setPadding(new Insets(20));
		label1 = new Label("Project2: Weather APP");
		button1 = new Button("The Day Weather");
		button2 = new Button("Three Day Forecast");
		checkBox = new CheckBox();
		label2 = new Label("Specific Location");
		label3 = new Label("Grid X:");
		label4 = new Label("Grid Y:");
		label6 = new Label(" WFO :");
		
		text1 = new TextField();
		text2 = new TextField();
		text3 = new TextField();
		text1.setDisable(true);
		text2.setDisable(true);
		text3.setDisable(true);
		label5 = new Label("");

		checkBox.setOnAction(e -> { // active text1, text2 when checkBox actived
			if (checkBox.isSelected()) {
				text1.setDisable(false);
				text2.setDisable(false);
				text3.setDisable(false);
			} else {
				text1.setDisable(true);
				text2.setDisable(true);
				text3.setDisable(true);
			}
		});
		// every Hboxes and Vboxes
		hbox1 = new HBox(10, checkBox, label2);
		hbox2 = new HBox(10, label3, text1);
		hbox3 = new HBox(10, label4, text2);
		hbox4 = new HBox(10, label6, text3);
		vbox1 = new VBox(10, label1, button1, button2, hbox1, hbox2, hbox3, hbox4);
		
		// main objects size, Fonts
		label1.setPrefSize(500, 120);
		label1.setPadding(new Insets(0,0,0,0));
		label1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 18));
		button1.setPrefSize(200, 40);
		button2.setPrefSize(200, 40);

		// set padding and locate on center
		vbox1 = new VBox(10, label1, button1, button2, hbox1, hbox2,hbox3,hbox4, label5);
		vbox1.setPadding(new Insets(20,230,50,240));
		pane.setCenter(vbox1);

		// create and set the main Scene
		Scene scene = new Scene(pane, 720,500);
		primaryStage.setScene(scene);
		primaryStage.show();

		// button 1(TheDayScene) active
		button1.setOnAction(e -> {
			try {
				int inputGridX, inputGridY; // it depends between user entered Specific values, if it empty -> chicago
				String inputWFO;

				// default "CHICAGO"
				if (!checkBox.isSelected()) {
					inputGridX = 74;
					inputGridY = 73;
					inputWFO = "LOT";
					
				} else {
					// confirm between value is valid or not
					try {
						inputGridX = Integer.parseInt(text1.getText().trim());
						inputGridY = Integer.parseInt(text2.getText().trim());
						inputWFO = text3.getText().trim();
					} catch (NumberFormatException ex) {
						// display error message
						label5.setText("Error: Please enter valid numbers!");
						label5.setStyle("-fx-text-fill: red;");
						return; 
					}
				}
				// open the daily weather Scene
				DailyScene(primaryStage, inputGridX, inputGridY, inputWFO); // for searching the data, I figured out weatherAPI required three parameters, GridX, GridY, Region.

			} catch (Exception ex) {
				label5.setText("Error: Please enter valid numbers!");
				label5.setStyle("-fx-text-fill: red;");
				ex.printStackTrace();
			}
		});
		
		// Button2 (move to TheThreeDayForecast)
		button2.setOnAction(e -> {
			try {
				int inputGridX, inputGridY; 
				String inputWFO;

				if (!checkBox.isSelected()) {
					// Default: Chicago
					inputGridX = 74;
					inputGridY = 73;
					inputWFO = "LOT";
				} else {
					try {
						inputGridX = Integer.parseInt(text1.getText().trim());
						inputGridY = Integer.parseInt(text2.getText().trim());
						inputWFO = text3.getText().trim();
					} catch (NumberFormatException ex) {
						label5.setText("Error: Please enter valid numbers!");
						label5.setStyle("-fx-text-fill: red;");
						return;
					}
				}

				// Pass the location parameters correctly
				ThreeDayScene(primaryStage, inputGridX, inputGridY, inputWFO);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void DailyScene(Stage stage, int gridX, int gridY, String inputWFO) throws Exception {
		// pending text, sometimes when I typed some specific input, it occurred infinite loop or error
		// So, I wrote "On Progress..."
		System.out.println("On Progress...");
		// API calling // data call
		ArrayList<Period> forecast = WeatherAPI.getForecast(inputWFO, gridX, gridY);

		// API connectting fail
		if (forecast == null || forecast.isEmpty()) {
			System.err.println("Error: Failed!! ");
			return;
		}
		// API connecting success
		Period today = forecast.get(0);

		// Back Button
		button4 = new Button("<-");
		button4.setPrefSize(50, 30);
		button4.setOnAction(e -> {
			try {
				start(stage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		paneD = new BorderPane();

		// thedayScene objects
		Label labelGridX = new Label("Grid X:");
		TextField textGridX = new TextField(String.valueOf(gridX));

		Label labelGridY = new Label("Grid Y:");
		TextField textGridY = new TextField(String.valueOf(gridY));

		Button updateButton = new Button("Update");
		updateButton.setOnAction(e -> {
			try {
				// able to get new grid inputs coordinates from them,but WFO is fixed
				int newGridX, newGridY;
				
				// if the input is empty, reset as chicago location
				if (textGridX.getText().trim().isEmpty()) {
					newGridX = 74; // Default value for Chicago
				} else {
					newGridX = Integer.parseInt(textGridX.getText().trim()); // search
				}
		
				if (textGridY.getText().trim().isEmpty()) {
					newGridY = 73; // Default value for Chicago
				} else {
					newGridY = Integer.parseInt(textGridY.getText().trim()); // search
				}

				DailyScene(stage, newGridX, newGridY, inputWFO);  // Reload scene with new coordinates
			} catch (NumberFormatException ex) {
				System.err.println("Error: Invalid input. Please enter numbers.");
			} catch (Exception ex) { 
				ex.printStackTrace(); // catch unexpected errors that might occur
			}
		});

		hboxa1 = new HBox(10, labelGridX, textGridX, labelGridY, textGridY, updateButton);

		// Time in thedayScene
		labela2 = new Label(String.valueOf(today.startTime));
		labela2.setFont(Font.font("Times New Roman", FontWeight.BOLD, 14));

		// Weather Image, it gonna be changed depend on weather
		Image weatherImage = getWeatherIcon(today.shortForecast);
		ImageView weatherImageView = new ImageView(weatherImage);
		weatherImageView.setFitWidth(120);
		weatherImageView.setFitHeight(120);

		// Initail value for temperature
		labela3 = new Label(String.valueOf(today.temperature));
		labela3.setFont(Font.font("Times New Roman", FontWeight.BOLD, 50));

		// Letter symbol of temperature
		labela4 = new Label("°" + today.temperatureUnit);
		labela4.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
		labela4.setPadding(new Insets(0,170,0,0));
		
		Label labelT1 = new Label("Wind Speed:");
		labelT1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		Label labelT1_1 = new Label(today.windSpeed);
		labelT1_1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		HBox hboxT1 = new HBox(5, labelT1, labelT1_1);

		Label labelT2 = new Label("Wind Direction:");
		labelT2.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		Label labelT2_1 = new Label(today.windDirection);
		labelT2_1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		HBox hboxT2 = new HBox(5, labelT2, labelT2_1);

		Label labelT3 = new Label("Precipitation probability:");
		labelT3.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		Label labelT3_1 = new Label(today.probabilityOfPrecipitation.value + "%");
		labelT3_1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
		HBox hboxT3 = new HBox(5, labelT3, labelT3_1);

		VBox vboxT1 = new VBox(3, hboxT1, hboxT2, hboxT3);
		vboxT1.setStyle(getWeatherColor(today.shortForecast) + " -fx-border-color: darkGray; -fx-border-width: 2;");
		vboxT1.setPadding(new Insets(25,10,0,10));

		// hbox1 that includes outboundary objects
		hboxa2 = new HBox(5, weatherImageView, labela3, labela4, vboxT1);
		hboxa2.setPadding(new Insets(10));

		// short descriptions
		labela5 = new Label(today.shortForecast);
		labela5.setFont(Font.font("Times New Roman", FontWeight.BOLD, 18));

		// objects in boundary
		Text labela6 = new Text(today.detailedForecast);
		labela6.setStyle("-fx-font-size: 14px; -fx-font-family: Arial;");

		TextFlow textFlow = new TextFlow(labela6);
		textFlow.setMaxWidth(400);
		textFlow.setPrefHeight(Region.USE_COMPUTED_SIZE);

		// Final Box with Background Color
		VBox weatherBox = new VBox(10, hboxa2, labela5, textFlow);
		weatherBox.setPadding(new Insets(15));
		weatherBox.setPrefSize(550, 250);
		weatherBox.setStyle(getWeatherColor(today.shortForecast) + " -fx-border-color: black; -fx-border-width: 2;");

		// making final order in thedayScene
		VBox vboxa2 = new VBox(15, button4, hboxa1, labela2, weatherBox);
		vboxa2.setPadding(new Insets(20));

		paneD.setCenter(vboxa2);

		// Scene Setup
		Scene dailyScene = new Scene(paneD, 720, 500); // I changed same as Main
		stage.setScene(dailyScene);
	}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// get weather icons (png files)
	private Image getWeatherIcon(String forecast) {
		forecast = forecast.toLowerCase();
		String imagePath = "/WetCloudy.png";

		// Filter for short explanation, figure out the images
		if (forecast.contains("sunny") && forecast.contains("cloud")) {
			imagePath = "/Cloud&Sunny.png"; // D1FFBD (209,255,189)
		} else if (forecast.contains("sunny") || forecast.contains("clear")) {
			imagePath = "/sunny.png"; // FFFFC5 (255,255,197)
		} else if (forecast.contains("rain") && forecast.contains("slight")) {
			imagePath = "/SlightRainy.png"; // B0C4DE (176,196,222)
		} else if (forecast.contains("rain")) {
			imagePath = "/rainny.png"; // 95B9DB (149,185,219)
		} else if (forecast.contains("snow")) {
			imagePath = "/snowy.png"; // C7C7C7 (199,199,199)
		} else if (forecast.contains("thunder") || forecast.contains("lightning")) {
			imagePath = "/Thunder.png"; // CABBA8 (202,187,168)
		} else if (forecast.contains("wind")) {
			imagePath = "/windy.png"; // DCE7F3 (220,231,243)
		} else{
			imagePath = "/WetCloudy.png"; // A8B8C9 (168,184,201)
		}

		return new Image(getClass().getResource(imagePath).toExternalForm());
	}

	// fill the different color in boxes depending on each whether
	private String getWeatherColor(String forecast) {
		forecast = forecast.toLowerCase();
		
		if (forecast.contains("sunny") && forecast.contains("cloud")) {
			return "-fx-background-color: #D1FFBD;"; // Light green
		} else if (forecast.contains("sunny") || forecast.contains("clear")) {
			return "-fx-background-color: #FFFFC5;"; // Yellow
		} else if (forecast.contains("rain") && forecast.contains("slight")) {
			return "-fx-background-color: #B0C4DE;"; // Light blue
		} else if (forecast.contains("rain")) {
			return "-fx-background-color: #95B9DB;"; // Darker blue
		} else if (forecast.contains("snow")) {
			return "-fx-background-color: #C7C7C7;"; // Gray
		} else if (forecast.contains("thunder") || forecast.contains("lightning")) {
			return "-fx-background-color: #CABBA8;"; // Light brown
		} else if (forecast.contains("wind")) {
			return "-fx-background-color: #DCE7F3;"; // Light sky blue
		} else {
			return "-fx-background-color: #A8B8C9;"; // Default cloudy gray
		}
	}

	private void ThreeDayScene(Stage stage, int gridX, int gridY, String inputWFO) throws Exception {
		// fetch forecast data based on location
		ArrayList<Period> forecast = WeatherAPI.getForecast(inputWFO, gridX, gridY);
		
		// if weather data is invalid, error
		if (forecast == null || forecast.size() < 6) {
			System.err.println("Error: Failed to retrieve forecast data!");
			Label errorLabel = new Label("Error: Weather data not available.");
			VBox errorBox = new VBox(10, errorLabel);
			errorBox.setPadding(new Insets(20));
			paneT = new BorderPane();
			paneT.setCenter(errorBox);
			Scene errorScene = new Scene(paneT, 900, 500);
			stage.setScene(errorScene);
			return;
		}
	
		// Back Button
		Button backButton = new Button("<-");
		backButton.setPrefSize(50, 30);
		backButton.setOnAction(e -> {
			try {
				start(stage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		
		// 3 days forecast title
		Label title = new Label("3-Day Weather Forecast");
		title.setFont(Font.font("Times New Roman", FontWeight.BOLD, 22));
	
		// Grid X Input
		Label labelGridX = new Label("Grid X:");
		TextField textGridX = new TextField(String.valueOf(gridX));
		
		// Grid Y Input
		Label labelGridY = new Label("Grid Y:");
		TextField textGridY = new TextField(String.valueOf(gridY));
		
		// update button in the scene of 3 days forecast
		Button updateButton = new Button("Update Location");
		updateButton.setOnAction(e -> {
			try {
				int newGridX;
				int newGridY;
				
				// if Grid X not chosen, values default
				if (textGridX.getText().trim().isEmpty()) {
					newGridX = 74; // Default value for Chicago
				} else {
					newGridX = Integer.parseInt(textGridX.getText().trim()); // search
				}
				// if Grid Y not chosen, values default
				if (textGridY.getText().trim().isEmpty()) {
					newGridY = 73; // Default value for Chicago
				} else {
					newGridY = Integer.parseInt(textGridY.getText().trim()); // search
				}

				ThreeDayScene(stage, newGridX, newGridY, inputWFO); // Reload Scene
	
				} catch (Exception ex) { 
				ex.printStackTrace();
			}
		});
	
		// HBox for Grid input and update button
		HBox locationBox = new HBox(10, labelGridX, textGridX, labelGridY, textGridY, updateButton);
		locationBox.setPadding(new Insets(10));
		
		// VBox to hold weather data
		VBox forecastBox = new VBox(12);
		forecastBox.setPadding(new Insets(10, 30, 10, 30));
		forecastBox.getChildren().add(title);
		
		// date format
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		// loop of weather data and create 'day' and 'night' boxes
		for (int i = 0; i < 6; i += 2) {
			Period day = forecast.get(i);
			Period night = forecast.get(i + 1);
	
			// date label
			Label dateLabel = new Label(dateFormat.format(day.startTime));
			dateLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 16));
	
			// create day/night weather boxes
			VBox dayBox = createDetailedWeatherBox("[Day]", day);
			VBox nightBox = createDetailedWeatherBox("[Night]", night);
	
			// new HBox for spacing to look better
			HBox dailyForecastBox = new HBox(15, dayBox, nightBox);
			dailyForecastBox.setPadding(new Insets(8));
	
			forecastBox.getChildren().addAll(dateLabel, dailyForecastBox);
		}
	
		// Scroll function
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(forecastBox);
		scrollPane.setFitToWidth(true);  // expand horizontally
		scrollPane.setPannable(true);  // let users drag with the mouse
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // show scrollbar
	
		// VBox to hold all elements in the scene
		VBox mainBox = new VBox(12, backButton, locationBox, scrollPane);
		mainBox.setPadding(new Insets(10));
		
		// set scene
		paneT = new BorderPane();
		paneT.setCenter(mainBox);
	
		Scene threeDayScene = new Scene(paneT, 900, 500);
		stage.setScene(threeDayScene);
	}
	
	// function to create each weather boxes
	private VBox createDetailedWeatherBox(String timeOfDay, Period period) {
		
		// time labels of 'day' and 'night'
		Label timeLabel = new Label(timeOfDay);
		timeLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 14));
		
		// weather icon
		ImageView imageView = new ImageView(getWeatherIcon(period.shortForecast));
		imageView.setFitWidth(80);
		imageView.setFitHeight(80);

		// wind speed information showing
		Label labelT1 = new Label("Wind Speed:");
		labelT1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		Label labelT1_1 = new Label(period.windSpeed);
		labelT1_1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		HBox hboxT1 = new HBox(5, labelT1, labelT1_1);

		// wind derection information showing
		Label labelT2 = new Label("Wind Direction:");
		labelT2.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		Label labelT2_1 = new Label(period.windDirection);
		labelT2_1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		HBox hboxT2 = new HBox(5, labelT2, labelT2_1);

		// precipitation probability information showing
		Label labelT3 = new Label("Precipitation probability:");
		labelT3.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		Label labelT3_1 = new Label(period.probabilityOfPrecipitation.value + "%");
		labelT3_1.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		HBox hboxT3 = new HBox(5, labelT3, labelT3_1);

		// VBox and HBox to hold weather info details
		VBox vboxT1 = new VBox(3, hboxT1, hboxT2, hboxT3);
		HBox hboxTH = new HBox(35, imageView, vboxT1);
		
		// Temperature showing
		Label tempLabel = new Label("Temp: " + period.temperature + "°" + period.temperatureUnit);
		tempLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
		
		// forecast showing
		Label forecastLabel = new Label(period.shortForecast);
		forecastLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 12));
	
		// TextFlow to make sure the texts are fit in the boxes
		Text labela6 = new Text(period.detailedForecast);
		labela6.setFont(Font.font("Arial", 12));
		TextFlow textFlow = new TextFlow(labela6);
		textFlow.setMaxWidth(340);
		textFlow.setPrefHeight(Region.USE_COMPUTED_SIZE);
		
		// weather boxes' composition
		VBox weatherBox = new VBox(8, timeLabel, hboxTH, tempLabel, forecastLabel, textFlow);
		weatherBox.setPadding(new Insets(10));
		weatherBox.setPrefWidth(380);
		weatherBox.setPrefHeight(180);
		weatherBox.setStyle(getWeatherColor(period.shortForecast) + " -fx-border-color: black; -fx-border-width: 1;");
	
		return weatherBox;
	}

}