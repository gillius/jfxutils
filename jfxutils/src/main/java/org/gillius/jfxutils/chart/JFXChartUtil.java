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

package org.gillius.jfxutils.chart;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.gillius.jfxutils.JFXUtil;

/**
 * JFXChartUtil contains chart-related JavaFX utility methods used in the Gillius jfxutils project.
 *
 * @author Jason Winnebeck
 */
public class JFXChartUtil {
	/**
	 * Sets up zooming via the {@link #setupZooming(XYChart, EventHandler)} method by using the
	 * {@link ChartZoomManager}'s {@link ChartZoomManager#DEFAULT_FILTER default filter}.
	 */
	public static Region setupZooming( XYChart<?, ?> chart ) {
		return setupZooming( chart, ChartZoomManager.DEFAULT_FILTER );
	}

	/**
	 * Convenience method for simple and default setup of zooming on an {@link XYChart} via a
	 * {@link ChartZoomManager}. Wraps the chart in the components required to implement zooming. The
	 * current implementation wraps the chart in a StackPane, which has the chart and a blue
	 * translucent rectangle as children. Returns the top level of the created components.
	 * <p>
	 * If the chart already has a parent, that parent must be a {@link Pane}, and the chart is
	 * replaced with the wrapping region, and the return value could be ignored. If the chart does
	 * not have a parent, the same wrapping node is returned, which will need to be added to some
	 * parent.
	 * <p>
	 * The chart's axes must both be a type of ValueAxis.
	 * <p>
	 * The wrapping logic does not seem to be perfect, in fact there is a special case to handle
	 * {@link BorderPane}s. If it's not found to be reliable, then create the wrapping components
	 * yourself (such as in the FXML), or setup zooming before adding it to a parent.
	 *
	 * @param mouseFilter EventHandler that consumes events that should not trigger a zoom action
	 *
	 * @return The top-level Region
	 */
	public static Region setupZooming( XYChart<?, ?> chart,
	                                   EventHandler<? super MouseEvent> mouseFilter ) {
		StackPane chartPane = new StackPane();

		if ( chart.getParent() != null )
			JFXUtil.replaceComponent( chart, chartPane );

		Rectangle selectRect = new Rectangle( 0, 0, 0, 0 );
		selectRect.setFill( Color.DODGERBLUE );
		selectRect.setMouseTransparent( true );
		selectRect.setOpacity( 0.3 );
		selectRect.setStroke( Color.rgb( 0, 0x29, 0x66 ) );
		selectRect.setStrokeType( StrokeType.INSIDE );
		selectRect.setStrokeWidth( 3.0 );
		StackPane.setAlignment( selectRect, Pos.TOP_LEFT );

		chartPane.getChildren().addAll( chart, selectRect );

		ChartZoomManager zoomManager = new ChartZoomManager( chartPane, selectRect, chart );
		zoomManager.setMouseFilter( mouseFilter );
		zoomManager.start();
		return chartPane;
	}
}
