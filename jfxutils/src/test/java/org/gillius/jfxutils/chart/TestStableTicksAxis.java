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

import org.junit.*;

import static org.junit.Assert.*;

/**
 * TestStableTicksAxis
 *
 * @author Jason Winnebeck
 */
public class TestStableTicksAxis {
	@Test
	public void testCalculateTickSpacing() {
		assertEquals( 1.0, StableTicksAxis.calculateTickSpacing( 3.0, 5 ), 0.0001 );
		assertEquals( 2.5, StableTicksAxis.calculateTickSpacing( 9.0, 5 ), 0.0001 );
		assertEquals( 2.5, StableTicksAxis.calculateTickSpacing( 10.0, 5 ), 0.0001 );
		assertEquals( 2.5, StableTicksAxis.calculateTickSpacing( 11.0, 5 ), 0.0001 );
		assertEquals( 5.0, StableTicksAxis.calculateTickSpacing( 14.0, 5 ), 0.0001 );

		assertEquals( 10.0, StableTicksAxis.calculateTickSpacing( 50.0, 5 ), 0.0001 );

		assertEquals( 0.5, StableTicksAxis.calculateTickSpacing( 2.5, 5 ), 0.0001 );

		assertEquals( 0.5 * 10000, StableTicksAxis.calculateTickSpacing( 2.5 * 10000, 5 ), 0.0001 );
	}

	@Test
	public void testCalculateTickSpacingHuge() {
		assertEquals( 5e10, StableTicksAxis.calculateTickSpacing( 23e10, 5 ), 0.0001e10 );
	}

	@Test
	public void testCalculateTickSpacingTiny() {
		assertEquals( 5e-10, StableTicksAxis.calculateTickSpacing( 23e-10, 5 ), 0.001e-10 );
	}
}
