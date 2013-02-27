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

import javafx.scene.chart.ValueAxis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.*;

/**
 * StableTicksAxis is not ready to be used.
 *
 * @author Jason Winnebeck
 */
public class StableTicksAxis extends ValueAxis<Number> {

	public StableTicksAxis() {
		System.out.println( "StableTicksAxis.StableTicksAxis" );
	}

	public StableTicksAxis( double lowerBound, double upperBound ) {
		super( lowerBound, upperBound );
		System.out.println( "StableTicksAxis.StableTicksAxis" );
	}

	@Override
	protected Object autoRange( double minValue, double maxValue, double length, double labelSize ) {
		System.out.printf( "autoRange(%f, %f, %f, %f)%n",
		                   minValue, maxValue, length, labelSize );
		double delta = maxValue - minValue;
		double scale = length / ( maxValue - minValue );

		int maxTicks = Math.max( 1, (int) ( length / labelSize ) );

		return new double[] { minValue, maxValue, scale };
	}

	private static final double[] dividers = new double[] { 1.0, 2.0, 5.0 };

	public static double calculateTickSpacing( double delta, int maxTicks ) {
		if ( delta <= 0.0 )
			throw new IllegalArgumentException( "delta must be positive" );
		if ( maxTicks < 1 )
			throw new IllegalArgumentException( "must be at least one tick" );

		//Take a guess that we'll space ticks at 1
		int factor = 0;
		int divider = 0;
		int numTicks = (int) delta;

		//We don't have enough ticks, so increase ticks until we're over the limit, then back off once.
		if ( numTicks < maxTicks ) {
			while ( numTicks < maxTicks ) {
				//Move up
				--divider;
				if ( divider < 0 ) {
					--factor;
					divider = dividers.length - 1;
				}

				numTicks = (int) (delta / ( dividers[divider] * Math.pow( 10, factor ) ));
			}

			//Now back off once unless we hit exactly
			if ( numTicks != maxTicks ) {
				++divider;
				if ( divider >= dividers.length ) {
					++factor;
					divider = 0;
				}
			}
		} else {
			//We have too many ticks or exactly max, so decrease until we're just under (or at) the limit.
			while ( numTicks > maxTicks ) {
				++divider;
				if ( divider >= dividers.length ) {
					++factor;
					divider = 0;
				}

				numTicks = (int) (delta / ( dividers[divider] * Math.pow( 10, factor ) ));
			}
		}

		return dividers[divider] * Math.pow( 10, factor );
	}

	@Override
	protected List<Number> calculateMinorTickMarks() {
		System.out.println( "StableTicksAxis.calculateMinorTickMarks" );
		return Collections.emptyList();
	}

	@Override
	protected void setRange( Object range, boolean animate ) {
		double[] rangeVal = (double[]) range;
		System.out.format( "StableTicksAxis.setRange (%s, %s)%n",
		                   Arrays.toString( rangeVal ),
		                   animate );
		setLowerBound( rangeVal[0] );
		setUpperBound( rangeVal[1] );
		currentLowerBound.set( rangeVal[0] );
		setScale( rangeVal[2] );
	}

	@Override
	protected Object getRange() {
		double[] ret = { getLowerBound(), getUpperBound() };
		System.out.println( "StableTicksAxis.getRange = " + Arrays.toString( ret ) );
		return ret;
	}

	@Override
	protected List<Number> calculateTickValues( double length, Object range ) {
		double[] rangeVal = (double[]) range;
		System.out.format( "StableTicksAxis.calculateTickValues (length=%f, range=%s)%n",
		                   length,
		                   Arrays.toString( rangeVal ) );
		return asList( (Number) rangeVal[0], rangeVal[1] );
	}

	@Override
	protected String getTickMarkLabel( Number number ) {
		System.out.println( "StableTicksAxis.getTickMarkLabel " + number );
		return String.valueOf( number );
	}
}
