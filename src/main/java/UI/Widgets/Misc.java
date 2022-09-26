package UI.Widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


/**
 * A class that contains miscellaneous widgets that do not have many functions
 */
public class Misc {

    /**
     * Override of a selection model to not allow any selection
     *
     * @param <T>  the type of observable list
     */
    public static class NoSelectionModel<T> extends MultipleSelectionModel<T> {

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void selectIndices(int index, int... indices) {
        }

        @Override
        public void selectAll() {
        }

        @Override
        public void selectFirst() {
        }

        @Override
        public void selectLast() {
        }

        @Override
        public void clearAndSelect(int index) {
        }

        @Override
        public void select(int index) {
        }

        @Override
        public void select(T obj) {
        }

        @Override
        public void clearSelection(int index) {
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public boolean isSelected(int index) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void selectPrevious() {
        }

        @Override
        public void selectNext() {
        }
    }


    /**
     * Creates a JavaFx Pane that grows vertically to fill up remaining space in a window
     *
     * @return the Pane object that grows vertically
     */
    public static Pane getVerticalSpacer() {
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setMaxHeight(Double.MAX_VALUE);

        return spacer;
    }


    /**
     * Creates a JavaFx Pane that grows horizontally to fill up remaining space in a window
     *
     * @return the Pane object that grows vertically
     */
    public static Pane getHorizontalSpacer() {
        Pane spacer = new Pane();  // used as a spacer between buttons
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMaxWidth(Double.MAX_VALUE);

        return spacer;
    }


    public static Bounds calculateNodeSize(Node node) {
        Pane ghostPane = new Pane();
        Scene ghostScene = new Scene(ghostPane);  // a scene is needed to calculate preferred sizes of nodes

        ghostPane.getChildren().add(node);
        ghostPane.applyCss();
        ghostPane.layout();
        Bounds b = node.getBoundsInLocal();
        ghostPane.getChildren().clear();

        return b;
    }
}
