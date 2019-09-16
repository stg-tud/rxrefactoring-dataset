package pipe.views;

import net.sourceforge.jeval.EvaluationException;
import org.jfree.util.ShapeUtilities;
import pipe.controllers.PetriNetController;
import pipe.controllers.TransitionController;
import pipe.gui.ApplicationSettings;
import pipe.gui.Constants;
import pipe.gui.PetriNetTab;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.TransitionEditorPanel;
import pipe.handlers.TransitionAnimationHandler;
import pipe.handlers.TransitionHandler;
import pipe.historyActions.ClearRateParameter;
import pipe.historyActions.GroupTransition;
import pipe.historyActions.HistoryItem;
import pipe.models.component.rate.NormalRate;
import pipe.models.component.transition.Transition;
import pipe.models.petrinet.ExprEvaluator;
import pipe.models.petrinet.PetriNet;
import pipe.views.viewComponents.RateParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;


public class TransitionView extends ConnectableView<Transition> {
    public boolean _highlighted;

    private boolean _enabled;

    private boolean _enabledBackwards;

    private double _delay;

    private boolean _delayValid;

    private RateParameter _rateParameter;

    private GroupTransitionView _groupTransitionView;

    public TransitionView(Transition model, PetriNetController controller) {
        super(model.getId(), model, controller, new Rectangle2D.Double(0, 0, model.getWidth(),
                model.getHeight()));
//        constructTransition();
        setChangeListener();

        _enabled = false;
        _enabledBackwards = false;
        _highlighted = false;

        rotate(model.getAngle());
        //TODO: DEBUG WHY CANT CALL THIS IN CONSTRUCTOR
        //        changeToolTipText();

    }

//    private void constructTransition() {
//        shape.append(), false);
//    }

    public void rotate(int angleInc) {
        ShapeUtilities.rotateShape(shape, Math.toRadians(angleInc), new Double(model.getCentre().getX()).floatValue(), new Double(model.getCentre().getY()).floatValue());
//        shape.transform(AffineTransform.getRotateInstance(Math.toRadians(angleInc), model.getHeight() / 2,
//                model.getHeight() / 2));
    }

    private void setChangeListener() {
        model.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String name = propertyChangeEvent.getPropertyName();
                if (name.equals(Transition.PRIORITY_CHANGE_MESSAGE) || name.equals(Transition.RATE_CHANGE_MESSAGE)) {
                    repaint();
                } else if (name.equals(Transition.ANGLE_CHANGE_MESSAGE) || name.equals(Transition.TIMED_CHANGE_MESSAGE)
                        || name.equals(Transition.INFINITE_SEVER_CHANGE_MESSAGE)) {
                    repaint();
                } else if (name.equals(Transition.ENABLED_CHANGE_MESSAGE) || name.equals(
                        Transition.DISABLED_CHANGE_MESSAGE)) {
                    repaint();
                }
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public void setEnabled(boolean status) {
        if (_enabled && !status) {
            _delayValid = false;
        }
        if (_groupTransitionView != null) {
            _groupTransitionView.setEnabled(status);
        }
        _enabled = status;

    }

    @Override
    public void addedToGui() {
        super.addedToGui();
        update();
    }

    public void update() {
        this.repaint();
    }

    @Override
    public void delete() {
        if (_rateParameter != null) {
            _rateParameter.remove(this);
            _rateParameter = null;
        }
        super.delete();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected() && !_ignoreSelection) {
            g2.setColor(Constants.SELECTION_FILL_COLOUR);
        } else {
            g2.setColor(Constants.ELEMENT_FILL_COLOUR);
        }

        if (model.isTimed()) {
            if (model.isInfiniteServer()) {
                for (int i = 2; i >= 1; i--) {
                    g2.translate(2 * i, -2 * i);
                    g2.fill(shape);
                    Paint pen = g2.getPaint();
                    if (highlightView()) {
                        g2.setPaint(Constants.ENABLED_TRANSITION_COLOUR);
                    } else if (isSelected() && !_ignoreSelection) {
                        g2.setPaint(Constants.SELECTION_LINE_COLOUR);
                    } else {
                        g2.setPaint(Constants.ELEMENT_LINE_COLOUR);
                    }
                    g2.draw(shape);
                    g2.setPaint(pen);
                    g2.translate(-2 * i, 2 * i);
                }
            }
            g2.fill(shape);
        }

        if (highlightView()) {
            g2.setPaint(Constants.ENABLED_TRANSITION_COLOUR);
        } else if (isSelected() && !_ignoreSelection) {
            g2.setPaint(Constants.SELECTION_LINE_COLOUR);
        } else {
            g2.setPaint(Constants.ELEMENT_LINE_COLOUR);
        }

        g2.draw(shape);
        if (!model.isTimed()) {
            if (model.isInfiniteServer()) {
                for (int i = 2; i >= 1; i--) {
                    g2.translate(2 * i, -2 * i);
                    Paint pen = g2.getPaint();
                    g2.setPaint(Constants.ELEMENT_FILL_COLOUR);
                    g2.fill(shape);
                    g2.setPaint(pen);
                    g2.draw(shape);
                    g2.translate(-2 * i, 2 * i);
                }
            }
            g2.draw(shape);
            g2.fill(shape);
        }
        changeToolTipText();
    }

    @Override
    void setCentre(double x, double y) {
        super.setCentre(x, y);
        update();
    }

    @Override
    public void showEditor() {
        EscapableDialog guiDialog = new EscapableDialog(ApplicationSettings.getApplicationView(), "PIPE2", true);
        TransitionEditorPanel te = new TransitionEditorPanel(guiDialog.getRootPane(),
                petriNetController.getTransitionController(this.model), petriNetController);
        guiDialog.add(te);
        guiDialog.getRootPane().setDefaultButton(null);
        guiDialog.setResizable(false);
        guiDialog.pack();
        guiDialog.setLocationRelativeTo(null);
        guiDialog.setVisible(true);
        guiDialog.dispose();
    }

    @Override
    public void toggleAttributesVisible() {
        _attributesVisible = !_attributesVisible;
    }

    private void changeToolTipText() {
        try {
            Double.parseDouble(getRateExpr());
            if (this.isTimed()) {
                setToolTipText("r = " + this.getRate());
            } else {
                setToolTipText("\u03c0 = " + this.getPriority() + "; w = " + this.getRate());
            }
        } catch (Exception e) {
            if (this.isTimed()) {
                setToolTipText("r = " + this.getRateExpr() + " = " + this.getRate());
            } else {
                setToolTipText(
                        "\u03c0 = " + this.getPriority() + "; w = " + this.getRateExpr() + " = " + this.getRate());
            }
        }
    }

    public double getRate() {
        if (isInfiniteServer()) {
            PetriNet petriNet = petriNetController.getPetriNet();
            return petriNet.getEnablingDegree(model);
        }

        if (model.getRateExpr() == null) {
            return -1;
        }
        try {
            return Double.parseDouble(model.getRateExpr());
        } catch (Exception e) {
            ExprEvaluator parser = new ExprEvaluator(petriNetController.getPetriNet());
            try {
                return parser.parseAndEvalExprForTransition(model.getRateExpr());
            } catch (EvaluationException ee) {
                showErrorMessage();
                return 1.0;
            }

        }
    }

    private void showErrorMessage() {
        String message =
                "Errors in marking-dependent transition rate expression." + "\r\n The computation should be aborted";
        String title = "Error";
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.YES_NO_OPTION);
    }

    public boolean isInfiniteServer() {
        return model.isInfiniteServer();
    }

    public String getRateExpr() {
        return model.getRateExpr();
    }

    public boolean isTimed() {
        return model.isTimed();
    }

    public int getPriority() {
        return model.
                getPriority();
    }

    /**
     * @return true if in animate mode and the model is enabled
     */
    private boolean highlightView() {
        //TODO: GET THIS IN A BETTER WAY
        PipeApplicationView view = ApplicationSettings.getApplicationView();
        PetriNetTab tab = view.getCurrentTab();

        return model.isEnabled() && tab.isInAnimationMode();
    }

    private String getAttributes() {
        if (_attributesVisible) {
            try {
                Double.parseDouble(getRateExpr());
                if (isInfiniteServer()) {
                    return "\nr=" + "ED(" + this.getId() + ",M)=" + getRate();
                }
                if (isTimed()) {
                    if (_rateParameter != null) {
                        return "\nr=" + _rateParameter.getName();
                    } else {
                        return "\nr=" + getRate();
                    }
                } else {
                    if (_rateParameter != null) {
                        return "\n" + '\u03c0' + "=" + model.getPriority() + "\nw=" + _rateParameter.getName();
                    } else {
                        return "\n" + '\u03c0' + "=" + model.getPriority() + "\nw=" + getRate();
                    }
                }
            } catch (Exception e) {
                if (isInfiniteServer()) {
                    return "\nr=" + "ED(" + this.getId() + ",M)=" + getRate();
                }
                if (isTimed()) {
                    if (_rateParameter != null) {
                        return "\nr=" + _rateParameter.getName();
                    } else {
                        return "\nr=" + getRateExpr() + "=" + getRate();
                    }
                } else {
                    if (_rateParameter != null) {
                        return "\n" + '\u03c0' + "=" + model.getPriority() + "\nw=" + _rateParameter.getName();
                    } else {
                        return "\n" + '\u03c0' + "=" + model.getPriority() + "\nw=" + getRateExpr() + "=" + getRate();
                    }
                }
            }
        }
        return "";
    }

    @Override
    public void addToPetriNetTab(PetriNetTab tab) {
        addLabelToContainer(tab);

        TransitionHandler transitionHandler = new TransitionHandler(this, tab, this.model, petriNetController);
        addMouseListener(transitionHandler);
        addMouseMotionListener(transitionHandler);
        addMouseWheelListener(transitionHandler);

        MouseListener transitionAnimationHandler = new TransitionAnimationHandler(this.model, tab);
        addMouseListener(transitionAnimationHandler);
    }

    public boolean isEnabled(boolean animationStatus) {
        if (_groupTransitionView != null) {
            _groupTransitionView.isEnabled(animationStatus);
        }
        if (animationStatus) {
            if (_enabled) {
                _highlighted = true;
                return true;
            } else {
                _highlighted = false;
            }
        }
        return false;
    }

    public boolean isEnabledBackwards() {
        return _enabledBackwards;
    }

    public void setEnabledBackwards(boolean status) {
        _enabledBackwards = status;
        if (_groupTransitionView != null) {
            _groupTransitionView.setEnabledBackwards(status);
        }
    }

    public void setHighlighted(boolean status) {
        if (_groupTransitionView != null) {
            _groupTransitionView.setHighlighted(status);
        }
        _highlighted = status;
    }

    //TODO: RE-IMPLEMENT
    public HistoryItem setInfiniteServer(boolean status) {
        throw new RuntimeException("THIS SHOULD BE IMPLEMENTED IN CONTROLLER");
        //        _infiniteServer = status;
        //        repaint();
        //        return new TransitionInfiniteServer(this);
    }

    public void setEnabledFalse() {
        _enabled = false;
        _highlighted = false;
        if (_groupTransitionView != null) {
            _groupTransitionView.setEnabled(false);
        }
    }

    public int getAngle() {
        return model.getAngle();
    }

    public HistoryItem setPriority(int newPriority) {
        //        int oldPriority = getPriority();
        //
        //        model.setPriority(newPriority);
        //        _nameLabel.setText(getAttributes());
        //        repaint();
        //        return new TransitionPriority(this, oldPriority, model.getPriority());
        return null;
    }

    public double getDelay() {
        return _delay;
    }

    public void setDelay(double _delay) {
        this._delay = _delay;
        _delayValid = true;
    }

    @Override
    public boolean contains(int x, int y) {
        return shape.contains(x,y);
    }

    public RateParameter getRateParameter() {
        return _rateParameter;
    }

    //TODO: DELETE
    public HistoryItem setRateParameter(RateParameter rateParameter) {
        throw new RuntimeException("SHOULD NOT BE HERE DELETE THIS CODE");
    }

    public HistoryItem clearRateParameter() {
        RateParameter oldRateParameter = _rateParameter;
        _rateParameter.remove(this);
        _rateParameter = null;
        update();
        return new ClearRateParameter(this, oldRateParameter);
    }

    //TODO: DELETE
    public HistoryItem changeRateParameter(RateParameter rateParameter) {
        throw new RuntimeException("SHOULD NOT BE HERE DELETE THIS CODE");
        //        RateParameter oldRateParameter = this._rateParameter;
        //        this._rateParameter.remove(this);
        //        this._rateParameter = rateParameter;
        //        this._rateParameter.add(this);
        //        model.setRateExpr(rateParameter.getValue());
        //        update();
        //        return new ChangeRateParameter(this, oldRateParameter, this._rateParameter);
    }

    public void setModel(Transition model) {
        this.model = model;
    }

    public void bindToGroup(GroupTransitionView groupTransitionView) {
        this._groupTransitionView = groupTransitionView;
    }

    public boolean isGrouped() {
        return _groupTransitionView != null;
    }

    public GroupTransitionView getGroup() {
        return _groupTransitionView;
    }

    public void ungroupTransition() {
        _groupTransitionView = null;
    }

    public int confirmOrTimeout(String message, String title) throws HeadlessException {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
        return showWithTimeout(pane, null, title);
    }

    private int showWithTimeout(JOptionPane pane, Component parent, String title) {
        final JDialog dialog = pane.createDialog(parent, title);
        Thread timeoutThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException ex) {
                }
                javax.swing.SwingUtilities.invokeLater(new Runnable() //   from the event dispatch
                {
                    @Override
                    public void run() {
                        dialog.setVisible(false);
                    }
                });            //   thread
            }
        };
        timeoutThread.start();
        dialog.setVisible(true);
        Object selection = pane.getValue();                      // We get to this point when
        int result = JOptionPane.CLOSED_OPTION;                   // (1) The user makes a selection
        if (selection != null && selection instanceof Integer)        // or (2) the timeout thread closes
        {
            result = ((Integer) selection).intValue();             // the dialog.
        }
        return result;
    }

    public HistoryItem groupTransitions() {
        ArrayList<TransitionView> transitionsToHide = groupTransitionsValidation();
        GroupTransitionView newGroupTransitionView = new GroupTransitionView(this, model.getX(), model.getY());
        groupTransitionsHelper(transitionsToHide, newGroupTransitionView);
        return new GroupTransition(newGroupTransitionView);
    }

    public void groupTransitionsHelper(ArrayList<TransitionView> transitionsToHide,
                                       GroupTransitionView newGroupTransitionView) {
    }

    private ArrayList<TransitionView> groupTransitionsValidation() {
        ArrayList<TransitionView> transitionsToHide = new ArrayList<TransitionView>();
        return transitionsToHide;
    }

    public void hideFromCanvas() {
        this.setVisible(false);
    }

    public void unhideFromCanvas() {
        this.setVisible(true);
    }

    public void hideAssociatedArcs() {
    }

    public void showAssociatedArcs() {
    }

    //TODO: DELETE
    public HistoryItem setRate(double rate) {
        throw new RuntimeException("SHOULD NOT BE HERE DELETE THIS CODE");
        //        String oldRate = model.getRateExpr();
        //        model.setRateExpr(rate + "");
        //        repaint();
        //        return new TransitionRate(this, oldRate, model.getRateExpr());
    }
}
