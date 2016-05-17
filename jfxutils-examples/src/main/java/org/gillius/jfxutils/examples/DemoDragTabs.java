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

package org.gillius.jfxutils.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.gillius.jfxutils.tab.TabUtil;

/**
 * Demonstrate draggable Tabs from one TabPane to another, allowing the user to rearrange their GUI layout.
 *
 * Note:  For this to work, instead of using new Tab("Label") you should use
 * <pre>
 * t = new Tab();
 * t.setGraphic(new Label("Label"));
 *
 * @author gforman44
 */
public class DemoDragTabs extends Application {
	private static Tab makeTab( String tabName, String tabContentDummy ) {
		Tab t = TabUtil.newDraggableTab( tabName );
		t.setContent(new Label(tabContentDummy));
		return t;
	}

	@Override
	public void start(Stage primaryStage) {

		Tab f = makeTab("Files", "File system view here");
		Tab t = makeTab("Type Hierarchy","Type hierarchy view here");
		Tab d = makeTab("Debug","Debug view here");

		Tab p = makeTab("Properties","Ah, the ubiquitous 'properties' panel");
		Tab c = makeTab("Console","Console output here");
		Tab o = makeTab("Outline","Outline of fields/methods view here");

		TabPane left = new TabPane(f,t,d);
		TabUtil.makeDroppable(left); //////////////// see

		TabPane right = new TabPane(p,c,o);
		TabUtil.makeDroppable(right); /////////////// see

		left.setStyle("-fx-border-color: black;");
		right.setStyle("-fx-border-color: black;");

		BorderPane main = new BorderPane();
		main.setPadding(new Insets(0, 20, 0, 20));
		main.setTop(new Label("Menubar and toolbars"));
		main.setLeft(left);
		main.setCenter(new Label("Central work area here"));
		main.setRight(right);
		main.setBottom(new Label("Statusbar"));

		primaryStage.setScene(new Scene(main, 800, 600));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
