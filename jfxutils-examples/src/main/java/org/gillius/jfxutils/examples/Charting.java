/*
 * Copyright 2013 Jason Winnebeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gillius.jfxutils.examples;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.gillius.jfxutils.JFXUtil;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.FixedFormatTickFormatter;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.gillius.jfxutils.chart.StableTicksAxis;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Charting extends Application {
	public static void main( String[] args ) {
		launch( args );
	}

	@FXML
	private LineChart<Number, Number> chart;

	@FXML
	private Slider valueSlider;

	@FXML
	private Label outputLabel;

	private XYChart.Series<Number, Number> series;

	private long startTime;

	private Timeline addDataTimeline;

	@FXML
	void addSample() {
		series.getData().add( new XYChart.Data<Number, Number>( System.currentTimeMillis() - startTime,
		                                                        valueSlider.getValue() ) );
	}

	@FXML
	void autoZoom() {
		chart.getXAxis().setAutoRanging( true );
		chart.getYAxis().setAutoRanging( true );
		//There seems to be some bug, even with the default NumberAxis, that simply setting the
		//auto ranging does not recompute the ranges. So we clear all chart data then re-add it.
		//Hopefully I find a more proper way for this, unless it's really bug, in which case I hope
		//it gets fixed.
		ObservableList<XYChart.Series<Number,Number>> data = chart.getData();
		chart.setData( FXCollections.<XYChart.Series<Number, Number>>emptyObservableList() );
		chart.setData( data );
	}

	@FXML
	void toggleAdd() {
		switch ( addDataTimeline.getStatus() ) {
			case PAUSED:
			case STOPPED:
				addDataTimeline.play();
				chart.getXAxis().setAutoRanging( true );
				chart.getYAxis().setAutoRanging( true );
				//Animation looks horrible if we're updating a lot
				chart.setAnimated( false );
				chart.getXAxis().setAnimated( false );
				chart.getYAxis().setAnimated( false );
				break;
			case RUNNING:
				addDataTimeline.stop();
				//Return the animation since we're not updating a lot
				chart.setAnimated( true );
				chart.getXAxis().setAnimated( true );
				chart.getYAxis().setAnimated( true );
				break;

			default:
				throw new AssertionError( "Unknown status" );
		}
	}

	@Override
	public void start( Stage stage ) throws Exception {
		FXMLLoader loader = new FXMLLoader( getClass().getResource( "Charting.fxml" ) );
		Region contentRootRegion = (Region) loader.load();

		StackPane root = JFXUtil.createScalePane( contentRootRegion, 960, 540, false );
		Scene scene = new Scene( root, root.getPrefWidth(), root.getPrefHeight() );
		stage.setScene( scene );
		stage.setTitle( "Charting Example" );
		stage.show();
	}

	@FXML
	void initialize() {
		startTime = System.currentTimeMillis();

		//Set chart to format dates on the X axis
		SimpleDateFormat format = new SimpleDateFormat( "HH:mm:ss" );
		format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		((StableTicksAxis) chart.getXAxis()).setAxisTickFormatter(
				new FixedFormatTickFormatter( format ) );

		series = new XYChart.Series<Number, Number>();
		series.setName( "Data" );

		chart.getData().add( series );

		addDataTimeline = new Timeline( new KeyFrame(
				Duration.millis( 250 ),
				new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent actionEvent ) {
						addSample();
					}
				}
		));
		addDataTimeline.setCycleCount( Animation.INDEFINITE );

		chart.setOnMouseMoved( new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				double xStart = chart.getXAxis().getLocalToParentTransform().getTx();
				double axisXRelativeMousePosition = mouseEvent.getX() - xStart;
				outputLabel.setText( String.format(
						"%d, %d (%d, %d); %d - %d",
						(int) mouseEvent.getSceneX(), (int) mouseEvent.getSceneY(),
						(int) mouseEvent.getX(), (int) mouseEvent.getY(),
						(int) xStart,
						chart.getXAxis().getValueForDisplay( axisXRelativeMousePosition ).intValue()
				) );
			}
		} );

		//Panning works via either secondary (right) mouse or primary with ctrl held down
		ChartPanManager panner = new ChartPanManager( chart );
		panner.setMouseFilter( new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( mouseEvent.getButton() == MouseButton.SECONDARY ||
						 ( mouseEvent.getButton() == MouseButton.PRIMARY &&
						   mouseEvent.isShortcutDown() ) ) {
					//let it through
				} else {
					mouseEvent.consume();
				}
			}
		} );
		panner.start();

		//Zooming works only via primary mouse button without ctrl held down
		JFXChartUtil.setupZooming( chart, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( mouseEvent.getButton() != MouseButton.PRIMARY ||
				     mouseEvent.isShortcutDown() )
					mouseEvent.consume();
			}
		} );
	}
}
