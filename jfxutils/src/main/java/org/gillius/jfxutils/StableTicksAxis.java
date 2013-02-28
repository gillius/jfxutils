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

import javafx.geometry.Dimension2D;
import javafx.scene.chart.ValueAxis;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StableTicksAxis is not ready to be used.
 *
 * @author Jason Winnebeck
 */
public class StableTicksAxis extends ValueAxis<Number> {

	private final NumberFormat normalFormat = NumberFormat.getNumberInstance();
	private final NumberFormat engFormat = NumberFormat.getNumberInstance();

	private NumberFormat currFormat = normalFormat;

	public StableTicksAxis() {
	}

	public StableTicksAxis( double lowerBound, double upperBound ) {
		super( lowerBound, upperBound );
	}

	@Override
	protected Object autoRange( double minValue, double maxValue, double length, double labelSize ) {
//		System.out.printf( "autoRange(%f, %f, %f, %f)",
//		                   minValue, maxValue, length, labelSize );
		Range ret;
		//noinspection FloatingPointEquality
		if ( minValue == maxValue ) {
			minValue = minValue - 1;
			maxValue = maxValue + 1;
		}
		length = getLength();
		double delta = maxValue - minValue;
		double scale = calculateNewScale( length, minValue, maxValue );

		int maxTicks = Math.max( 1, (int) ( length / getLabelSize() ) );

		ret = new Range( minValue, maxValue, calculateTickSpacing( delta, maxTicks ), scale );
//		System.out.printf( " = %s%n", ret );
		return ret;
	}

	private static final double[] dividers = new double[] { 1.0, 2.0, 5.0 };

	public static double calculateTickSpacing( double delta, int maxTicks ) {
		if ( delta == 0.0 )
			return 0.0;
		if ( delta <= 0.0 )
			throw new IllegalArgumentException( "delta must be positive" );
		if ( maxTicks < 1 )
			throw new IllegalArgumentException( "must be at least one tick" );

		//The factor will be close to the log10, this just optimizes the search
		int factor = (int) Math.log10( delta );
		int divider = 0;
		int numTicks = (int) (delta / ( dividers[divider] * Math.pow( 10, factor ) ));

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
//		System.out.println( "StableTicksAxis.calculateMinorTickMarks" );
		return Collections.emptyList();
	}

	@Override
	protected void setRange( Object range, boolean animate ) {
		Range rangeVal = (Range) range;
//		System.out.format( "StableTicksAxis.setRange (%s, %s)%n",
//		                   range, animate );
		setLowerBound( rangeVal.low );
		setUpperBound( rangeVal.high );
		currentLowerBound.set( rangeVal.low );
		setScale( rangeVal.scale );

		//Set the number format. Pick a "normal" format unless the numbers are quite large or small.
		currFormat = normalFormat;
		double log10 = Math.log10( rangeVal.low );
		if ( log10 < -4.0 || log10 > 5.0 ) {
			currFormat = engFormat;
		} else {
			log10 = Math.log10( rangeVal.high );
			if ( log10 < -4.0 || log10 > 5.0 ) {
				currFormat = engFormat;
			}
		}
	}

	@Override
	protected Object getRange() {
		Object ret = autoRange( getLowerBound(), getUpperBound(), getLength(), getLabelSize() );
//		System.out.println( "StableTicksAxis.getRange = " + ret );
		return ret;
	}

	@Override
	protected List<Number> calculateTickValues( double length, Object range ) {
		Range rangeVal = (Range) range;
//		System.out.format( "StableTicksAxis.calculateTickValues (length=%f, range=%s)",
//		                   length, rangeVal );
		double firstTick = Math.ceil( rangeVal.low / rangeVal.tickSpacing ) * rangeVal.tickSpacing;
		int numTicks = (int) (rangeVal.getDelta() / rangeVal.tickSpacing);
		List<Number> ret = new ArrayList<Number>( numTicks + 1 );
		for ( int i = 0; i <= numTicks; ++i ) {
			ret.add( firstTick + rangeVal.tickSpacing * i );
		}
//		System.out.printf( " = %s%n", ret );
		return ret;
	}

	@Override
	protected String getTickMarkLabel( Number number ) {
		return currFormat.format( number );
	}

	private double getLength() {
		if ( getSide().isHorizontal() )
			return getWidth();
		else
			return getHeight();
	}

	private double getLabelSize() {
		Dimension2D dim = measureTickMarkLabelSize( "-888.88E-88", getTickLabelRotation() );
		if ( getSide().isHorizontal() ) {
			return dim.getWidth();
		} else {
			return dim.getHeight();
		}
	}

	private static class Range {
		public double low;
		public double high;
		public double tickSpacing;
		public double scale;

		private Range( double low, double high, double tickSpacing, double scale ) {
			this.low = low;
			this.high = high;
			this.tickSpacing = tickSpacing;
			this.scale = scale;
		}

		public double getDelta() {
			return high - low;
		}

		@Override
		public String toString() {
			return "Range{" +
			       "low=" + low +
			       ", high=" + high +
			       ", tickSpacing=" + tickSpacing +
			       ", scale=" + scale +
			       '}';
		}
	}
}
