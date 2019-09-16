package pipe.actions.file;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pipe.actions.gui.file.NewPetriNetAction;
import pipe.views.PipeApplicationView;

import javax.swing.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class NewPetriNetActionTest {
    NewPetriNetAction createAction;

    @Mock
    PipeApplicationView mockView;

    @Before
    public void setUp() {
        createAction = new NewPetriNetAction(mockView);
    }

    @Test
    public void actionPerformed() {
        createAction.actionPerformed(null);
    }

    @Test
    public void setShortDescription() {
        Object shortDescription = createAction.getValue(Action.SHORT_DESCRIPTION);
        assertEquals("Create a new Petri net", shortDescription);
    }

    @Test
    public void setKeyboardShortcut() {
        Object acceleratorKey = createAction.getValue(Action.ACCELERATOR_KEY);
        KeyStroke stroke = KeyStroke.getKeyStroke("meta N");
        assertEquals(stroke, acceleratorKey);
    }
}
