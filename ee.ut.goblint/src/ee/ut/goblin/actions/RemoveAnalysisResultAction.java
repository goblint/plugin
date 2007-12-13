/*
 * Created on Feb 16, 2005
 *
 */
package ee.ut.goblin.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ee.ut.goblin.GoblinPlugin;

/**
 * @author kalmer
 *
 */
public class RemoveAnalysisResultAction implements IViewActionDelegate {
    public RemoveAnalysisResultAction() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
    	IWorkbench wb = PlatformUI.getWorkbench();
    	IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
    	IWorkbenchPage page = win.getActivePage();
    	IEditorPart editor = page.getActiveEditor();
    	IFile original = ((FileEditorInput)editor.getEditorInput()).getFile();
    	try { original.getProject().setSessionProperty(GoblinPlugin.RESULT_NAME, null);
		} catch (CoreException e) {}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
