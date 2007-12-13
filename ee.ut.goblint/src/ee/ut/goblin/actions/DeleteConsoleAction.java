/*
 * Created on Feb 16, 2005
 *
 */
package ee.ut.goblin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.*;

/**
 * @author vesal
 *
 */
public class DeleteConsoleAction implements IViewActionDelegate {

    //private IViewPart view;

    /**
     * 
     */
    public DeleteConsoleAction() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        //this.view = view;

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IConsoleManager cm = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = cm.getConsoles();
        IConsole[] toDelete = new IConsole[consoles.length];
        for (int i = 0; i < consoles.length; i++) {
            IConsole console = consoles[i];
            if (console.getName().equals("Analysis results"))
                toDelete[i] = console;
        }
        cm.removeConsoles(toDelete);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
