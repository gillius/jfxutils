jfxutils
========

JavaFX Utilities - Zoom and Pan Charts and Pane Scaling

License: Apache 2<br/>
Latest Release: 0.1<br/>
Development version: 0.2-SNAPSHOT

* Add zooming functionality to a JavaFX XYChart:
  * Drag mouse in plot area to zoom both axes
  * Drag mouse on X axis to zoom that axis only
  * Drag moues on Y axis to zoom that axis only
  * (New in 0.2) Mouse filter, for example allow only left click drag to zoom.
  * (New in 0.2) Animated zooming
* (New in 0.2) Panning ability for XYCharts
  * Mouse filtering, for example right click drag or ctrl+left click for panning
* (New in 0.2) StableTicksAxis provides the same tick locations for a zoom level, regardless of the lower and upper bounds. This provides a much smoother experience when panning and zooming.
* Ability to create a Pane that scales when it resizes instead of reflowing its layout, creating an effect like a presentation slide.
* An experimental way to replace a Node in the scene graph with a different one (it's not as easy as you think)
* A way to get X and Y offsets of a Node from an arbitrary ancestor in a way that handles translation/padding/layout, etc., but does not yet handle scale and rotations (this might be possible if transforms can be combined as rumored for JFX 8)

Getting
-------

Maven - Latest Development Version
```xml
<repositories>
  <repository>
    <id>gillius-org</id>
    <name>Gillius.org Maven Repository</name>
    <url>http://www.gillius.org/maven2</url>
  </repository>
</repositories>

<dependency>
  <groupId>org.gillius</groupId>
  <artifactId>jfxutils</artifactId>
  <version>0.1</version>
</dependency>
```

javadoc and sources variants are also available.

Future Work
-----------

* Panning ability on graphs
* (Maybe) Default widget panel with buttons to switch between zooming and panning and reset graph's zoom to default

Building
--------

Since JavaFX is not included in the default classpath of applications, even if you have installed JavaFX SDK or are using a recent Java 7 with it bundled, compiling is awkward. When you compile if you get reference errors to javafx packages, copy the jfxrt.jar from your JDK's jre/lib directory to jre/lib/ext. This solution was proposed at https://github.com/zonski/javafx-maven-plugin/wiki/Fixing-the-JRE-classpath. You can also run the Maven command on that page to do the copy for you, but you must run as a user with permissions to copy files in the JDK (i.e. run as admin). Once this is done you can use Maven to build the library. Using the library as a dependency does not require this particular solution, as long as your own project is already building fine with JavaFX.

This workaround should not be necessary when JDK 8 is released.
