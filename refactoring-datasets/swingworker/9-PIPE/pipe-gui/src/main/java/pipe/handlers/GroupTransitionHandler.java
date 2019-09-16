package pipe.handlers;

import pipe.controllers.PetriNetController;
import pipe.controllers.PipeApplicationController;
import pipe.gui.ApplicationSettings;
import pipe.historyActions.HistoryItem;
import pipe.historyActions.HistoryManager;
import pipe.views.GroupTransitionView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Class used to implement methods corresponding to mouse events on transitions.
 */
public class GroupTransitionHandler
        extends ConnectableHandler {
    //implements java.awt.event.MouseWheelListener {  //NOU-PERE


    public GroupTransitionHandler(GroupTransitionView view, Container contentpane, GroupTransitionView obj, PetriNetController controller) {
        super(view, contentpane, null, controller);
        //TODO: FIX THIS BACK IN RATHER THAN NULL;
//      super(contentpane, obj);
    }


    public void mouseWheelMoved(MouseWheelEvent e) {

        if (!ApplicationSettings.getApplicationModel().isEditionAllowed() ||
                e.isControlDown()) {
            return;
        }

        int rotation = 0;
        if (e.getWheelRotation() < 0) {
            rotation = -e.getWheelRotation() * 135;
        } else {
            rotation = e.getWheelRotation() * 45;
        }
        PipeApplicationController controller = ApplicationSettings.getApplicationController();
        PetriNetController petriNetController = controller.getActivePetriNetController();
        HistoryManager historyManager = petriNetController.getHistoryManager();
        historyManager.addNewEdit(((GroupTransitionView) component).rotate(rotation));

    }


    /**
     * Creates the popup menu that the user will see when they right click on a
     * component
     */
    protected JPopupMenu getPopup(MouseEvent e) {
        int index = 0;
        JPopupMenu popup = super.getPopup(e);

        JMenuItem menuItem = new JMenuItem("Edit Transition");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((GroupTransitionView) component).showEditor();
            }
        });
        popup.insert(menuItem, index++);

        popup.insert(new JPopupMenu.Separator(), index);
        menuItem = new JMenuItem("Ungroup Transitions");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HistoryItem edit = ((GroupTransitionView) component).ungroupTransitions();


                PipeApplicationController controller = ApplicationSettings.getApplicationController();
                PetriNetController petriNetController = controller.getActivePetriNetController();
                HistoryManager historyManager = petriNetController.getHistoryManager();
                historyManager.addNewEdit(edit);
/*    		PetriNet model = Pipe.getCurrentPetriNetView();
            model.removePetriNetObject(((GroupTransition)component));
    		PetriNetTab view = Pipe.getCurrentTab();
    		view.remove(((GroupTransition)component));*/
            }
        });
        popup.insert(menuItem, index++);

        return popup;
    }


    public void mouseClicked(MouseEvent e) {
//      if (SwingUtilities.isLeftMouseButton(e)){
//          if (e.getClickCount() == 2 &&
//                 ApplicationSettings.getApplicationModel().isEditionAllowed() &&
//                 (ApplicationSettings.getApplicationModel().getMode() == Constants.TIMEDTRANS ||
//                 ApplicationSettings.getApplicationModel().getMode() == Constants.IMMTRANS ||
//                 ApplicationSettings.getApplicationModel().getMode() == Constants.SELECT)) {
//            ((GroupTransitionView) component).showEditor();
//         }
//      }  else if (SwingUtilities.isRightMouseButton(e)){
//          if (ApplicationSettings.getApplicationModel().isEditionAllowed() && enablePopup) {
//            JPopupMenu m = getPopup(e);
//            if (m != null) {
//               int x = ZoomController.getZoomedValue(
//                       component.getNameOffsetXObject().intValue(),
//                       component.getZoomPercentage());
//               int y = ZoomController.getZoomedValue(
//                       component.getNameOffsetYObject().intValue(),
//                       component.getZoomPercentage());
//               m.show(component, x, y);
//            }
//         }
//      }
    }

}
