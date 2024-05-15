package UI;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.Flags.IPropagationAnalysis;
import Matrices.Data.MultiDomainDSMData;
import Matrices.Data.SymmetricDSMData;
import Matrices.EditorTabs.AbstractEditorTab;
import Matrices.EditorTabs.AsymmetricEditorTab;
import Matrices.EditorTabs.MultiDomainEditorTab;
import Matrices.EditorTabs.SymmetricEditorTab;
import Matrices.IOHandlers.AbstractIOHandler;
import Matrices.IOHandlers.AsymmetricIOHandler;
import Matrices.IOHandlers.Flags.IThebeauExport;
import Matrices.IOHandlers.MultiDomainIOHandler;
import Matrices.IOHandlers.SymmetricIOHandler;
import UI.MatrixViews.AbstractMatrixView;
import UI.MatrixViews.Flags.ISymmetricHighlight;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.*;


/**
 * Class to create the header of the gui. Includes menus like file, edit, and view
 *
 * @author Aiden Carney
 */
public class HeaderMenu {
    private static int defaultName = 0;

    private final Menu fileMenu = new Menu("_File");
    private final Menu editMenu = new Menu("_Edit");
    private final Menu viewMenu = new Menu("_View");
    private final Menu toolsMenu = new Menu("_Tools");
    private final Menu helpMenu = new Menu("_Help");

    private ToggleGroup toggleGroup = new ToggleGroup();
    private RadioMenuItem namesView = new RadioMenuItem("Names");
    private RadioMenuItem weightsView = new RadioMenuItem("Weights");
    private RadioMenuItem interfacesView = new RadioMenuItem("Interfaces");
    private RadioMenuItem fastRenderView = new RadioMenuItem("Fast Render");

    private List<DSMInterfaceType> currentInterfaces = new ArrayList<>();

    private final MenuBar menuBar = new MenuBar();

    private final EditorPane editor;
    private AbstractDSMData matrixData;
    private AbstractIOHandler ioHandler;
    private AbstractMatrixView matrixView;

    private boolean disabled = false;
    private boolean crossHighlight = false;

    private BooleanProperty isChanged = new SimpleBooleanProperty(false);
    private BooleanProperty isMutable = new SimpleBooleanProperty(false);

    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     */
    public HeaderMenu(EditorPane editor) {
        this.editor = editor;
        if(editor.getFocusedMatrix() != null) {
            this.matrixData = editor.getFocusedMatrix().getMatrixData();
            this.ioHandler = editor.getFocusedMatrix().getMatrixIOHandler();
            this.matrixView = editor.getFocusedMatrix().getMatrixView();
        }

        setupFileMenu();
        setupEditMenu();
        setUpToolsMenu();
        setupViewMenu();
        setupHelpMenu();
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, helpMenu);
    }


//region File Menu Items
    /**
     * sets up the Menu object for the file menu
     */
    private void setupFileMenu() {
        Menu newFileMenu = new Menu("New");
        MenuItem openMenu = new MenuItem("Open...");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem saveFileAs = new MenuItem("Save As...");
        Menu importMenu = new Menu("Import");
        Menu exportMenu = new Menu("Export");
        MenuItem exitMenu = new MenuItem("Exit");

        setupNewFileMenuButton(newFileMenu);
        setupOpenMenuButton(openMenu);
        setupSaveFileMenuButton(saveFile);
        setupSaveAsFileMenuButton(saveFileAs);
        setupImportMenuButton(importMenu);
        setupExportMenuButton(exportMenu);
        exitMenu.setOnAction(e -> menuBar.getScene().getWindow().fireEvent(
                new WindowEvent(
                        menuBar.getScene().getWindow(),
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        ));


        fileMenu.getItems().add(newFileMenu);
        fileMenu.getItems().add(openMenu);
        if(matrixData != null) {
            fileMenu.getItems().add(saveFile);
            fileMenu.getItems().add(saveFileAs);
        }
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(importMenu);
        if(matrixData != null) {
            fileMenu.getItems().add(exportMenu);
        }
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exitMenu);
    }


    /**
     * Sets up the menu items for creating new DSMs
     *
     * @param parent  the parent menu
     */
    private void setupNewFileMenuButton(Menu parent) {
        MenuItem newSymmetric = new MenuItem("Symmetric Matrix");
        newSymmetric.setOnAction(e -> {
            File file = new File("./untitled" + defaultName);
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + defaultName);
            }
            SymmetricDSMData newMatrix = new SymmetricDSMData();
            SymmetricIOHandler ioHandler = new SymmetricIOHandler(file, newMatrix);

            this.editor.addTab(new SymmetricEditorTab(newMatrix, ioHandler));

            defaultName += 1;
        });
        MenuItem newNonSymmetric = new MenuItem("Non-Symmetric Matrix");
        newNonSymmetric.setOnAction(e -> {
            File file = new File("./untitled" + defaultName);
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + defaultName);
            }
            AsymmetricDSMData newMatrix = new AsymmetricDSMData();
            AsymmetricIOHandler ioHandler = new AsymmetricIOHandler(file, newMatrix);

            this.editor.addTab(new AsymmetricEditorTab(newMatrix, ioHandler));

            defaultName += 1;
        });

        MenuItem newMultiDomain = new MenuItem("Multi-Domain Matrix");
        newMultiDomain.setOnAction(e -> {
            File file = new File("./untitled" + defaultName);
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + defaultName);
            }
            MultiDomainDSMData newMatrix = new MultiDomainDSMData();
            MultiDomainIOHandler ioHandler = new MultiDomainIOHandler(file, newMatrix);

            this.editor.addTab(new MultiDomainEditorTab(newMatrix, ioHandler));

            defaultName += 1;
        });

        parent.getItems().addAll(newSymmetric, newNonSymmetric, newMultiDomain);
    }


    /**
     * Sets up the menu for opening DSMs
     *
     * @param menu  the menu item to set the callback for
     */
    private void setupOpenMenuButton(MenuItem menu) {
        menu.setOnAction( e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file == null) {
                return;
            } else if (this.editor.getMatricesCollection().getMatrixFileAbsoluteSavePaths().contains(file.getAbsolutePath())) {
                editor.focusTab(file);  // focus on that tab because it is already open
                return;
            }
            // make sure user did not just close out of the file chooser window
            switch (AbstractIOHandler.getFileDSMType(file)) {
                case "symmetric" -> this.editor.addTab(new SymmetricEditorTab(file));
                case "asymmetric" -> this.editor.addTab(new AsymmetricEditorTab(file));
                case "multi-domain" -> this.editor.addTab(new MultiDomainEditorTab(file));

                default -> System.out.println("the type of dsm could not be determined from the file " + file.getAbsolutePath());
            }
        });
    }


    /**
     * Sets up the menu button for saving a file
     *
     * @param menu  the Menu object
     */
    private void setupSaveFileMenuButton(MenuItem menu) {
        menu.setOnAction(e -> {
            if(matrixData == null) return;

            if(!ioHandler.getSavePath().exists() || ioHandler.getSavePath().getAbsolutePath().contains("untitled")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
                File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
                if(fileName != null) {
                    fileName = AbstractIOHandler.forceExtension(fileName, ".dsm");
                    ioHandler.setSavePath(fileName);
                } else {  // user did not select a file, so do not save it
                    return;
                }
            }
            int code = ioHandler.saveMatrixToFile(ioHandler.getSavePath());  // TODO: add checking with the return code
        });
        menu.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
    }


    /**
     * Sets up the menu button for performing the "save as" operation on a file
     *
     * @param menu  the Menu object
     */
    private void setupSaveAsFileMenuButton(MenuItem menu) {
        menu.setOnAction(e -> {
            if(matrixData == null) return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
            File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
            if(file != null) {
                file = AbstractIOHandler.forceExtension(file, ".dsm");
                int code = ioHandler.saveMatrixToFile(file);  // TODO: add checking with the return code
                ioHandler.setSavePath(file);
                matrixData.setWasModified();  // flash the modified flag to trigger an update
                matrixData.clearWasModifiedFlag();
            }
        });
        menu.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN));
    }


    /**
     * Sets up the menu for importing DSMs from alternative sources (ex. thebeau matlab files)
     *
     * @param parent  the parent menu
     */
    private void setupImportMenuButton(Menu parent) {
        MenuItem importThebeau = new MenuItem("Thebeau Matlab File...");
        importThebeau.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Matlab File", "*.m"));  // matlab is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file != null) {  // make sure user did not just close out of the file chooser window
                SymmetricIOHandler ioHandler = new SymmetricIOHandler(file);
                SymmetricDSMData matrix = ioHandler.importThebeauMatlabFile(file);
                if(matrix == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file);
                } else if(!this.editor.getMatricesCollection().getMatrixFileAbsoluteSavePaths().contains(file.getAbsolutePath())) {
                    File importedFile = new File(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".dsm");  // convert .m extension to .dsm
                    ioHandler.setMatrix(matrix);
                    ioHandler.setSavePath(importedFile);
                    this.editor.addTab(new SymmetricEditorTab(matrix, ioHandler));
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        MenuItem importAdjacencyMatrix = new MenuItem("Adjacency Matrix...");
        importAdjacencyMatrix.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));  // matlab is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file != null) {  // make sure user did not just close out of the file chooser window
                String dsmType = AbstractIOHandler.getAdjacencyDSMType(file);
                AbstractDSMData matrix;
                AbstractIOHandler ioHandler;
                AbstractEditorTab dsm;

                switch(dsmType) {
                    case "symmetric" -> {
                        ioHandler = new SymmetricIOHandler(file);
                        matrix = ((SymmetricIOHandler) ioHandler).importAdjacencyMatrix(file);
                        dsm = new SymmetricEditorTab((SymmetricDSMData) matrix, (SymmetricIOHandler) ioHandler);
                    }
                    case "multi-domain" -> {
                        ioHandler = new MultiDomainIOHandler(file);
                        matrix = ((MultiDomainIOHandler) ioHandler).importAdjacencyMatrix(file);
                        dsm = new MultiDomainEditorTab((MultiDomainDSMData) matrix, (MultiDomainIOHandler) ioHandler);
                    }
                    case "asymmetric" -> {
                        ioHandler = new AsymmetricIOHandler(file);
                        matrix = ((AsymmetricIOHandler) ioHandler).importAdjacencyMatrix(file);
                        dsm = new AsymmetricEditorTab((AsymmetricDSMData) matrix, (AsymmetricIOHandler) ioHandler);
                    }
                    default -> {
                        // TODO: open window saying there was an error parsing the document
                        System.out.println("there was an error importing the file " + file);
                        return;
                    }
                }

                if(matrix == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file);
                } else if(!this.editor.getMatricesCollection().getMatrixFileAbsoluteSavePaths().contains(file.getAbsolutePath())) {
                    File importedFile = new File(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".dsm");  // convert .csv extension to .dsm
                    ioHandler.setMatrix(matrix);
                    ioHandler.setSavePath(importedFile);
                    this.editor.addTab(dsm);
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        parent.getItems().addAll(importThebeau, importAdjacencyMatrix);
    }


    /**
     * Sets up the menu items for exporting a dsm
     *
     * @param parent  the parent menu
     */
    private void setupExportMenuButton(Menu parent) {
        if (matrixData == null) return;

        MenuItem exportCSV = new MenuItem("CSV File (.csv)...");
        MenuItem exportAdjacency = new MenuItem("Adjacency Matrix (.csv)...");
        MenuItem exportXLSX = new MenuItem("Micro$oft Excel File (.xlsx)...");
        MenuItem exportImage = new MenuItem("PNG Image File (.png)...");

        // matrices by default are instances of IStandardExports so set them up
        exportCSV.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ioHandler.promptExportToCSV(menuBar.getScene().getWindow());
        });

        exportAdjacency.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ioHandler.promptExportToAdjacencyMatrix(menuBar.getScene().getWindow());
        });

        exportXLSX.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ioHandler.promptExportToExcel(menuBar.getScene().getWindow());
        });

        exportImage.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ioHandler.exportToImage(menuBar.getScene().getWindow(), matrixData.createCopy(), matrixView.createCopy());
        });


        parent.getItems().addAll(exportCSV, exportAdjacency, exportXLSX, exportImage);


        if(ioHandler instanceof IThebeauExport) {
            MenuItem exportThebeau = new MenuItem("Thebeau Matlab File (.m)...");
            exportThebeau.setOnAction(e -> {
                if(editor.getFocusedMatrixUid() == null) {
                    return;
                }

                ((IThebeauExport)ioHandler).promptExportToThebeau(menuBar.getScene().getWindow());
            });

            parent.getItems().add(exportThebeau);
        }

    }
//endregion


    /**
     * sets up the Menu object for the edit menu
     */
    private void setupEditMenu() {
        editMenu.getItems().clear();  // ensure parent not set twice

        MenuItem undo = new MenuItem("Undo");
        undo.setOnAction(e -> {
            if(matrixData == null) {
                return;
            }
            matrixData.undoToCheckpoint();
            matrixView.refreshView();
        });
        undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));

        MenuItem redo = new MenuItem("Redo");
        redo.setOnAction(e -> {
            if(matrixData == null) {
                return;
            }
            matrixData.redoToCheckpoint();
            matrixView.refreshView();
        });
        redo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));


        MenuItem invert = new MenuItem("Transpose Matrix");
        invert.setOnAction(e -> {
            if(matrixData == null) {
                return;
            }
            matrixData.transposeMatrix();
            matrixData.setCurrentStateAsCheckpoint();
            matrixView.refreshView();
        });


        MenuItem convertToMDM = new MenuItem("Convert to Multi-Domain");
        convertToMDM.setOnAction(e -> {
            if(matrixData instanceof SymmetricDSMData symmetricMatrix) {
                // create the default domain and the groupings
                ArrayList<Grouping> domainGroupings = new ArrayList<>();
                for(Grouping grouping : symmetricMatrix.getGroupings()) {
                    domainGroupings.add(new Grouping(grouping));
                }
                HashMap<Grouping, Collection<Grouping>> domains = new HashMap<>();
                Grouping domain = new Grouping("default", Color.color(1, 1, 1));
                domains.put(domain, domainGroupings);

                MultiDomainDSMData multiDomainMatrix = new MultiDomainDSMData(domains);
                multiDomainMatrix.setTitle(symmetricMatrix.getTitle());
                multiDomainMatrix.setProjectName(symmetricMatrix.getProjectName());
                multiDomainMatrix.setCustomer(symmetricMatrix.getCustomer());
                multiDomainMatrix.setVersionNumber(symmetricMatrix.getVersionNumber());

                // create the items
                for(DSMItem row : symmetricMatrix.getRows()) {
                    DSMItem newRow = new DSMItem(row);
                    newRow.setGroup2(domain);
                    multiDomainMatrix.addItem(newRow, true);
                }
                for(DSMItem col : symmetricMatrix.getCols()) {
                    DSMItem newCol = new DSMItem(col);
                    newCol.setGroup2(domain);
                    multiDomainMatrix.addItem(newCol, false);
                }

                // create the connections
                for(DSMConnection conn : symmetricMatrix.getConnections()) {
                    ArrayList<DSMInterfaceType> connectionInterfaces = new ArrayList<>();  // parse interfaces
                    for(DSMInterfaceType interfaceType : conn.getInterfaces()) {
                        connectionInterfaces.add(new DSMInterfaceType(interfaceType));
                    }
                    multiDomainMatrix.modifyConnection(conn.getRowUid(), conn.getColUid(), conn.getConnectionName(), conn.getWeight(), connectionInterfaces);
                }

                // create the interfaces
                HashMap<String, List<DSMInterfaceType>> interfaceTypes = new HashMap<>();
                for(Map.Entry<String, List<DSMInterfaceType>> interfaceGroup : symmetricMatrix.getInterfaceTypes().entrySet()) {
                    List<DSMInterfaceType> interfaces = new ArrayList<>();
                    for(DSMInterfaceType i : interfaceGroup.getValue()) {
                        interfaces.add(new DSMInterfaceType(i));
                    }
                    interfaceTypes.put(interfaceGroup.getKey(), interfaces);
                }
                for(String interfaceGrouping : interfaceTypes.keySet()) {  // add the groupings
                    multiDomainMatrix.addInterfaceTypeGrouping(interfaceGrouping);
                }
                for(Map.Entry<String, List<DSMInterfaceType>> interfaces : interfaceTypes.entrySet()) {  // add the interfaces
                    for(DSMInterfaceType i : interfaces.getValue()) {
                        multiDomainMatrix.addInterface(interfaces.getKey(), i);
                    }
                }

                multiDomainMatrix.setCurrentStateAsCheckpoint();
                multiDomainMatrix.clearStacks();


                File file = new File("./untitled" + defaultName);
                while(file.exists()) {  // make sure file does not exist
                    defaultName += 1;
                    file = new File("./untitled" + defaultName);
                }
                MultiDomainIOHandler ioHandler = new MultiDomainIOHandler(file, multiDomainMatrix);

                this.editor.addTab(new MultiDomainEditorTab(multiDomainMatrix, ioHandler));

                defaultName += 1;
            }
        });


        editMenu.setOnShown(e -> {
            if(matrixData == null) {
                undo.setDisable(true);
                redo.setDisable(true);
                invert.setDisable(true);
                convertToMDM.setDisable(true);
            } else {
                undo.setDisable(!matrixData.canUndo());
                redo.setDisable(!matrixData.canRedo());
                invert.setDisable(false);
                convertToMDM.setDisable(false);
            }
        });


        editMenu.getItems().add(undo);
        editMenu.getItems().add(redo);
        editMenu.getItems().add(new SeparatorMenuItem());
        editMenu.getItems().add(invert);

        if(matrixData instanceof SymmetricDSMData) {
            editMenu.getItems().add(convertToMDM);
        }
    }


    /**
     * sets up the Menu object for the tools menu
     */
    private void setUpToolsMenu() {
        if(matrixData == null) return;

        MenuItem toggleCrossHighlight = new MenuItem("Toggle Cross-Highlight");
        toggleCrossHighlight.setOnAction(e -> {
            matrixView.toggleCrossHighlighting();
            crossHighlight = !crossHighlight;
        });
        if(crossHighlight) {
            matrixView.enableCrossHighlighting();
            crossHighlight = true;
        } else {
            matrixView.disableCrossHighlighting();
            crossHighlight = false;
        }
        toggleCrossHighlight.setAccelerator(new KeyCodeCombination(KeyCode.F));
        toolsMenu.getItems().add(toggleCrossHighlight);

        MenuItem search = new MenuItem("Find Connections");
        search.setOnAction(e -> editor.getSearchWidget().open());
        search.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
        toolsMenu.getItems().add(search);


        if(matrixView instanceof ISymmetricHighlight symmetricMatrixView) {
            RadioMenuItem validateSymmetry = new RadioMenuItem("Validate Symmetry");
            validateSymmetry.setSelected(symmetricMatrixView.getSymmetryValidation());
            validateSymmetry.setOnAction(e -> {
                if (validateSymmetry.isSelected()) {
                    symmetricMatrixView.setValidateSymmetry();
                } else {
                    symmetricMatrixView.clearValidateSymmetry();
                }
            });
            toolsMenu.getItems().add(validateSymmetry);
        }


        if(matrixData instanceof IPropagationAnalysis) {
            MenuItem propagationAnalysis = new MenuItem("Propagation Analysis...");
            propagationAnalysis.setOnAction(e -> {
                if (editor.getFocusedMatrixUid() == null) {
                    return;
                }

                PropagationAnalysisWindow p = new PropagationAnalysisWindow(this.editor.getFocusedMatrixData());
                p.start(menuBar.getScene().getWindow());
            });

            toolsMenu.getItems().add(propagationAnalysis);
        }

        if(matrixData instanceof SymmetricDSMData) {
            MenuItem coordinationScore = new MenuItem("Thebeau Cluster Analysis...");
            coordinationScore.setOnAction(e -> {
                if (editor.getFocusedMatrixUid() == null) {
                    return;
                }

                ClusterAnalysisWindow c = new ClusterAnalysisWindow((SymmetricDSMData) this.editor.getFocusedMatrixData());
                c.start(menuBar.getScene().getWindow());
            });

            MenuItem cluster = new MenuItem("Cluster Algorithms...");
            cluster.setOnAction(e -> {
                if (editor.getFocusedMatrixUid() == null) {
                    return;
                }

                ClusterAlgorithmWindow c = new ClusterAlgorithmWindow((SymmetricDSMData) this.editor.getFocusedMatrixData());
                c.start(menuBar.getScene().getWindow());
            });

            toolsMenu.getItems().addAll(coordinationScore, cluster);
        }
    }


    /**
     * sets up the Menu object for the view menu
     */
    private void setupViewMenu() {
        MenuItem zoomIn = new MenuItem("Zoom In");
        zoomIn.setOnAction(e -> editor.increaseFontScaling());
        zoomIn.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));

        MenuItem zoomOut = new MenuItem("Zoom Out");
        zoomOut.setOnAction(e -> editor.decreaseFontScaling());
        zoomOut.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));

        MenuItem zoomReset = new MenuItem("Reset Zoom");
        zoomReset.setOnAction(e -> editor.resetFontScaling());

        Menu viewMode = new Menu("View Mode");

        namesView = new RadioMenuItem("Names");
        weightsView = new RadioMenuItem("Weights");
        interfacesView = new RadioMenuItem("Interfaces");
        fastRenderView = new RadioMenuItem("Fast Render");

        MenuItem configureVisibleInterfaces = new MenuItem("Configure Visible Interfaces...");
        configureVisibleInterfaces.setOnAction(e -> {
            currentInterfaces = ConfigureConnectionInterfaces.configureConnectionInterfaces(
                    menuBar.getScene().getWindow(), matrixData.getInterfaceTypes(), currentInterfaces);
            matrixView.setVisibleInterfaces(currentInterfaces);
        });
        configureVisibleInterfaces.setVisible(false);  // default to invisible, can be over-ridden if matrix data is not null when default view is selected

        toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener((o, oldValue, newValue) -> {
            if(matrixData == null || newValue == null) return;

            AbstractMatrixView.MatrixViewMode oldMode = matrixView.getCurrentMode();
            if(newValue.equals(namesView)) {
                configureVisibleInterfaces.setVisible(false);
                if(this.disabled) {
                    matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_NAMES);
                } else {
                    matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.EDIT_NAMES);
                }

            } else if(newValue.equals(weightsView)) {
                configureVisibleInterfaces.setVisible(false);
                if(this.disabled) {
                    matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_WEIGHTS);
                } else {
                    matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.EDIT_WEIGHTS);
                }

            } else if(newValue.equals(interfacesView)) {
                configureVisibleInterfaces.setVisible(true);
                if(this.disabled) {
                    matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_INTERFACES);
                } else {
                    matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.EDIT_INTERFACES);
                }

            } else if(newValue.equals(fastRenderView)) {
                configureVisibleInterfaces.setVisible(false);
                matrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.FAST_RENDER);
            }
            AbstractMatrixView.MatrixViewMode newMode = matrixView.getCurrentMode();


            boolean refresh = true;
            if(  // if staying within either static or edit there is not need for a refresh because the bindings will update the value automatically
                    ((oldMode.equals(AbstractMatrixView.MatrixViewMode.EDIT_NAMES) || oldMode.equals(AbstractMatrixView.MatrixViewMode.EDIT_WEIGHTS) || oldMode.equals(AbstractMatrixView.MatrixViewMode.EDIT_INTERFACES)) &&
                    (newMode.equals(AbstractMatrixView.MatrixViewMode.EDIT_NAMES) || newMode.equals(AbstractMatrixView.MatrixViewMode.EDIT_WEIGHTS) || newMode.equals(AbstractMatrixView.MatrixViewMode.EDIT_INTERFACES)))
                    ||
                    ((oldMode.equals(AbstractMatrixView.MatrixViewMode.STATIC_NAMES) || oldMode.equals(AbstractMatrixView.MatrixViewMode.STATIC_WEIGHTS) || oldMode.equals(AbstractMatrixView.MatrixViewMode.STATIC_INTERFACES)) &&
                    (newMode.equals(AbstractMatrixView.MatrixViewMode.STATIC_NAMES) || newMode.equals(AbstractMatrixView.MatrixViewMode.STATIC_WEIGHTS) || newMode.equals(AbstractMatrixView.MatrixViewMode.STATIC_INTERFACES)))
            ) {
                refresh = false;
            }
            if(refresh) {
                matrixView.refreshView();
            }

        });

        namesView.setToggleGroup(toggleGroup);
        weightsView.setToggleGroup(toggleGroup);
        interfacesView.setToggleGroup(toggleGroup);
        fastRenderView.setToggleGroup(toggleGroup);

        viewMode.getItems().addAll(namesView, weightsView, interfacesView, fastRenderView);


        // set default values of check boxes
        if(matrixData != null) {
            AbstractMatrixView.MatrixViewMode mode = matrixView.getCurrentMode();
            if(mode.equals(AbstractMatrixView.MatrixViewMode.EDIT_NAMES) || mode.equals(AbstractMatrixView.MatrixViewMode.STATIC_NAMES)) {
                namesView.setSelected(true);
            } else if(mode.equals(AbstractMatrixView.MatrixViewMode.EDIT_WEIGHTS) || mode.equals(AbstractMatrixView.MatrixViewMode.STATIC_WEIGHTS)) {
                weightsView.setSelected(true);
            } else if(mode.equals(AbstractMatrixView.MatrixViewMode.EDIT_INTERFACES) || mode.equals(AbstractMatrixView.MatrixViewMode.STATIC_INTERFACES)) {
                interfacesView.setSelected(true);
            } else if(mode.equals(AbstractMatrixView.MatrixViewMode.FAST_RENDER)) {
                fastRenderView.setSelected(true);
            }
        } else {  // default to true if no matrix is open
            namesView.setSelected(true);
        }


        viewMenu.setOnShown(e -> {
            if(matrixData == null) {
                zoomIn.setDisable(true);
                zoomOut.setDisable(true);
                zoomReset.setDisable(true);
                viewMode.setDisable(true);
            } else {
                zoomIn.setDisable(false);
                zoomOut.setDisable(false);
                zoomReset.setDisable(false);
                viewMode.setDisable(false);
            }
        });


        viewMenu.getItems().addAll(zoomIn, zoomOut, zoomReset);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(viewMode);
        viewMenu.getItems().add(configureVisibleInterfaces);

    }


    /**
     * sets up the Menu object for the help menu
     */
    private void setupHelpMenu() {
        MenuItem submitBugReport = new MenuItem("Submit Bug Report");
        submitBugReport.setOnAction(e -> HelpMenu.openBugReportMenu());


        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> HelpMenu.openAboutMenu(menuBar.getScene().getWindow()));

        helpMenu.getItems().addAll(submitBugReport, about);
    }


    /**
     * @return  the menu bar object that has been set up
     */
    public MenuBar getMenuBar() {
        return this.menuBar;
    }


    /**
     * Refreshes the header menu for a new matrix
     *
     * @param matrixData  the matrix data object
     * @param ioHandler   the matrix ioHandler object
     * @param matrixView  the matrix view object
     */
    public void refresh(AbstractDSMData matrixData, AbstractIOHandler ioHandler, AbstractMatrixView matrixView) {
        if(Objects.equals(this.matrixData, matrixData) && Objects.equals(this.ioHandler, ioHandler) && Objects.equals(this.matrixView, matrixView)) {
            return;
        }
        if(this.matrixView != null) {  // disable cross highlighting of old matrix if there was an old matrix
            this.matrixView.disableCrossHighlighting();
            crossHighlight = false;
        }
        this.matrixData = matrixData;
        this.ioHandler = ioHandler;
        this.matrixView = matrixView;

        currentInterfaces = new ArrayList<>();

        menuBar.getMenus().clear();
        fileMenu.getItems().clear();
        editMenu.getItems().clear();
        viewMenu.getItems().clear();
        toolsMenu.getItems().clear();
        helpMenu.getItems().clear();

        setupFileMenu();
        setupEditMenu();
        setUpToolsMenu();
        setupViewMenu();
        setupHelpMenu();
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, helpMenu);
    }


    /**
     * Enables or disables the edit menu
     *
     * @param disabled  if the edit menu should be disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        editMenu.setDisable(disabled);

        RadioMenuItem currentMode = (RadioMenuItem) toggleGroup.getSelectedToggle();
        toggleGroup.selectToggle(null);
        toggleGroup.selectToggle(currentMode);  // force update to the view mode
    }



    public void setupBindings(AbstractEditorTab matrix) {
        ChangeListener<Boolean> refreshListener = (observable, oldValue, newValue) -> {
            if(newValue) {
                refresh(matrix.getMatrixData(), matrix.getMatrixIOHandler(), matrix.getMatrixView());
                matrixData = matrix.getMatrixData();       // update the matrix data
                ioHandler = matrix.getMatrixIOHandler();
                matrixView = matrix.getMatrixView();
            }
            isChanged.set(false);  // clear the changed flag because it is taken care of
        };

        ChangeListener<Boolean> disableListener = (observable, oldValue, newValue) -> setDisabled(!newValue);

        if (matrix != null) {
            this.isChanged.removeListener(refreshListener);
            this.isChanged = matrix.isChanged;
            this.isChanged.addListener(refreshListener);

            refresh(matrix.getMatrixData(), matrix.getMatrixIOHandler(), matrix.getMatrixView());
            matrixData = matrix.getMatrixData();       // update the matrix data
            ioHandler = matrix.getMatrixIOHandler();
            matrixView = matrix.getMatrixView();


            this.isMutable.removeListener(disableListener);
            this.isMutable = matrix.isMutable;
            this.isMutable.addListener(refreshListener);

            setDisabled(!isMutable.get());
        } else {
            refresh(null, null, null);
        }


    }
}
