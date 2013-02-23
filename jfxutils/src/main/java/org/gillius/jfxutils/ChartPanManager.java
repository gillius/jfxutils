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

import javafx.event.EventHandler;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

/**
 * ChartPanManager manages drag gestures on an {@link XYChart} by translating them to panning
 * actions on the chart's axes.
 *
 * @author Jason Winnebeck
 */
public class ChartPanManager {
	private final EventHandlerManager handlerManager;

	private final ValueAxis<?> xAxis;
	private final ValueAxis<?> yAxis;

	private boolean dragging = false;

	private boolean wasXAnimated;
	private boolean wasYAnimated;

	private double lastX;
	private double lastY;

	public ChartPanManager( XYChart<?, ?> chart ) {
		handlerManager = new EventHandlerManager( chart );
		xAxis = (ValueAxis<?>) chart.getXAxis();
		yAxis = (ValueAxis<?>) chart.getYAxis();

		handlerManager.addEventHandler( false, MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
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

	public void start() {
		handlerManager.addAllHandlers();
	}

	public void stop() {
		handlerManager.removeAllHandlers();
		release();
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

		dragging = true;
	}

	private void drag( MouseEvent event ) {
		if ( !dragging )
			return;

		double dX = ( event.getX() - lastX ) / -xAxis.getScale();
		double dY = ( event.getY() - lastY ) / -yAxis.getScale();
		lastX = event.getX();
		lastY = event.getY();

		xAxis.setAutoRanging( false );
		xAxis.setLowerBound( xAxis.getLowerBound() + dX );
		xAxis.setUpperBound( xAxis.getUpperBound() + dX );

		yAxis.setAutoRanging( false );
		yAxis.setLowerBound( yAxis.getLowerBound() + dY );
		yAxis.setUpperBound( yAxis.getUpperBound() + dY );
	}

	private void release() {
		if ( !dragging )
			return;

		dragging = false;

		xAxis.setAnimated( wasXAnimated );
		yAxis.setAnimated( wasYAnimated );
	}
}
