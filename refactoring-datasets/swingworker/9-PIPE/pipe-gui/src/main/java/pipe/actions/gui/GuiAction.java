/*
 * Created on 07-Mar-2004
 */
package pipe.actions.gui;

import pipe.gui.ApplicationSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;


/**
 * GuiAction class
 *
 * @author Maxim and others
 *         <p/>
 *         Handles loading icon based on action name and setting up other stuff
 *         <p/>
 *         Toggleable actions store the toggle state in a way that allows ChangeListeners
 *         to be notified of changes
 */
public abstract class GuiAction
        extends AbstractAction
{

    /**
     *
     * @param name image name
     * @param tooltip tooltip message
     * @param key {@link java.awt.event.KeyEvent} key
     * @param modifiers e.g. ctrl/shift obtained from {@link java.awt.event.InputEvent}
     */
    protected GuiAction(String name, String tooltip, int key, int modifiers) {
        this(name, tooltip);

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, modifiers));
    }

    protected GuiAction(String name, String tooltip) {
        super(name);
        URL iconURL = Thread.currentThread().getContextClassLoader().getResource(ApplicationSettings.getImagePath() + name + ".png");
        if(iconURL != null)
        {
            putValue(SMALL_ICON, new ImageIcon(iconURL));
        }

        if(tooltip != null)
        {
            putValue(SHORT_DESCRIPTION, tooltip);
        }
    }

    protected GuiAction(String name, String tooltip, String keystroke)
    {

        super(name);
        URL iconURL = Thread.currentThread().getContextClassLoader().getResource(ApplicationSettings.getImagePath() + name + ".png");
        if(iconURL != null)
        {
            putValue(SMALL_ICON, new ImageIcon(iconURL));
        }

        if(tooltip != null)
        {
            putValue(SHORT_DESCRIPTION, tooltip);
        }

        if(keystroke != null)
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keystroke));
        }
    }


    protected GuiAction(String name, String tooltip, String keystroke,
                        boolean toggleable)
    {
        this(name, tooltip, keystroke);
        putValue("selected", Boolean.FALSE);
    }


    public boolean isSelected()
    {
        Boolean b = (Boolean) getValue("selected");

        if(b != null)
        {
            return b.booleanValue();
        }
        return false; // default
    }


    public void setSelected(boolean selected)
    {
        Boolean b = (Boolean) getValue("selected");

        if(b != null)
        {
            putValue("selected", null);
            putValue("selected", Boolean.valueOf(selected));
        }
    }

}
