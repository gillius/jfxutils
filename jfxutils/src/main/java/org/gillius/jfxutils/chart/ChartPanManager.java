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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.gillius.jfxutils.EventHandlerManager;

/**
 * ChartPanManager manages drag gestures on an {@link XYChart} by translating them to panning
 * actions on the chart's axes.
 *
 * @author Jason Winnebeck
 */
public class ChartPanManager {
	/**
	 * The default mouse filter for the {@link ChartPanManager} filters events unless only primary
	 * mouse button (usually left) is depressed.
	 */
	public static final EventHandler<MouseEvent> DEFAULT_FILTER = ChartZoomManager.DEFAULT_FILTER;

	private final EventHandlerManager handlerManager;

	private final ValueAxis<?> xAxis;
	private final ValueAxis<?> yAxis;
	private final XYChartInfo chartInfo;

	private enum PanMode { Horizontal, Vertical, Both }

	private PanMode panMode;

	private EventHandler<? super MouseEvent> mouseFilter = DEFAULT_FILTER;

	private boolean dragging = false;

	private boolean wasXAnimated;
	private boolean wasYAnimated;

	private double lastX;
	private double lastY;

	public ChartPanManager(XYChart<?, ?> chart ) {
		handlerManager = new EventHandlerManager( chart );
		xAxis = (ValueAxis<?>) chart.getXAxis();
		yAxis = (ValueAxis<?>) chart.getYAxis();
		chartInfo = new XYChartInfo( chart, chart );

		handlerManager.addEventHandler( false, MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( passesFilter( mouseEvent ) )
					startDrag( mouseEvent );
			}
		} );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				drag( mouseEvent );
			}
		} );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				release();
			}
		} );
	}

	/**
	 * Returns the mouse filter.
	 *
	 * @see #setMouseFilter(EventHandler)
	 */
	public EventHandler<? super MouseEvent> getMouseFilter() {
		return mouseFilter;
	}

	/**
	 * Sets the mouse filter for starting the pan action. If the filter consumes the event with
	 * {@link Event#consume()}, then the event is ignored. If the filter is null, all events are
	 * passed through. The default filter is {@link #DEFAULT_FILTER}.
	 */
	public void setMouseFilter( EventHandler<? super MouseEvent> mouseFilter ) {
		this.mouseFilter = mouseFilter;
	}

	public void start() {
		handlerManager.addAllHandlers();
	}

	public void stop() {
		handlerManager.removeAllHandlers();
		release();
	}

	private boolean passesFilter( MouseEvent event ) {
		if ( mouseFilter != null ) {
			MouseEvent cloned = (MouseEvent) event.clone();
			mouseFilter.handle( cloned );
			if ( cloned.isConsumed() )
				return false;
		}

		return true;
	}

	private void startDrag( MouseEvent event ) {
		lastX = event.getX();
		lastY = event.getY();

		wasXAnimated = xAxis.getAnimated();
		wasYAnimated = yAxis.getAnimated();

		xAxis.setAnimated( false );
		xAxis.setAutoRanging( false );
		yAxis.setAnimated( false );
		yAxis.setAutoRanging( false );

		if ( chartInfo.getXAxisArea().contains( lastX, lastY ) ) {
			panMode = PanMode.Horizontal;
		} else if ( chartInfo.getYAxisArea().contains( lastX, lastY ) ) {
			panMode = PanMode.Vertical;
		} else {
			// probably chartInfo.getPlotArea().contains( lastX, lastY ), 
			// but not necessarily, e.g. in the corners/title/etc.
			panMode = PanMode.Both;	
		}

		dragging = true;
	}

	private void drag( MouseEvent event ) {
		if ( !dragging )
			return;

		if ( panMode == PanMode.Both || panMode == PanMode.Horizontal ) {
			double dX = ( event.getX() - lastX ) / -xAxis.getScale();
			lastX = event.getX();
			xAxis.setAutoRanging( false );
			xAxis.setLowerBound( xAxis.getLowerBound() + dX );
			xAxis.setUpperBound( xAxis.getUpperBound() + dX );
		}

		if ( panMode == PanMode.Both || panMode == PanMode.Vertical ) {
			double dY = ( event.getY() - lastY ) / -yAxis.getScale();
			lastY = event.getY();
			yAxis.setAutoRanging( false );
			yAxis.setLowerBound( yAxis.getLowerBound() + dY );
			yAxis.setUpperBound( yAxis.getUpperBound() + dY );
		}
	}

	private void release() {
		if ( !dragging )
			return;

		dragging = false;

		xAxis.setAnimated( wasXAnimated );
		yAxis.setAnimated( wasYAnimated );
	}
}
