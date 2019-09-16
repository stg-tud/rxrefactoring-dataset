package pipe.actions.gui.edit;

import pipe.actions.gui.GuiAction;
import pipe.controllers.PetriNetController;
import pipe.controllers.PipeApplicationController;
import pipe.gui.ApplicationSettings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class RedoAction extends GuiAction {

    public RedoAction() {
        super("Redo", "Redo (Ctrl-Y)",  KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        PipeApplicationController applicationController = ApplicationSettings
                .getApplicationController();
        PetriNetController controller = applicationController.getActivePetriNetController();
        controller.getHistoryManager().doRedo();
    }
}