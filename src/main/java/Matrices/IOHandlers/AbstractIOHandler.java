package Matrices.IOHandlers;

import Matrices.Data.AbstractDSMData;
import Matrices.IOHandlers.Flags.IStandardExports;
import Matrices.Views.AbstractMatrixView;
import UI.Widgets.Misc;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A class with methods that handle exporting, saving, or importing a matrix object.
 *
 * @author Aiden Carney
 */
public abstract class AbstractIOHandler implements IStandardExports {

    protected File savePath;
    protected AbstractDSMData matrix;


    /**
     * Constructor. Sets the default save path
     *
     * @param savePath  the location to read and write from
     */
    public AbstractIOHandler(File savePath) {
        this.savePath = savePath;
    }


    /**
     * Gets the current file save path
     *
     * @return  the current path of the save location
     */
    public File getSavePath() {
        return savePath;
    }


    /**
     * Sets the path of the save location
     *
     * @param savePath  the new location to save to
     */
    public void setSavePath(File savePath) {
        this.savePath = savePath;
    }


    /**
     * Forces a file to have a specific extension. Returns a new file object with the specified extension.
     * Checks if the file absolute path ends with ".extension" and if not adds it
     *
     * @param file      the file to check
     * @param extension the extension to force
     * @return          a file object with the extension at the end
     */
    static public File forceExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        if(!path.endsWith(extension)) {
            path += extension;
            return new File(path);
        }

        return file;
    }


    /**
     * Reads a file into memory to determine what type of matrix it is
     *
     * @param file  the file to read in
     * @return      type string of the dsm type the file contains. Empty string if error occurred
     */
    public static String getFileDSMType(File file) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(file);  // read file into memory
            Element rootElement = document.getRootElement();
            Element info = rootElement.getChild("info");

            return info.getChild("type").getText();
        } catch(Exception e) {
            // TODO: add alert box that says the file was corrupted in some way and could not be read in
            System.out.println("Error checking DSM file type");
            e.printStackTrace();
            return "";
        }
    }


    /**
     * Reads an xml file and parses it as an object that extends the template DSM. Returns the object,
     * but does not automatically add it to be handled.
     *
     * @return  the parsed in matrix
     */
    public abstract <T1 extends AbstractDSMData> T1 readFile();


    /**
     * Saves the matrix to an xml file specified by the caller of the function. Clears
     * the matrix's wasModifiedFlag
     *
     * @param file      the file to save the matrix to
     * @return          1 on success, 0 on error
     */
    public abstract int saveMatrixToFile(File file);


    /**
     * Opens a file chooser window to choose a location to save a matrix to
     *
     * @param window the window associated with the file chooser
     */
    public void promptSaveToFile(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = this.saveMatrixToFile(fileName);
        }
    }


    /**
     * Brings up a dialogue window that asks whether the user wants to save a file or not.
     * Presents the user with three options: save, don't save, and cancel. This function
     * should be called before removing a matrix. Does not save the matrix only decides what
     * should be done
     *
     * @return     0 = don't save, 1 = save, 2 = cancel
     */
     public Integer promptSave() {
        AtomicReference<Integer> code = new AtomicReference<>(); // 0 = close the tab, 1 = save and close, 2 = don't close
        code.set(2);  // default value
        Stage window = new Stage();

        Label prompt = new Label("Would you like to save your changes to " + savePath.getAbsolutePath());

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("DSMEditor");

        // create HBox for user to close with our without changes
        HBox optionsArea = new HBox();
        optionsArea.setAlignment(Pos.CENTER);
        optionsArea.setSpacing(15);
        optionsArea.setPadding(new Insets(10, 10, 10, 10));

        Button saveAndCloseButton = new Button("Save");
        saveAndCloseButton.setOnAction(ee -> {
            code.set(1);
            window.close();
        });

        Button closeButton = new Button("Don't Save");
        closeButton.setOnAction(ee -> {
            code.set(0);
            window.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            code.set(2);
            window.close();
        });

        optionsArea.getChildren().addAll(saveAndCloseButton, closeButton, cancelButton);


        VBox layout = new VBox(10);
        layout.getChildren().addAll(prompt, optionsArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 500, 125);
        window.setScene(scene);
        window.showAndWait();

        return code.get();
    }


    /**
     * Opens a file chooser window to choose a location to export a matrix to csv
     *
     * @param window the window associated with the file chooser
     */
    @Override
    public void promptExportToCSV(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = exportMatrixToCSV(fileName);
        }
    }


    /**
     * Styles a cell in an excel workbook in place
     *
     * @param wb         the workbook object of the spreadsheet that contains the cell
     * @param cell       the cell created from the workbook to style
     * @param bgColor    the background color for the cell
     * @param fontColor  the font color for the cell
     * @param rotation   the degrees rotation for the cell (to change horizontal vs vertical text)
     */
    protected void styleExcelCell(XSSFWorkbook wb, Cell cell, Color bgColor, Color fontColor, short rotation) {
        XSSFCellStyle style = wb.createCellStyle();
        // background color
        if(bgColor != null) {
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (bgColor.getRed()), (float) (bgColor.getGreen()), (float) (bgColor.getBlue())), new DefaultIndexedColorMap()));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        // font color
        if(fontColor != null) {
            XSSFFont font = wb.createFont();
            font.setColor(new XSSFColor(new java.awt.Color((float) (fontColor.getRed()), (float) (fontColor.getGreen()), (float) (fontColor.getBlue())), new DefaultIndexedColorMap()));
            style.setFont(font);
        }
        // rotation
        style.setRotation(rotation);

        // set up borders and wrap text
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);

        cell.setCellStyle(style);
    }


    /**
     * Opens a file chooser window to choose a location to export a matrix to excel
     *
     * @param window  the window associated with the file chooser
     */
    @Override
    public void promptExportToExcel(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel File", "*.xlsx"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = exportMatrixToXLSX(fileName);
        }
    }


    /**
     * Opens a window to export a matrix to a png file with different configuration options
     *
     * @param matrix      the matrix object to save to an image
     * @param matrixView  the matrix gui handler for the matrix object
     */
    @Override
     public void exportToImage(AbstractDSMData matrix, AbstractMatrixView matrixView) {
        // Create Root window
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("DSMEditor");

        SplitPane splitPane = new SplitPane();

        // preview area
        VBox preview = new VBox();
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setSpacing(10);
        preview.setPadding(new Insets(5));


        // parameters area
        VBox parametersLayout = new VBox();
        parametersLayout.setAlignment(Pos.CENTER);
        parametersLayout.setSpacing(10);
        parametersLayout.setPadding(new Insets(5));

        // add info about the matrix (customer, title, version, etc.)
        CheckBox addInfo = new CheckBox("Add Matrix Info");
        addInfo.setSelected(true);
        addInfo.setMaxWidth(Double.MAX_VALUE);

        // make title big and centered
        CheckBox bigTitle = new CheckBox("Big Title");
        bigTitle.setSelected(true);
        bigTitle.setMaxWidth(Double.MAX_VALUE);

        // show connection names
        CheckBox showConnectionNames = new CheckBox("Show Connection Names");
        showConnectionNames.setSelected(true);
        showConnectionNames.setMaxWidth(Double.MAX_VALUE);

        // show connection names
        CheckBox fastRender = new CheckBox("Remove Detail");
        fastRender.setSelected(false);
        fastRender.setMaxWidth(Double.MAX_VALUE);

        // annotation
        CheckBox addAnnotation = new CheckBox("Add Annotation");
        addAnnotation.setMaxWidth(Double.MAX_VALUE);

        VBox annotationLayout = new VBox();
        TextArea annotation = new TextArea();
        annotation.setMinHeight(100);
        annotation.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(annotation, Priority.ALWAYS);
        annotationLayout.getChildren().addAll(annotation);

        // listener to make annotation area invisible
        addAnnotation.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                annotationLayout.setVisible(true);
                annotationLayout.setManaged(true);
            } else {
                annotationLayout.setVisible(false);
                annotationLayout.setManaged(false);
            }
        });

        // file save area
        HBox saveArea = new HBox();
        Label saveLocation = new Label("");
        saveLocation.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveLocation, Priority.ALWAYS);

        Button chooseFile = new Button("Choose Save Location");
        chooseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image File", "*.png"));  // dsm is the only file type usable
            File fileName = fileChooser.showSaveDialog(window);
            if(fileName != null) {
                saveLocation.setText(fileName.getAbsolutePath());
            }
        });

        saveArea.getChildren().addAll(saveLocation, chooseFile);
        saveArea.setSpacing(10);
        saveArea.setPadding(new Insets(5));

        // save button
        HBox saveButtonArea = new HBox();

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            if(!saveLocation.getText().equals("")){
                File file = new File(saveLocation.getText());
                BufferedImage img = SwingFXUtils.fromFXImage(preview.snapshot(new SnapshotParameters(), null), null);
                try {
                    ImageIO.write(img, "png", file);
                } catch (IOException ioException) {
                    System.out.println("Did not save image - caught IOException");
                    ioException.printStackTrace();
                }
            }
            window.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> window.close());

        saveButtonArea.getChildren().addAll(Misc.getHorizontalSpacer(), cancelButton, saveButton);


        // parameter layout configuring
        parametersLayout.getChildren().addAll(addInfo, bigTitle, showConnectionNames, fastRender, addAnnotation, annotationLayout, saveArea, Misc.getVerticalSpacer(), saveButtonArea);
        addAnnotation.setSelected(false);  // set it here so annotation area is not shown
        annotationLayout.setVisible(false);
        annotationLayout.setManaged(false);


        Runnable updatePreview = () -> {
            preview.getChildren().removeAll(preview.getChildren());  // remove items
            if(bigTitle.isSelected()) {
                HBox centeredLabel = new HBox();
                Label l = new Label(matrix.getTitle());
                l.setStyle(l.getStyle() + "-fx-font-size: 24}; .combo-box > .list-cell {-fx-padding: 0 0 0 0; -fx-border-insets: 0 0 0 0;}");
                l.setAlignment(Pos.CENTER);

                centeredLabel.getChildren().add(l);
                centeredLabel.setAlignment(Pos.CENTER);

                preview.getChildren().add(centeredLabel);
            }

            if(addInfo.isSelected() && bigTitle.isSelected()) {
                Label l = new Label(
                    "Project Name: " + matrix.getProjectName() + "\n"
                    + "Customer: " + matrix.getCustomer() + "\n"
                    + "Version: " + matrix.getVersionNumber()
                );
                l.setAlignment(Pos.CENTER_LEFT);

                preview.getChildren().add(l);
            } else if(addInfo.isSelected() && !bigTitle.isSelected()) {
                Label l = new Label(
                    "Title: " + matrix.getTitle() + "\n"
                    + "Project Name: " + matrix.getProjectName() + "\n"
                    + "Customer: " + matrix.getCustomer() + "\n"
                    + "Version: " + matrix.getVersionNumber()
                );
                l.setAlignment(Pos.CENTER_LEFT);

                preview.getChildren().add(l);
            }

            HBox centeredMatrix = new HBox();

            if(fastRender.isSelected()) {
                matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.FAST_RENDER);
            } else if(showConnectionNames.isSelected()) {
                matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_NAMES);
            } else {
                matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_WEIGHTS);
            }
            matrixView.refreshView();

            centeredMatrix.getChildren().add(matrixView.getView());
            centeredMatrix.setAlignment(Pos.CENTER);
            preview.getChildren().add(centeredMatrix);

            if(addAnnotation.isSelected()) {
                preview.getChildren().add(new Label(annotation.getText()));
            }
        };

        // set up listeners to update the preview
        addInfo.selectedProperty().addListener((observable, oldValue, newValue) -> updatePreview.run());
        bigTitle.selectedProperty().addListener((observable, oldValue, newValue) -> updatePreview.run());
        showConnectionNames.selectedProperty().addListener((observable, oldValue, newValue) -> updatePreview.run());
        fastRender.selectedProperty().addListener((observable, oldValue, newValue) -> updatePreview.run());
        addAnnotation.selectedProperty().addListener((observable, oldValue, newValue) -> updatePreview.run());
        annotation.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {  // TextField changed to be not focused so update the view
                 updatePreview.run();
            }
        });
        updatePreview.run();  // initial update to show the matrix


        ScrollPane previewArea = new ScrollPane(preview);
        previewArea.setFitToWidth(true);
        splitPane.getItems().addAll(previewArea, parametersLayout);
        splitPane.setDividerPositions(0.8);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(splitPane, 1400, 1000);
        window.setScene(scene);
        window.showAndWait();
    }


}
