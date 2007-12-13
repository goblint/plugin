package ee.ut.goblin.actions;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import ee.ut.goblin.AnalysisJob;


public class RunGoblinAction implements IObjectActionDelegate {
    private ISelection selection;
    //private IWorkbenchPart part;

    public RunGoblinAction() {
    }

    public void run(IAction action) {
        if (!(selection instanceof IStructuredSelection))
            return;
        IStructuredSelection structured = (IStructuredSelection) selection;
        Object el = structured.getFirstElement();
        IFile file;
        if (el instanceof ITranslationUnit) {
            ITranslationUnit test = (ITranslationUnit) el;
            //Assert.isTrue(test.getResource() instanceof IFile,"What?? Translation unit should be a file...");
            file = (IFile) test.getResource();
        } else
            file = (IFile) el;
             
        AnalysisJob job = new AnalysisJob(file);
        job.schedule();
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        //part = targetPart;
    }
    
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }
    

}