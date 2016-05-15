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

/**
 * @author gforman44
 */
public class TabUtil {
	public static final DataFormat TAB_TYPE = new DataFormat("nonserializableObject/JfxTab");

	public static Tab newDraggableTab(String label) {
		Tab rr = new Tab();
		rr.setGraphic(new Label(label));
		makeDraggable(rr);
		return rr;
	}

	/** global for drag-n-drop of non-serializable type */
	private static Tab dndTab;

	public static void makeDraggable( final Tab tab) {
		tab.getGraphic().setOnDragDetected( new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent event ) {
				Dragboard dragboard = tab.getGraphic().startDragAndDrop( TransferMode.MOVE );
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.put( TAB_TYPE, 1 );
				dndTab = tab;
				dragboard.setContent( clipboardContent );
				event.consume();
			}
		} );
	}

	public static void makeDroppable( final TabPane tabPane) {
		tabPane.setOnDragOver( new EventHandler<DragEvent>() {
			@Override
			public void handle( DragEvent event ) {
				if ( event.getDragboard().hasContent( TAB_TYPE )
						&& dndTab.getTabPane() != tabPane ) {// && different from source location
					event.acceptTransferModes( TransferMode.MOVE );
					event.consume();
				}
			}
		} );
		tabPane.setOnDragDropped( new EventHandler<DragEvent>() {
			@Override
			public void handle( DragEvent event ) {
				if ( event.getDragboard().hasContent( TAB_TYPE )
						&& dndTab.getTabPane() != tabPane ) {// && different from source location
					dndTab.getTabPane().getTabs().remove( dndTab );
					tabPane.getTabs().add( dndTab );
					event.setDropCompleted( true );
					event.consume();
				}
			}
		} );
	}
}
