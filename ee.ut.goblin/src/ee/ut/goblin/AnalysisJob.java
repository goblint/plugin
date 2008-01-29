/*
 * Created on Feb 9, 2005
 *
 */
package ee.ut.goblin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * @author vesal, kalmera
 *  
 */
public class AnalysisJob extends Job {
	private IProject project;
	private ArrayList<String> lst;
	private ArrayList<IFile> lstI;
	private GoblinMonitor monitor;

    private AnalysisJob(String name) {
        super(name);
        lst = new ArrayList<String>();
        lstI = new ArrayList<IFile>();
        setPriority(Job.LONG);
    }

    public AnalysisJob(IFile file) {
        this("Analysing " + file.getName());
        project = file.getProject();
    }
    
   
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IStatus run(IProgressMonitor mon) {
    	Process proc = null;
    	
    	monitor = new GoblinMonitor(mon);
        monitor.setTaskName("Launching the goblin");
        
        try {
        	IStatus execStatus = getExecutbleStatus();
        	if (!execStatus.isOK())
        		return execStatus;
        	
        	String[] cmds = gatherGoblinParameters();
        	String[] envp = null;
        	File     dir  = project.getLocation().toFile();

            proc = Runtime.getRuntime().exec(cmds, envp, dir);
            
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }

            monitor.setTaskName("Analysing");
            analyseIO(proc);
            
        } catch (IOException e) {
            return new Status(IStatus.ERROR, "ee.ut.goblin", 98, "I/O problems", e);
        } catch (CoreException e) {
            return new Status(IStatus.ERROR,"ee.ut.goblin", 98, "Core exception", e);
        } catch (InterruptedException e) {
            proc.destroy();
            monitor.done();
        	return Status.CANCEL_STATUS;
		}

        monitor.done();

        Job parse = new XMLJob(project);
        parse.schedule();
        
        return Status.OK_STATUS;
    }
    
    private void analyseIO(Process proc) throws IOException, InterruptedException {
        BufferedReader goblinStream = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader goblerrStream = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        MessageConsoleStream out = consoleOut();
    	
        String line;
        while ((line = goblinStream.readLine()) != null) {
            
        	if (monitor.isCanceled()) {
                throw new InterruptedException();
            }
            
        	if (line.startsWith("WARNING")) {
        		String[] parts = line.split(" /-/ ");
        		
        		IFile file = getFile(parts[1]);
        		int lnr = Integer.parseInt(parts[2]);
        		String warn = parts[3];
        		
        		
        		if (file != null) 
        			addMarker(file,lnr,warn);
        		
        	} else if (line.startsWith("PROGRESS")){
        		String[] parts = line.split(" /-/ ");
        		
        		if ("SUBTASK".equals(parts[1])){
        			int subtaskWork = Integer.parseInt(parts[3]);
        			monitor.beginSubtask(parts[2], subtaskWork);
        		} else if ("WORKED".equals(parts[1])) {
        			int worked = Integer.parseInt(parts[2]);
        			monitor.worked(worked);
        		} else if ("MORE WORK".equals(parts[1])){
        			int work = Integer.parseInt(parts[2]);
        			monitor.add_work(work);
        		}
        		
        	} else
                out.println(line);
        }
        while ((line = goblerrStream.readLine()) != null)
            out.println(line);
    }
    
    private static MessageConsoleStream consoleOut(){
        MessageConsole console = new MessageConsole("Analysis results", null);

        IConsoleManager cm = ConsolePlugin.getDefault().getConsoleManager();
        cm.addConsoles(new MessageConsole[] { console });
        
        return console.newMessageStream();
    }
    
    private IStatus getExecutbleStatus(){
    	IPreferenceStore store = GoblinPlugin.getDefault().getPreferenceStore();
    	String goblinExec = store.getString(GoblinPreferencePage.P_GOBLIN_EXEC).trim();
    	
        if (goblinExec.equals("")) 
            return new Status(IStatus.ERROR, "ee.ut.goblin", 99, "Goblin executable not set.", null);
        
        File exectest = new File(goblinExec);
        if (! exectest.exists())
            return new Status(IStatus.ERROR, "ee.ut.goblin", 99, "Goblin executable not found.", null);
        
        return Status.OK_STATUS;
    }
    
    
    private String[] gatherGoblinParameters() throws CoreException{
    	IPreferenceStore store = GoblinPlugin.getDefault().getPreferenceStore();
    	
        String goblinExec = store.getString(GoblinPreferencePage.P_GOBLIN_EXEC).trim();
        String extraParametersPref = store.getString(GoblinPreferencePage.P_EXTRA_PARAMETERS);
        String extraParametersProp = GoblinPropertyPage.getAdditionalParameters(project);
        String analysis = GoblinPropertyPage.getAnalysis(project);
        boolean allfuns = GoblinPropertyPage.getAllFuns(project);
        
        // gather parameters
        lst.add(goblinExec);
        lst.add("-o"); lst.add("goblin.xml"); 
        lst.add("--eclipse");
        lst.add("--analysis"); lst.add(analysis);
        
        if(allfuns) 
        	lst.add("--allfuns");
        
        String[] par = (extraParametersPref+" "+extraParametersProp).split(" ");
        for (int i = 0; i < par.length; ++i)
        	if	(par[i].toString().length()>0)
        		lst.add(par[i]);
        
        getAllFiles(project);
        
        String[] cmdArray = new String[lst.size()];
        
        for(int i = 0; i < lst.size(); i++){ 
        	cmdArray[i] = lst.get(i);
        }    	
        
        return cmdArray;
    }

	private void getAllFiles(IContainer project) throws CoreException {
		IResource[] res = project.members();
		
		for (int i = 0; i < res.length; i++) {
			IResource r = res[i];
			
			if (r instanceof IFile) {
				IFile f = (IFile) r;
				deleteMarkers(f);
				
			    String ext = r.getFileExtension();
			    
			    if (ext != null && (ext.equals("c") || ext.equals("h"))) {
			    	lst.add(r.getName());
			    	lstI.add(f);
			    }
			}
			
			if (r instanceof IFolder) 
				getAllFiles((IFolder) r);
		}
	}
	
	private IFile getFile(String name) {
		Iterator<IFile> iter = lstI.iterator();
		
		while (iter.hasNext()) {
			IFile f = iter.next();
			
			if (f.getName().equals(name))
				return f;
		}
		
		return null;
	}
	
    private static final String MARKER_TYPE = "ee.ut.goblin.warning";
	private void addMarker(IFile file, int lineNumber, String message) {
		try {
			Map<String, Integer> map = new HashMap<String, Integer>(4);

			if (lineNumber == -1) {
				lineNumber = 1;
			}
			
			MarkerUtilities.setLineNumber(map, lineNumber);
			MarkerUtilities.setMessage(map, message);
			map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_WARNING));
			MarkerUtilities.createMarker(file, map, MARKER_TYPE);
		} catch (CoreException e) {}
	}

	public static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {}
	}
}


/**
 * IProgressMonitor wrapper. Shows subtask's progress rather than main progress.
 * 
 * @author kalmera
 * @see org.eclipse.core.runtime.IProgressMonitor
 */
class GoblinMonitor{
	private IProgressMonitor m;
	private int saved;
	private int done;
	private int subtaskWork;

	private static long WORK_GRANULARITY = 1002041;
	
	private void initMonitor(){
		saved = 0;
		done = 0;
		subtaskWork = 1;

		m.beginTask("initializing ...",(int)WORK_GRANULARITY);
	}
	
	private void updateProgressBar(){
		int s; 
		
		if (subtaskWork == 0)
			s = 0;
		else
			s = (int)( (done * WORK_GRANULARITY) / subtaskWork );
		
		m.worked(s-saved);
		saved = s;
	}
	
	public GoblinMonitor(IProgressMonitor m){
		this.m = m;
		initMonitor();
	}
	
	public void done(){
		m.done();
	}
	
	public void beginSubtask(String s, int work){
		m.subTask(s);
		done = 0;
		subtaskWork = work;
		updateProgressBar();
	}
	
	public void add_work(long i){
		subtaskWork += i;
		updateProgressBar();
	}
	
	public void worked(long i){
		if (i != 0) {
			done += i;
			updateProgressBar();
		}
	}
	
	public void setTaskName(String s){
		m.setTaskName(s);
	}
	
	public boolean isCanceled(){
		return m.isCanceled();
	}
}
