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

package org.gillius.jfxutils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import static org.gillius.jfxutils.JFXUtil.*;

/**
 * ChartZoomManager manages a zooming selection rectangle and the bounds of the graph. It can be
 * enabled via {@link #start()} and disabled via {@link #stop()}. The normal usage is to create a
 * StackPane with two children, an XYChart type and a Rectangle. The Rectangle should start out
 * invisible and have mouseTransparent set to true. If it has a stroke, it should be of INSIDE
 * type to be pixel perfect.
 * <p/>
 * You can also use {@link JFXUtil#setupZooming(XYChart)} for a default solution.
 * <p/>
 * Three types of zooming are supported:
 * <ul>
 *   <li>Free-form zooming in the plot area on both axes</li>
 *   <li>X-axis only zooming by dragging in the x-axis</li>
 *   <li>Y-axis only zooming by dragging in the y-axis</li>
 * </ul>
 * <p/>
 * A lot of code in ChartZoomManager currently assumes there are no scale or rotate
 * transforms between the chartPane and the axes and plot area. However, all translation transforms,
 * layoutX/Y changes, padding, margin, and setTranslate issues should be OK. This might be improved
 * later, for example JavaFX 8 is rumored to allow transform multiplication, which could solve this.
 * <p/>
 * Example FXML to create the components used by this class:
 * <pre>
&lt;StackPane fx:id="chartPane" alignment="CENTER"&gt;
  &lt;LineChart fx:id="chart" animated="false" legendVisible="false"&gt;
    &lt;xAxis&gt;
      &lt;NumberAxis animated="false" side="BOTTOM" /&gt;
    &lt;/xAxis&gt;
    &lt;yAxis&gt;
      &lt;NumberAxis animated="false" side="LEFT" /&gt;
    &lt;/yAxis&gt;
  &lt;/LineChart&gt;
  &lt;Rectangle fx:id="selectRect" fill="DODGERBLUE" height="0.0" mouseTransparent="true"
             opacity="0.3" stroke="#002966" strokeType="INSIDE" strokeWidth="3.0" width="0.0"
             x="0.0" y="0.0" StackPane.alignment="TOP_LEFT" /&gt;
&lt;/StackPane&gt;</pre>
 *
 * Example Java code in bound controller class:
 * <pre>
ChartZoomManager zoomManager = new ChartZoomManager( chartPane, selectRect,
                                                     (ValueAxis&lt;?&gt;) chart.getXAxis(),
                                                     (ValueAxis&lt;?&gt;) chart.getYAxis() );
zoomManager.start();</pre>
 *
 * @author Jason Winnebeck
 */
public class ChartZoomManager {
	private final SimpleDoubleProperty rectX = new SimpleDoubleProperty();
	private final SimpleDoubleProperty rectY = new SimpleDoubleProperty();
	private final SimpleBooleanProperty selecting = new SimpleBooleanProperty( false );

	private static enum SelectMode { Horizontal, Vertical, Both }

	private SelectMode selectMode;

	private final EventHandlerManager handlerManager;

	private final Pane chartPane;
	private final Rectangle selectRect;
	private final ValueAxis<?> xAxis;
	private final ValueAxis<?> yAxis;

	/**
	 * Construct a new ChartZoomManager. See {@link ChartZoomManager} documentation for normal usage.
	 *
	 * @param chartPane  A Pane which is the ancestor of all arguments
	 * @param selectRect A Rectangle whose layoutX/Y makes it line up with the chart
	 * @param xAxis      ValueAxis
	 * @param yAxis      ValueAxis
	 */
	public ChartZoomManager( Pane chartPane, Rectangle selectRect, ValueAxis<?> xAxis,
	                         ValueAxis<?> yAxis ) {
		this.chartPane = chartPane;
		this.selectRect = selectRect;
		this.xAxis = xAxis;
		this.yAxis = yAxis;

		handlerManager = new EventHandlerManager( chartPane );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				onMousePressed( mouseEvent );
			}
		} );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				onMouseDragged( mouseEvent );
			}
		} );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				onMouseReleased();
			}
		} );
	}

	/**
	 * Start managing zoom management by adding event handlers and bindings as appropriate.
	 */
	public void start() {
		handlerManager.addAllHandlers();

		selectRect.widthProperty().bind( rectX.subtract( selectRect.translateXProperty() ) );
		selectRect.heightProperty().bind( rectY.subtract( selectRect.translateYProperty() ) );
		selectRect.visibleProperty().bind( selecting );
	}

	/**
	 * Stop managing zoom management by removing all event handlers and bindings, and hiding the
	 * rectangle.
	 */
	public void stop() {
		handlerManager.removeAllHandlers();
		selecting.set( false );
		selectRect.widthProperty().unbind();
		selectRect.heightProperty().unbind();
		selectRect.visibleProperty().unbind();
	}

	private void onMousePressed( MouseEvent mouseEvent ) {
		double x = mouseEvent.getX();
		double y = mouseEvent.getY();

		Rectangle2D plotArea = getPlotArea();

		if ( plotArea.contains( x, y ) ) {
			selectRect.setTranslateX( x );
			selectRect.setTranslateY( y );
			rectX.set( x );
			rectY.set( y );
			selecting.set( true );
			selectMode = SelectMode.Both;

		} else if ( getXAxisArea().contains( x, y ) ) {
			selectRect.setTranslateX( x );
			selectRect.setTranslateY( plotArea.getMinY() );
			rectX.set( x );
			rectY.set( plotArea.getMaxY() );
			selecting.set( true );
			selectMode = SelectMode.Horizontal;

		} else if ( getYAxisArea().contains( x, y ) ) {
			selectRect.setTranslateX( plotArea.getMinX() );
			selectRect.setTranslateY( y );
			rectX.set( plotArea.getMaxX() );
			rectY.set( y );
			selecting.set( true );
			selectMode = SelectMode.Vertical;
		}
	}

	private void onMouseDragged( MouseEvent mouseEvent ) {
		if ( !selecting.get() )
			return;

		Rectangle2D plotArea = getPlotArea();

		if ( selectMode == SelectMode.Both || selectMode == SelectMode.Horizontal ) {
			double x = mouseEvent.getX();
			//Clamp to the selection start
			x = Math.max( x, selectRect.getTranslateX() );
			//Clamp to plot area
			x = Math.min( x, plotArea.getMaxX() );
			rectX.set( x );
		}

		if ( selectMode == SelectMode.Both || selectMode == SelectMode.Vertical ) {
			double y = mouseEvent.getY();
			//Clamp to the selection start
			y = Math.max( y, selectRect.getTranslateY() );
			//Clamp to plot area
			y = Math.min( y, plotArea.getMaxY() );
			rectY.set( y );
		}
	}

	private void onMouseReleased() {
		if ( !selecting.get() )
			return;

		//Prevent a silly zoom... I'm still undecided about && vs ||
		if ( selectRect.getWidth() == 0.0 ||
				 selectRect.getHeight() == 0.0 ) {
			selecting.set( false );
			return;
		}

		double xStart = getXShift( xAxis, chartPane );
		double xStartVal =
				xAxis.getValueForDisplay( selectRect.getTranslateX() - xStart ).doubleValue();
		double xEndVal = xAxis.getValueForDisplay( rectX.get() - xStart ).doubleValue();

		double yStart = getYShift( yAxis, chartPane );
		double yStartVal =
				yAxis.getValueForDisplay( selectRect.getTranslateY() - yStart ).doubleValue();
		double yEndVal = yAxis.getValueForDisplay( rectY.get() - yStart ).doubleValue();

//		System.out.printf( "Zooming, x=[%f, %f] y=[%f, %f]%n",
//		                   xStartVal, xEndVal,
//		                   yStartVal, yEndVal );

		xAxis.setAutoRanging( false );
		xAxis.setLowerBound( xStartVal );
		xAxis.setUpperBound( xEndVal );
		yAxis.setAutoRanging( false );
		yAxis.setLowerBound( yEndVal );
		yAxis.setUpperBound( yStartVal );

		selecting.set( false );
	}

	/**
	 * Returns true if the given x and y coordinate in the chartPane's coordinate system is in the
	 * chart's plot area, based on the xAxis and yAxis locations. This method works regardless of
	 * the started/stopped state.
	 */
	public boolean isInPlotArea( double x, double y ) {
		return getPlotArea().contains( x, y );
	}

	/**
	 * Returns the plot area in the chartPane's coordinate space. This method works regardless of
	 * the started/stopped state.
	 */
	public Rectangle2D getPlotArea() {
		double xStart = getXShift( xAxis, chartPane );
		double yStart = getYShift( yAxis, chartPane );

		//If the direct method to get the width (which is based on its Node dimensions) is not found to
		//be appropriate, an alternative method is commented.
//		double width = xAxis.getDisplayPosition( xAxis.toRealValue( xAxis.getUpperBound() ) );
		double width = xAxis.getWidth();
//		double height = yAxis.getDisplayPosition( yAxis.toRealValue( yAxis.getLowerBound() ) );
		double height = yAxis.getHeight();

		return new Rectangle2D( xStart, yStart, width, height );
	}

	/**
	 * Returns the X axis area in the chartPane's coordinate space. This method works regardless of
	 * the started/stopped state.
	 */
	public Rectangle2D getXAxisArea() {
		double xStart = getXShift( xAxis, chartPane );
		double yStart = getYShift( xAxis, chartPane );

		return new Rectangle2D( xStart, yStart, xAxis.getWidth(), xAxis.getHeight() );
	}

	/**
	 * Returns the Y axis area in the chartPane's coordinate space. This method works regardless of
	 * the started/stopped state.
	 */
	public Rectangle2D getYAxisArea() {
		double xStart = getXShift( yAxis, chartPane );
		double yStart = getYShift( yAxis, chartPane );

		return new Rectangle2D( xStart, yStart, yAxis.getWidth(), yAxis.getHeight() );
	}
}
