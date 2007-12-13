package ee.ut.goblin.views;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

import ee.ut.goblin.TreeAnalysisMap;
import ee.ut.goblin.GoblinPlugin;
import ee.ut.goblin.XMLJob;



public class ResultView extends ViewPart implements ISelectionListener {
	private TreeViewer viewer;
	private Action action1;
	private Action doubleClickAction;

	private IFile currentFile;
	private TreeState state;
	private int currentView;

	class ViewContentProvider implements ITreeContentProvider {
		private Object[] failNode = { new TreeError("Analysis result not available") };

		private Object[] failure(String str) {
			TreeError node = new TreeError(str);
			return new Object[] {node};
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		    /* if (newInput instanceof ViewInput) {
		        ViewInput in = (ViewInput) newInput;
		        TreeLeaf it = invisibleRoot.getChildren()[0];
		        System.out.println(it.getName());
			    it.setName(in.file + ": " + in.sLine);
			    v.refresh();
		    }
		    */
		}

		public void dispose() {
		}

		public Object[] getElements(Object input) {
			if (input instanceof ViewInput) {
				ViewInput in = (ViewInput) input;
				setContentDescription("Analysis Result - " + input.toString());
				try {
					// try to get analysis map, that we generated earlier
					TreeAnalysisMap map = (TreeAnalysisMap) in.file.getProject().getSessionProperty(GoblinPlugin.RESULT_NAME);
					
					// analysis not yet done or map generation failed
					if (map == null)
						return failNode;
					
					// get needed info
					TreeLoc tl = map.getAnalysis(in.file.getName(), in.sLine);
					
					if (tl == null) 
						return failNode;
					else{
						return tl.getChildren();
					}
					
				} catch (CoreException e) {
					return failure(e.getMessage());
				}
			} else {
			    if (input instanceof TreeNode)
			        return ((TreeNode)input).getChildren();
				return failNode;
			}

		}

		public Object getParent(Object child) {
			if (child instanceof TreeLeaf) {
				return ((TreeLeaf) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeNode) {
				return ((TreeNode) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeNode)
				return ((TreeNode) parent).hasChildren();
			return false;
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
		    return null;
		    /* 
			if (obj instanceof TreeLeaf) {
				TreeLeaf leaf = (TreeLeaf) obj;
				return leaf.getImage();
			} else
				return null;
			*/
		}
	}

	class NameSorter extends ViewerSorter {
	}
	
	/**
	 * Saves tree strukture on treeCollapsed/Expanded signal;
	 * @author kalmera
	 */
	final class ResultViewListener implements ITreeViewerListener {
		final class SaveRec{
			// current item
			final private Object o;
			
			SaveRec(Object o){
				this.o = o;
			}
			
			final private void saveState(TreeState s,TreeItem[] i){
				for(int j = 0; j < i.length; ++j){
					Object data = i[j].getData();
					if (i[j].getExpanded()^o==data) {
							saveState(s.addChild(((TreeLeaf)data).getId()),i[j].getItems());
						}
					} 
				}
			}
		
		final private void treeChanged(TreeExpansionEvent event){
			state = new TreeState("root");
			// save tree strukture  NB! event-object not changed jet
			new SaveRec(event.getElement()).saveState(state,viewer.getTree().getItems());
		}
	    final public void treeCollapsed(TreeExpansionEvent event){
	    	treeChanged(event);
	    }

	    final public void treeExpanded(TreeExpansionEvent event){
	    	treeChanged(event);
	    }
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.addTreeListener(new ResultViewListener());
				
		try {
			IEditorPart editor = getViewSite().getWorkbenchWindow().getActivePage().getActiveEditor();
			ISelection sel = editor.getEditorSite().getSelectionProvider().getSelection();
			selectionChanged(editor, sel);
		} catch (NullPointerException e) {}
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		viewer.setAutoExpandLevel(3);

		getSite().getWorkbenchWindow().getPartService().addPartListener(fPartListener);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
		super.dispose();
	}


	////////////////////////////////////////////////////////////
	// Creating the GUI stuff
	
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu("what", menuMgr, viewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		//fillLocalToolBar(bars.getToolBarManager());
	} 

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
	}

	/* private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	} */

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				Job job = new XMLJob(currentFile.getProject());
				job.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								//viewer.refresh();
								//viewer.expandToLevel(2);
							}
						});
					}
				});
				job.schedule();
			}
		};
		action1.setText("From file");
		action1.setToolTipText("Reads the analysis from file.");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		/* action2 = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				TreeLeaf test = (TreeLeaf) obj;
				if (test.getInfo() != null)
				    showMessage("Context: " + test.getInfo());
                } else
                   showMessage("No info available");;
        } */
		

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				TreeLeaf test = (TreeLeaf) obj;
				if (test.getInfo() != null)
				    showMessage("Information for: " + test.getName(), test.getInfo());
				//viewer.setExpandedState(obj, ! viewer.getExpandedState(obj));
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String title, String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				title, message);
	}

	public void setFocus() {
		getControl().setFocus();
	}
	
	public Control getControl() {
		return viewer.getControl();
	}
	
	
	//////////////////////////////////////////////////////////
	// HANDLE SELECTION CHANGES!

	private IPartListener2 fPartListener= new IPartListener2() {
		public void partVisible(IWorkbenchPartReference ref) {
			if (ref.getId().equals(getSite().getId())) {
				IWorkbenchPart activePart= ref.getPage().getActivePart();
				if (activePart != null)
					selectionChanged(activePart, ref.getPage().getSelection());
				startListeningForSelectionChanges();
		}
		}
		public void partHidden(IWorkbenchPartReference ref) {
			if (ref.getId().equals(getSite().getId()))
				stopListeningForSelectionChanges();
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
			System.out.println("partInputChanged " + ref.getPartName());
			//if (!ref.getId().equals(getSite().getId()))
				//System.out.println("This happened!");
		}
		public void partActivated(IWorkbenchPartReference ref) {
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}
		public void partClosed(IWorkbenchPartReference ref) {
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
	};

	
	protected void startListeningForSelectionChanges() {
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	}

	protected void stopListeningForSelectionChanges() {
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IEditorPart) {
			Object obj = ((IEditorPart)part).getEditorInput().getAdapter(IFile.class);
			if (obj instanceof IFile && selection instanceof ITextSelection) {
				// get current scroll view position
				if (viewer.getTree().getVerticalBar().getThumb()!=
						viewer.getTree().getVerticalBar().getMaximum()) {
					currentView = viewer.getTree().getVerticalBar().getSelection();
				}
			
				// change view
				currentFile = (IFile) obj;
				ITextSelection textsel = (ITextSelection) selection;
				ViewInput input = new ViewInput(currentFile, textsel.getStartLine(), textsel.getEndLine());
				viewer.setInput(input);
				
				// refresh view (to previous meaningful state if possible)
				if (state != null) {
					viewer.expandAll();
					expandItems(viewer.getTree().getItems(), state);
				}

				viewer.refresh();
				viewer.getTree().getVerticalBar().setSelection(currentView+1);
				viewer.getTree().getVerticalBar().setSelection(currentView);
			}
		}
	}

	private void expandItems(TreeItem[] newt, TreeState st) {
		if (newt!=null && newt.length>0) {
			try {
				for (int i = 0; i < newt.length; ++i){
					if (newt[i].getData()==null) 
						continue;
					
					String nodeId = ((TreeLeaf)newt[i].getData()).getId();

					expandItems(newt[i].getItems(),st.in(nodeId));
					newt[i].setExpanded(st.isThereA(nodeId));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}

/**
 * Strukture for expanded tree elements
 * @author kalmera
 */
class TreeState {
	private ArrayList<TreeState> o = new ArrayList<TreeState>();
	private String n;
	
	TreeState(){
		n = "nothing here!";
	}
	
	TreeState(String n){
		this.n = n;
	}
	
	public TreeState in(String s){
		java.util.Iterator<TreeState> it = o.iterator();
		if (s!=null)
			while (it.hasNext()){
				TreeState obj = it.next();
				if (s.equals(obj.n)) {
					return (TreeState) obj;
				}
			}

		return new TreeState(){
			public TreeState in(String s){
				return this;
			}
			public boolean isThereA(String s){
				return false;
			}
		};
	}
	
	public boolean isThereA(String s){
		
		if (s==null)
			return false;
		
		java.util.Iterator<TreeState> it = o.iterator();
		while (it.hasNext()){
			TreeState obj = it.next();
			if (s.equals(obj.n)) {
				return true;
			}
		}
		
		return false;
	}
	
	public TreeState addChild(String name){
		TreeState ts = new TreeState(name);
		o.add(ts);
		return ts;
	}
}

