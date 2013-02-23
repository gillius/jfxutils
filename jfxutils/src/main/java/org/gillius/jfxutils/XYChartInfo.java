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

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;

import static org.gillius.jfxutils.JFXUtil.*;

/**
 * XYChartInfo provides information about areas in an {@link XYChart}. Most of the methods deal with
 * locating components of the chart in the coordinate space of the reference node. The reference
 * node could be the chart itself or an ancestor of the chart.
 * <p/>
 * There is a current limitation of this class in that there must not be scaling or rotation
 * transformations between the reference node and the chart's axes. Therefore, the reference node
 * is best when it is the chart itself or an immediate parent of the chart.
 *
 * @author Jason Winnebeck
 */
public class XYChartInfo {
	private final XYChart<?,?> chart;
	private final Node referenceNode;

	/**
	 * Constructs the XYChartInfo to find chart information in the reference node's coordinate
	 * system.
	 */
	public XYChartInfo( XYChart<?, ?> chart, Node referenceNode ) {
		this.chart = chart;
		this.referenceNode = referenceNode;
	}

	/**
	 * Constructs the XYChartInfo to find chart information in the chart's coordinate system.
	 */
	public XYChartInfo( XYChart<?, ?> chart ) {
		this( chart, chart );
	}

	public XYChart<?, ?> getChart() {
		return chart;
	}

	public Node getReferenceNode() {
		return referenceNode;
	}

	/**
	 * Returns true if the given x and y coordinate in the reference's coordinate system is in the
	 * chart's plot area, based on the xAxis and yAxis locations. This method works regardless of
	 * the started/stopped state.
	 */
	public boolean isInPlotArea( double x, double y ) {
		return getPlotArea().contains( x, y );
	}

	/**
	 * Returns the plot area in the reference's coordinate space.
	 */
	public Rectangle2D getPlotArea() {
		Axis<?> xAxis = chart.getXAxis();
		Axis<?> yAxis = chart.getYAxis();

		double xStart = getXShift( xAxis, referenceNode );
		double yStart = getYShift( yAxis, referenceNode );

		//If the direct method to get the width (which is based on its Node dimensions) is not found to
		//be appropriate, an alternative method is commented.
//		double width = xAxis.getDisplayPosition( xAxis.toRealValue( xAxis.getUpperBound() ) );
		double width = xAxis.getWidth();
//		double height = yAxis.getDisplayPosition( yAxis.toRealValue( yAxis.getLowerBound() ) );
		double height = yAxis.getHeight();

		return new Rectangle2D( xStart, yStart, width, height );
	}

	/**
	 * Returns the X axis area in the reference's coordinate space.
	 */
	public Rectangle2D getXAxisArea() {
		return getComponentArea( chart.getXAxis() );
	}

	/**
	 * Returns the Y axis area in the reference's coordinate space.
	 */
	public Rectangle2D getYAxisArea() {
		return getComponentArea( chart.getYAxis() );
	}

	private Rectangle2D getComponentArea( Region childRegion ) {
		double xStart = getXShift( childRegion, referenceNode );
		double yStart = getYShift( childRegion, referenceNode );

		return new Rectangle2D( xStart, yStart, childRegion.getWidth(), childRegion.getHeight() );
	}
}
