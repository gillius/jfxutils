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

package org.gillius.jfxutils.tab;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.*;

import java.lang.ref.WeakReference;

/**
 * Utility methods for working with JavaFX Tabs.
 *
 * @author gforman44
 * @author Jason Winnebeck
 */
public class TabUtil {
	/**
	 * Clipboard data format for draggable tabs.
	 */
	public static final DataFormat TAB_TYPE = new DataFormat( "nonserializableObject/JfxTab" );

	/**
	 * Helper method to create a new tab with the given label and make it draggable with {@link #makeDraggable}.
	 */
	public static Tab newDraggableTab( String label ) {
		Tab rr = new Tab();
		rr.setGraphic( new Label( label ) );
		makeDraggable( rr );
		return rr;
	}

	/**
	 * global for drag-n-drop of non-serializable type
	 */
	private static WeakReference<Tab> dndTab;

	/**
	 * Makes the specified tab draggable. It can be dragged to a {@link TabPane} set up by {@link #makeDroppable(TabPane)}.
	 * setOnDragDetected on the tab's graphic is called to handle the event.
	 */
	public static void makeDraggable( final Tab tab ) {
		tab.getGraphic().setOnDragDetected( new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent event ) {
				Dragboard dragboard = tab.getGraphic().startDragAndDrop( TransferMode.MOVE );
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.put( TAB_TYPE, 1 );
				dndTab = new WeakReference<>( tab );
				dragboard.setContent( clipboardContent );
				event.consume();
			}
		} );
	}

	/**
	 * Makes the specified {@link TabPane} a drag target for draggable tabs from {@link #makeDraggable(Tab)}.
	 * setOnDragOver and setOnDragDropped are called on the pane to handle the event.
	 */
	public static void makeDroppable( final TabPane tabPane ) {
		tabPane.setOnDragOver( new EventHandler<DragEvent>() {
			@Override
			public void handle( DragEvent event ) {
				if ( dndTab != null && event.getDragboard().hasContent( TAB_TYPE ) ) {
					Tab tab = dndTab.get();
					if ( tab != null && tab.getTabPane() != tabPane ) {// && different from source location
						event.acceptTransferModes( TransferMode.MOVE );
						event.consume();
					}
				}
			}
		} );
		tabPane.setOnDragDropped( new EventHandler<DragEvent>() {
			@Override
			public void handle( DragEvent event ) {
				if ( dndTab != null && event.getDragboard().hasContent( TAB_TYPE ) ) {
					Tab tab = dndTab.get();
					if ( tab != null && tab.getTabPane() != tabPane ) {// && different from source location
						tab.getTabPane().getTabs().remove( tab );
						tabPane.getTabs().add( tab );
						event.setDropCompleted( true );
						event.consume();
					}
					dndTab = null;
				}
			}
		} );
	}
}
