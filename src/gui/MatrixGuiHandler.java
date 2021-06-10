package gui;

import DSMData.DSMConnection;
import DSMData.DataHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.util.ArrayList;

public class MatrixGuiHandler {
    DataHandler matrix;

    MatrixGuiHandler(DataHandler matrix) {
        this.matrix = matrix;
    }

    GridPane getMatrixEditor() {
        GridPane grid = new GridPane();
//        grid.setGridLinesVisible(true);
//        grid.setVgap(8);
//        grid.setHgap(8);
        grid.setAlignment(Pos.CENTER);
        ArrayList< ArrayList< Pair<String, Object> > > template = matrix.getGridArray();
        int rows = template.size();
        int columns = template.get(0).size();

        System.out.println(rows);
        System.out.println(columns);
        System.out.println(template);
        for(int r=0; r<rows; r++) {
            for(int c=0; c<columns; c++) {
                Pair<String, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an hbox so a border can be added easily
                if(item.getKey().equals("plain_text")) {
                    Object label = null;
                    if(r == 0) {
                        label = new VerticalLabel(VerticalDirection.UP);
                        ((VerticalLabel) label).setText((String)item.getValue());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                    } else {
                        label = new Label((String)item.getValue());
                    }
                    cell.getChildren().add((Node) label);
                } else if(item.getKey().equals("item_name")) {
                    Object label = null;
                    if(r == 0) {
                        label = new VerticalLabel(VerticalDirection.UP);
                        ((VerticalLabel) label).setText(matrix.getItem((Integer)item.getValue()).getName());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                    } else {
                        label = new Label(matrix.getItem((Integer)item.getValue()).getName());
                    }
                    cell.getChildren().add((Node) label);
                } else if(item.getKey().equals("index_item")) {
                    Label label = new Label(((Double)matrix.getItem((Integer)item.getValue()).getSortIndex()).toString());
                    cell.setAlignment(Pos.CENTER_RIGHT);
                    cell.getChildren().add(label);
                } else if(item.getKey().equals("uneditable_connection")) {
                    HBox label = new HBox();  // use an hbox object because then background color is not tied to the text
                    cell.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0), new CornerRadii(3), new Insets(0))));
//                    cell.getChildren().add(label);
                } else if(item.getKey().equals("editable_connection")) {
                    int rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                    int colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();
                    DSMConnection conn = matrix.getConnection(rowUid, colUid);
                    Label label = null;
                    if(conn == null) {
                        label = new Label("");
                    } else {
                        label = new Label(conn.getConnectionName());
                    }
                    cell.setBackground(new Background(new BackgroundFill(Color.color(.9, .9, .9), new CornerRadii(3), new Insets(0))));
                    cell.setOnMouseClicked(e -> {
                        if(e.getButton().equals(MouseButton.PRIMARY)) {
                            System.out.println("editing connection");
                        } else if(e.getButton().equals(MouseButton.SECONDARY)) {  // toggle highlighting
                            if(cell.getBackground().getFills().get(0).getFill().equals(Color.color(.9, 1, 0))) {
                                cell.setBackground(new Background(new BackgroundFill(Color.color(.9, .9, .9), new CornerRadii(3), new Insets(0))));
                            } else {
                                cell.setBackground(new Background(new BackgroundFill(Color.color(.9, 1, 0), new CornerRadii(3), new Insets(0))));
                            }
                        }
                    });
                    cell.getChildren().add(label);
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(10, 10, 10, 10));
                GridPane.setConstraints(cell, c, r);
                grid.getChildren().add(cell);

            }
        }

        return grid;
    }
}
