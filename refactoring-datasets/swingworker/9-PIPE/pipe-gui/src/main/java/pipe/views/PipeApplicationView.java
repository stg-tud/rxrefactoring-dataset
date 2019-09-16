package pipe.views;

import pipe.actions.*;
import pipe.actions.gui.DeleteAction;
import pipe.actions.gui.ExampleFileAction;
import pipe.actions.gui.GuiAction;
import pipe.actions.gui.UnfoldAction;
import pipe.actions.gui.animate.*;
import pipe.actions.gui.create.*;
import pipe.actions.gui.edit.*;
import pipe.actions.gui.file.*;
import pipe.actions.gui.grid.GridAction;
import pipe.actions.gui.create.SelectAction;
import pipe.actions.gui.tokens.ChooseTokenClassAction;
import pipe.actions.gui.tokens.SpecifyTokenAction;
import pipe.actions.gui.window.ExitAction;
import pipe.actions.gui.zoom.SetZoomAction;
import pipe.actions.gui.zoom.ZoomInAction;
import pipe.actions.gui.zoom.ZoomOutAction;
import pipe.controllers.PetriNetController;
import pipe.controllers.PipeApplicationController;
import pipe.controllers.arcCreator.InhibitorCreator;
import pipe.controllers.arcCreator.NormalCreator;
import pipe.exceptions.PetriNetComponentNotFoundException;
import pipe.gui.*;
import pipe.gui.model.PipeApplicationModel;
import pipe.io.JarUtilities;
import pipe.models.component.token.Token;
import pipe.utilities.gui.GuiUtils;
import pipe.views.arc.InhibitorArcHead;
import pipe.views.arc.NormalHead;
import pipe.visitor.connectable.arc.InhibitorSourceVisitor;
import pipe.visitor.connectable.arc.NormalArcSourceVisitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class PipeApplicationView extends JFrame implements ActionListener, Observer, Serializable {
    public final StatusBar statusBar;

    public final GuiAction copyAction;

    public final GuiAction cutAction;

    public final GuiAction pasteAction;

    public final CreateAction arcAction;

    public final CreateAction inhibarcAction;

    public final SpecifyTokenAction specifyTokenClasses;

    public final AnimateAction toggleAnimationAction;

    public final AnimateAction stepbackwardAction;

    public final AnimateAction stepforwardAction;

    public final AnimateAction randomAction;

    public final GuiAction selectAction;

    public final DeleteAction deleteAction;

    final ZoomUI zoomUI = new ZoomUI(1, 0.1, 3, 0.4, this);

    private final SpecifyRateParameterAction specifyRateParameterAction;

    private final JSplitPane moduleAndAnimationHistoryFrame;

    private final JTabbedPane frameForPetriNetTabs = new JTabbedPane();

    private final List<PetriNetTab> petriNetTabs = new ArrayList<>();

    private final PipeApplicationController applicationController;

    private final PipeApplicationModel applicationModel;

    private final GuiAction openAction;

    public JComboBox<String> zoomComboBox;

    public JComboBox<String> tokenClassComboBox;

    public GuiAction printAction = new PrintAction();

    public GuiAction exportPNGAction = new ExportPNGAction();

    public GuiAction exportTNAction = new ExportTNAction();

    public GuiAction exportPSAction = new ExportPSAction();

    public GuiAction importAction = new ImportAction();

    public final ExitAction exitAction;

    public GuiAction undoAction = new UndoAction();

    public GuiAction redoAction = new RedoAction();

    public CreateAction placeAction = new PlaceAction();

    public CreateAction transAction = new ImmediateTransitionAction();

    public CreateAction timedtransAction = new TimedTransitionAction();

    public CreateAction annotationAction = new AnnotationAction();

    public CreateAction tokenAction = new AddTokenAction();

    public CreateAction deleteTokenAction = new DeleteTokenAction();

    public GridAction toggleGrid = new GridAction(this);

    public GuiAction zoomOutAction;

    public GuiAction zoomInAction;

    public GuiAction zoomAction;

    public AnimateAction multipleRandomAction =
            new MultiRandomAnimateAction("Animate", "Randomly fire a number of transitions", "7");

    public UnfoldAction unfoldAction;

    public ChooseTokenClassAction chooseTokenClassAction = new ChooseTokenClassAction(this);

    private JToolBar animationToolBar, drawingToolBar;

    private HelpBox helpAction;

    private JScrollPane scroller;

    private GuiAction createAction = new NewPetriNetAction(this);

    private GuiAction closeAction = new CloseWindowAction(this);

    private final GuiAction saveAction;

    private final GuiAction saveAsAction;

    private List<JLayer<JComponent>> wrappedPetrinetTabs = new ArrayList<>();

    public PipeApplicationView(PipeApplicationController applicationController, PipeApplicationModel applicationModel) {
        ApplicationSettings.register(this);
        this.applicationController = applicationController;
        this.applicationModel = applicationModel;

        inhibarcAction =
                new ArcAction("Inhibitor Arc", "Add an inhibitor arc (alt-h)", KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK,
                        new InhibitorSourceVisitor(), new InhibitorCreator(applicationController, this),
                        applicationController, this, new InhibitorArcHead());
        arcAction = new ArcAction("Arc", "Add an arc (alt-a)", KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK,
                new NormalArcSourceVisitor(), new NormalCreator(applicationController, this), applicationController,
                this, new NormalHead());
        zoomOutAction = new ZoomOutAction(zoomUI);
        zoomInAction = new ZoomInAction(zoomUI);
        zoomAction = new SetZoomAction("Zoom", "Select zoom percentage ", "", applicationController);

        specifyTokenClasses = new SpecifyTokenAction(this, applicationController);
        specifyRateParameterAction = new SpecifyRateParameterAction(applicationController);
        copyAction = new CopyAction(applicationController);
        pasteAction = new PasteAction(applicationController);
        cutAction = new CutAction(applicationController);
        unfoldAction =
                new UnfoldAction(this, applicationController);
        toggleAnimationAction = new ToggleAnimateAction("Animation mode", "Toggle Animation Mode", "Ctrl A", this,
                applicationController);
        stepforwardAction = new StepForwardAction("Forward", "Step forward a firing", "6", this, applicationController);
        randomAction =
                new RandomAnimateAction("Random", "Randomly fire a transition", "5", this, applicationController);
        stepbackwardAction = new StepBackwardAction("Back", "Step backward a firing", "4", this, applicationController);
        selectAction = new SelectAction(this, applicationController);
        deleteAction = new DeleteAction(applicationController);

        FileDialog fileDialog = new FileDialog(this, "Save Petri Net", FileDialog.SAVE);
        fileDialog.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        saveAction = new SaveAction(this, applicationController, fileDialog);
        saveAsAction= new SaveAsAction(this, applicationController, fileDialog);

        FileDialog loadFileDialog = new FileDialog(this, "Open Petri Net", FileDialog.LOAD);
        fileDialog.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        openAction = new OpenAction(applicationController, this, loadFileDialog);

        exitAction = new ExitAction(this, applicationController);

        setTitle(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        this.setIconImage(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(
                ApplicationSettings.getImagePath() + "icon.png")).getImage());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
        this.setLocationRelativeTo(null);

        setExitAction();

        buildMenus();

        // Status bar...
        statusBar = new StatusBar();
        getContentPane().add(statusBar, BorderLayout.PAGE_END);

        // Build menus
        buildToolbar();

        this.setForeground(java.awt.Color.BLACK);
        this.setBackground(java.awt.Color.WHITE);

        ModuleManager moduleManager = new ModuleManager();
        JTree moduleTree = moduleManager.getModuleTree();
        moduleAndAnimationHistoryFrame = new JSplitPane(JSplitPane.VERTICAL_SPLIT, moduleTree, null);
        moduleAndAnimationHistoryFrame.setContinuousLayout(true);
        moduleAndAnimationHistoryFrame.setDividerSize(0);
        JSplitPane pane =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, moduleAndAnimationHistoryFrame, frameForPetriNetTabs);
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        pane.setBorder(null); // avoid multiple borders
        pane.setDividerSize(8);
        getContentPane().add(pane);

        setVisible(true);
        this.applicationModel.setMode(Constants.SELECT);
        selectAction.actionPerformed(null);


        applicationController.createEmptyPetriNet(this);

        setTabChangeListener();

        setZoomChangeListener();
    }

    /**
     * Sets the default behaviour for exit for both Windows/Linux/Mac OS X
     */
    private void setExitAction() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitAction.tryToExit();
            }
        });
    }

    private void setZoomChangeListener() {
        zoomUI.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                getTabComponent().repaint();
                updateZoomCombo();
            }
        });
    }

    private JComponent getTabComponent() {
        return wrappedPetrinetTabs.get(frameForPetriNetTabs.getSelectedIndex());
    }

    /**
     * This method builds the menus for the application
     */
    private void buildMenus() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        addMenuItem(fileMenu, createAction);
        addMenuItem(fileMenu, openAction);
        addMenuItem(fileMenu, closeAction);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, saveAction);
        addMenuItem(fileMenu, saveAsAction);

        fileMenu.addSeparator();
        addMenuItem(fileMenu, importAction);

        // Export menu


        JMenu exportMenu = new JMenu("Export");
        exportMenu.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(
                ApplicationSettings.getImagePath() + "Export.png")));
        addMenuItem(exportMenu, exportPNGAction);
        addMenuItem(exportMenu, exportPSAction);
        addMenuItem(exportMenu, exportTNAction);
        fileMenu.add(exportMenu);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, printAction);
        fileMenu.addSeparator();

        // Example files menu
        JMenu exampleMenu = createExampleFileMenu();

        fileMenu.add(exampleMenu);
        fileMenu.addSeparator();

        addMenuItem(fileMenu, exitAction);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        addMenuItem(editMenu, undoAction);
        addMenuItem(editMenu, redoAction);
        editMenu.addSeparator();
        addMenuItem(editMenu, cutAction);
        addMenuItem(editMenu, copyAction);
        addMenuItem(editMenu, pasteAction);
        addMenuItem(editMenu, deleteAction);

        JMenu drawMenu = new JMenu("Draw");
        drawMenu.setMnemonic('D');
        addMenuItem(drawMenu, selectAction);

        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");

        rootPane.getActionMap().put("ESCAPE", selectAction);

        drawMenu.addSeparator();
        addMenuItem(drawMenu, placeAction);
        addMenuItem(drawMenu, transAction);
        addMenuItem(drawMenu, timedtransAction);
        addMenuItem(drawMenu, arcAction);
        addMenuItem(drawMenu, inhibarcAction);
        addMenuItem(drawMenu, annotationAction);
        drawMenu.addSeparator();
        addMenuItem(drawMenu, tokenAction);
        addMenuItem(drawMenu, deleteTokenAction);
        addMenuItem(drawMenu, specifyTokenClasses);
        addMenuItem(drawMenu, unfoldAction);
        drawMenu.addSeparator();
        addMenuItem(drawMenu, specifyRateParameterAction);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        JMenu zoomMenu = new JMenu("Zoom");
        zoomMenu.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(
                ApplicationSettings.getImagePath() + "Zoom.png")));
        addZoomMenuItems(zoomMenu);

        addMenuItem(viewMenu, zoomOutAction);

        addMenuItem(viewMenu, zoomInAction);
        viewMenu.add(zoomMenu);

        viewMenu.addSeparator();
        addMenuItem(viewMenu, toggleGrid);

        JMenu animateMenu = new JMenu("Animate");
        animateMenu.setMnemonic('A');
        addMenuItem(animateMenu, toggleAnimationAction);
        animateMenu.addSeparator();
        addMenuItem(animateMenu, stepbackwardAction);
        addMenuItem(animateMenu, stepforwardAction);
        addMenuItem(animateMenu, randomAction);
        addMenuItem(animateMenu, multipleRandomAction);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        helpAction = new HelpBox("Help", "View documentation", "F1", "index.htm");
        addMenuItem(helpMenu, helpAction);

        JMenuItem aboutItem = helpMenu.add("About PIPE");
        aboutItem.addActionListener(this); // Help - About is implemented
        // differently

        URL iconURL = Thread.currentThread().getContextClassLoader().getResource(
                ApplicationSettings.getImagePath() + "About.png");
        if (iconURL != null) {
            aboutItem.setIcon(new ImageIcon(iconURL));
        }

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(drawMenu);
        menuBar.add(animateMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

    }

    /**
     * Creates an example file menu based on examples in resources/extras/examples
     */
    private JMenu createExampleFileMenu() {
        JMenu exampleMenu = new JMenu("Examples");
        exampleMenu.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(
                ApplicationSettings.getImagePath() + "Example.png")));
        try {
            URL examplesDirURL = Thread.currentThread().getContextClassLoader().getResource(
                    ApplicationSettings.getExamplesDirectoryPath() + System.getProperty("file.separator"));

            if (JarUtilities.isJarFile(examplesDirURL)) {

                JarFile jarFile = new JarFile(JarUtilities.getJarName(examplesDirURL));

                ArrayList<JarEntry> nets =
                        JarUtilities.getJarEntries(jarFile, ApplicationSettings.getExamplesDirectoryPath());

                Arrays.sort(nets.toArray(), new Comparator() {
                    @Override
                    public int compare(Object one, Object two) {
                        return ((JarEntry) one).getName().compareTo(((JarEntry) two).getName());
                    }
                });

                if (nets.size() > 0) {
                    int index = 0;
                    for (JarEntry net : nets) {
                        if (net.getName().toLowerCase().endsWith(".xml")) {
                            addMenuItem(exampleMenu,
                                    new ExampleFileAction(net, this));
                            index++;
                        }
                    }
                }
            } else {
                /**
                 * The next block fixes a problem that surfaced on Mac OSX with
                 * PIPE 2.4. In that environment (and not in Windows) any blanks
                 * in the project name in Eclipse are property converted to
                 * '%20' but the blank in "extras.examples" is not. The following
                 * code will do nothing on a Windows machine or if the logic on
                 * OSX changess. I also added a stack trace so if the problem
                 * occurs for another environment (perhaps multiple blanks need
                 * to be manually changed) it can be easily fixed. DP
                 */
                // examplesDir = new File(new URI(examplesDirURL.toString()));
                String dirURLString = examplesDirURL.toString();
                int index = dirURLString.indexOf(" ");
                if (index > 0) {
                    StringBuffer sb = new StringBuffer(dirURLString);
                    sb.replace(index, index + 1, "%20");
                    dirURLString = sb.toString();
                }

                File examplesDir = new File(new URI(dirURLString));

                File[] nets = examplesDir.listFiles();

                Arrays.sort(nets, new Comparator() {
                    @Override
                    public int compare(Object one, Object two) {
                        return ((File) one).getName().compareTo(((File) two).getName());
                    }
                });

                // Oliver Haggarty - fixed code here so that if folder contains
                // non
                // .xml file the Example x counter is not incremented when that
                // file
                // is ignored

                if (nets.length > 0) {
                    int k = 0;
                    for (File net : nets) {
                        if (net.getName().toLowerCase().endsWith(".xml")) {
                            addMenuItem(exampleMenu, new ExampleFileAction(net, this));
                        }
                    }

                }
                return exampleMenu;
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error getting example files:" + e);
            e.printStackTrace();
        } finally {
            return exampleMenu;
        }
    }

    /**
     * @param zoomMenu to add to the applications menu bar
     */
    private void addZoomMenuItems(JMenu zoomMenu) {
        for (ZoomAction zoomAction : applicationModel.getZoomActions()) {
            JMenuItem newItem = new JMenuItem(zoomAction);
            zoomMenu.add(newItem);
        }
    }

    private void addMenuItem(JMenu menu, Action action) {
        JMenuItem item = menu.add(action);
        KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

        if (keystroke != null) {
            item.setAccelerator(keystroke);
        }
    }

    private void buildToolbar() {
        // Create the toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);// Inhibit toolbar floating

        addButton(toolBar, createAction);
        addButton(toolBar, openAction);
        addButton(toolBar, saveAction);
        addButton(toolBar, saveAsAction);
        addButton(toolBar, closeAction);
        toolBar.addSeparator();
        addButton(toolBar, printAction);
        toolBar.addSeparator();
        addButton(toolBar, cutAction);
        addButton(toolBar, copyAction);
        addButton(toolBar, pasteAction);
        addButton(toolBar, deleteAction);
        addButton(toolBar, undoAction);
        addButton(toolBar, redoAction);
        toolBar.addSeparator();

        addButton(toolBar, zoomOutAction);
        addZoomComboBox(toolBar, zoomAction);
        addButton(toolBar, zoomInAction);
        toolBar.addSeparator();
        addButton(toolBar, toggleGrid);
        addButton(toolBar, toggleAnimationAction);

        drawingToolBar = new JToolBar();
        drawingToolBar.setFloatable(false);

        toolBar.addSeparator();
        addButton(drawingToolBar, selectAction);
        drawingToolBar.addSeparator();
        addButton(drawingToolBar, placeAction);// Add Draw Menu Buttons
        addButton(drawingToolBar, transAction);
        addButton(drawingToolBar, timedtransAction);
        addButton(drawingToolBar, arcAction);
        addButton(drawingToolBar, inhibarcAction);
        addButton(drawingToolBar, annotationAction);
        drawingToolBar.addSeparator();
        addButton(drawingToolBar, tokenAction);
        addButton(drawingToolBar, deleteTokenAction);
        addTokenClassComboBox(drawingToolBar, chooseTokenClassAction);
        addButton(drawingToolBar, specifyTokenClasses);
        addButton(drawingToolBar, unfoldAction);
        drawingToolBar.addSeparator();
        addButton(drawingToolBar, specifyRateParameterAction);

        toolBar.add(drawingToolBar);

        animationToolBar = new JToolBar();
        animationToolBar.setFloatable(false);
        addButton(animationToolBar, stepbackwardAction);
        addButton(animationToolBar, stepforwardAction);
        addButton(animationToolBar, randomAction);
        addButton(animationToolBar, multipleRandomAction);

        toolBar.add(animationToolBar);
        animationToolBar.setVisible(false);

        toolBar.addSeparator();
        addButton(toolBar, helpAction);

        for (int i = 0; i < toolBar.getComponentCount(); i++) {
            toolBar.getComponent(i).setFocusable(false);
        }

        getContentPane().add(toolBar, BorderLayout.PAGE_START);
    }

    private void addButton(JToolBar toolBar, GuiAction action) {

        if (action.getValue("selected") != null) {
            toolBar.add(new ToggleButton(action));
        } else {
            toolBar.add(action);
        }
    }

    /**
     * @param toolBar the JToolBar to add the button to
     * @param action  the action that the ZoomComboBox performs
     */
    private void addZoomComboBox(JToolBar toolBar, Action action) {
        Dimension zoomComboBoxDimension = new Dimension(65, 28);
        String[] zoomExamples = applicationModel.getZoomExamples();
        zoomComboBox = new JComboBox<>(zoomExamples);
        zoomComboBox.setEditable(true);
        zoomComboBox.setSelectedItem("100%");
        zoomComboBox.setMaximumRowCount(zoomExamples.length);
        zoomComboBox.setMaximumSize(zoomComboBoxDimension);
        zoomComboBox.setMinimumSize(zoomComboBoxDimension);
        zoomComboBox.setPreferredSize(zoomComboBoxDimension);
        zoomComboBox.setAction(action);
        toolBar.add(zoomComboBox);
    }

    /**
     * Creates and adds the token view combo box to the view
     *
     * @param toolBar the JToolBar to add the combo box to
     * @param action  the action that the tokenClassComboBox performs when selected
     */
    protected void addTokenClassComboBox(JToolBar toolBar, Action action) {
        String[] tokenClassChoices = new String[]{"Default"};
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(tokenClassChoices);
        tokenClassComboBox = new JComboBox<>(model);
        tokenClassComboBox.setEditable(true);
        tokenClassComboBox.setSelectedItem(tokenClassChoices[0]);
        tokenClassComboBox.setMaximumRowCount(100);
        tokenClassComboBox.setMaximumSize(new Dimension(125, 100));
        tokenClassComboBox.setEditable(false);
        tokenClassComboBox.setAction(action);
        toolBar.add(tokenClassComboBox);
    }

    // set tabbed pane properties and add change listener that updates tab with
    // linked model and view
    private void setTabChangeListener() {
        frameForPetriNetTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                PetriNetController controller = applicationController.getActivePetriNetController();
                if (controller.isCopyInProgress()) {
                    controller.cancelPaste();
                }


                PetriNetTab petriNetTab = getCurrentTab();
                applicationController.setActiveTab(petriNetTab);

                if (frameForPetriNetTabs.getTabCount() > 0) {
                    petriNetTab.setVisible(true);
                    petriNetTab.repaint();
                    updateZoomCombo();

                    enableActions(!petriNetTab.isInAnimationMode(), applicationController.isPasteEnabled());

                    setTitle(petriNetTab.getName());

                    setAnimationMode(petriNetTab.isInAnimationMode());
                }

                refreshTokenClassChoices();
            }
        });
    }

    public void setAnimationMode(boolean animateMode) {
        enableActions(!animateMode);

        stepforwardAction.setEnabled(false);
        stepbackwardAction.setEnabled(false);
        multipleRandomAction.setSelected(false);
        toggleAnimationAction.setSelected(animateMode);

        PetriNetTab petriNetTab = getCurrentTab();
        petriNetTab.changeAnimationMode(animateMode);

        PetriNetController petriNetController = applicationController.getActivePetriNetController();
        if (animateMode) {
            enableActions(false, petriNetController.isPasteEnabled());// disables all non-animation buttons
            applicationModel.setEditionAllowed(false);
            statusBar.changeText(statusBar.textforAnimation);
            createAnimationViewPane();

        } else {
            applicationModel.setEditionAllowed(true);
            statusBar.changeText(statusBar.textforDrawing);
            removeAnimationViewPlane();
            enableActions(true, petriNetController.isPasteEnabled()); // renables all non-animation buttons
        }
    }

    /* sets all buttons to enabled or disabled according to status. */
    public void enableActions(boolean status) {
        if (status) {
            drawingToolBar.setVisible(true);
            animationToolBar.setVisible(false);
        }

        if (!status) {
            drawingToolBar.setVisible(false);
            animationToolBar.setVisible(true);
        }
    }

    /**
     * Creates a new currentAnimationView text area, and returns a reference to it
     */
    private void createAnimationViewPane() {
        AnimationHistoryView animationHistoryView = getCurrentTab().getAnimationView();
        scroller = new JScrollPane(animationHistoryView);
        scroller.setBorder(new EmptyBorder(0, 0, 0, 0)); // make it less bad on XP

        moduleAndAnimationHistoryFrame.setBottomComponent(scroller);

        moduleAndAnimationHistoryFrame.setDividerLocation(0.5);
        moduleAndAnimationHistoryFrame.setDividerSize(8);
    }

    void removeAnimationViewPlane() {
        if (scroller != null) {
            moduleAndAnimationHistoryFrame.remove(scroller);
            moduleAndAnimationHistoryFrame.setDividerLocation(0);
            moduleAndAnimationHistoryFrame.setDividerSize(0);
        }
    }

    /**
     * Remove the listener from the zoomComboBox, so that when
     * the box's selected item is updated to keep track of ZoomActions
     * called from other sources, a duplicate ZoomAction is not called
     */
    public void updateZoomCombo() {
        ActionListener zoomComboListener = (zoomComboBox.getActionListeners())[0];
        zoomComboBox.removeActionListener(zoomComboListener);

        String zoomPercentage = zoomUI.getPercentageZoom() + "%";
        zoomComboBox.setSelectedItem(zoomPercentage);
        zoomComboBox.addActionListener(zoomComboListener);
    }

    /**
     * Refreshes the combo box that presents the Tokens available for use.
     * If there are no Petri nets being displayed this clears it
     */
    public void refreshTokenClassChoices() {
        if (areAnyTabsDisplayed()) {
            String[] tokenClassChoices = buildTokenClassChoices();
            ComboBoxModel<String> model = new DefaultComboBoxModel<>(tokenClassChoices);
            tokenClassComboBox.setModel(model);
            PetriNetController controller = applicationController.getActivePetriNetController();
            try {
                controller.selectToken(getSelectedTokenName());
            } catch (PetriNetComponentNotFoundException petriNetComponentNotFoundException) {
                GuiUtils.displayErrorMessage(this, petriNetComponentNotFoundException.getMessage());
            }
        } else {
            tokenClassComboBox.setModel(new DefaultComboBoxModel<String>());
        }
    }

    /**
     * @return names of Tokens for the combo box
     */
    protected String[] buildTokenClassChoices() {
        if (areAnyTabsDisplayed()) {
            PetriNetController petriNetController = applicationController.getActivePetriNetController();
            Collection<Token> tokens = petriNetController.getNetTokens();
            String[] tokenClassChoices = new String[tokens.size()];
            int index = 0;
            for (Token token : tokens) {
                tokenClassChoices[index] = token.getId();
                index++;
            }
            return tokenClassChoices;
        }
        return new String[0];
    }

    /**
     * @return true if any tabs are displayed
     */
    public boolean areAnyTabsDisplayed() {
        return applicationController.getActivePetriNetController() != null;
    }

    public String getSelectedTokenName() {
        ComboBoxModel<String> model = tokenClassComboBox.getModel();
        Object selected = model.getSelectedItem();
        return selected.toString();
    }

    public PetriNetTab getCurrentTab() {
        int index = frameForPetriNetTabs.getSelectedIndex();
        return getTab(index);
    }

    PetriNetTab getTab(int index) {
        if (index < 0 || index >= petriNetTabs.size()) {
            return null;
        }
        return petriNetTabs.get(index);
    }

    private void enableActions(boolean editMode, boolean pasteEnabled) {
        saveAction.setEnabled(editMode);
        saveAsAction.setEnabled(editMode);

        placeAction.setEnabled(editMode);
        arcAction.setEnabled(editMode);
        inhibarcAction.setEnabled(editMode);
        annotationAction.setEnabled(editMode);
        transAction.setEnabled(editMode);
        timedtransAction.setEnabled(editMode);
        tokenAction.setEnabled(editMode);
        deleteAction.setEnabled(editMode);
        selectAction.setEnabled(editMode);
        deleteTokenAction.setEnabled(editMode);
        specifyRateParameterAction.setEnabled(editMode);
        //toggleGrid.setEnabled(status);

        if (editMode) {
            toggleAnimationAction.setSelected(false);
            multipleRandomAction.setSelected(false);
            stepbackwardAction.setEnabled(false);
            stepforwardAction.setEnabled(false);
            pasteAction.setEnabled(pasteEnabled);
        } else {
            pasteAction.setEnabled(true);
            undoAction.setEnabled(true);
            redoAction.setEnabled(true);
        }
        randomAction.setEnabled(!editMode);
        multipleRandomAction.setEnabled(!editMode);
        copyAction.setEnabled(editMode);
        cutAction.setEnabled(editMode);
        deleteAction.setEnabled(editMode);

    }

    @Override
    public final void setTitle(String title) {
        String name = applicationModel.getName();
        super.setTitle((title == null) ? name : name + ": " + title);
    }

    public JTabbedPane getFrameForPetriNetTabs() {
        return frameForPetriNetTabs;
    }

    /**
     * Displays contributors
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        JOptionPane.showMessageDialog(this, "PIPE: Platform Independent Petri Net Ediror\n\n" + "Authors:\n" +
                "2003: Jamie Bloom, Clare Clark, Camilla Clifford, Alex Duncan, Haroun Khan and Manos Papantoniou\n" +
                "2004: Tom Barnwell, Michael Camacho, Matthew Cook, Maxim Gready, Peter Kyme and Michail Tsouchlaris\n"
                +
                "2005: Nadeem Akharware\n" + "????: Tim Kimber, Ben Kirby, Thomas Master, Matthew Worthington\n" +
                "????: Pere Bonet Bonet (Universitat de les Illes Balears)\n" +
                "????: Marc Meli\u00E0 Aguil\u00F3 (Universitat de les Illes Balears)\n" +
                "2010: Alex Charalambous (Imperial College London)\n" +
                "2011: Jan Vlasak (Imperial College London)\n\n" + "http://pipe2.sourceforge.net/", "About PIPE",
                JOptionPane.INFORMATION_MESSAGE);
    }

    //TODO: Find out if this actually ever gets called
    @Override
    public void update(Observable o, Object obj) {
        System.out.println("UPDATE APPLICATION VIEW");
        PetriNetTab currentTab = getCurrentTab();
        if ((applicationModel.getMode() != Constants.CREATING) && (!currentTab.isInAnimationMode())) {
//            currentTab.setNetChanged(true);
        }
    }

    /**
     * Adds the tab to the main application view in the tabbed view frame
     * @param name name of tab
     * @param tab tab to add
     */
    //TODO: ADD SCROLL PANE
    public void addNewTab(String name, PetriNetTab tab) {

        //        JScrollPane scroller = new JScrollPane(tab);
        //        scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));

        JLayer<JComponent> jLayer = new JLayer<>(tab, zoomUI);
        wrappedPetrinetTabs.add(jLayer);

        petriNetTabs.add(tab);
        frameForPetriNetTabs.addTab(name, null, jLayer, null);
        frameForPetriNetTabs.setSelectedIndex(petriNetTabs.size() - 1);
    }

    public File getFile() {
        PetriNetTab petriNetTab = petriNetTabs.get(frameForPetriNetTabs.getSelectedIndex());
        return petriNetTab._appFile;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void close() {
        exitAction.actionPerformed(null);
    }

    public void setUndoActionEnabled(boolean flag) {
        undoAction.setEnabled(flag);
    }

    public void setRedoActionEnabled(boolean flag) {
        redoAction.setEnabled(flag);
    }

    //TODO: DELETE!
    public PetriNetView getCurrentPetriNetView() {
        return null;
    }

    public void setStepForward(boolean stepForwardAllowed) {
        stepforwardAction.setEnabled(stepForwardAllowed);
    }

    public void setStepBackward(boolean stepBackwardAllowed) {
        stepbackwardAction.setEnabled(stepBackwardAllowed);
    }

    public void removeCurrentTab() {
        removeTab(frameForPetriNetTabs.getSelectedIndex());
    }

    public void removeTab(int index) {
        petriNetTabs.remove(index);
        if ((frameForPetriNetTabs.getTabCount() > 0)) {
            frameForPetriNetTabs.remove(index);
        }
    }

    public void updateSelectedTabName(String title) {
        int index = frameForPetriNetTabs.getSelectedIndex();
        frameForPetriNetTabs.setTitleAt(index, title);
    }
}

