/*
 * Created on Apr 6, 2005
 */
package ee.ut.goblin;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.xml.sax.helpers.DefaultHandler;

import ee.ut.goblin.views.TreeNode;
import ee.ut.goblin.views.TreeAnalysis;
import ee.ut.goblin.views.XMLHandler;

/**
 * @author Vesal Vojdani
 */
public class XMLJob extends Job {
	private IProject proj;
	
	public XMLJob(IProject proj) {
		super("Reading XML file: " + proj.getName());
		this.proj = proj;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		TreeNode root = new TreeNode("");

		File it = new File(proj.getLocation().addTrailingSeparator() + "goblin.xml");
		if (! it.canRead()) 
			return new Status(IStatus.ERROR, "ee.ut.goblin", 97, "Can't read analysis file.", null);
		
		DefaultHandler handler = new XMLHandler(root);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try {
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(it, handler);
		} catch (Throwable t) {
			return new Status(IStatus.ERROR, "ee.ut.goblin", 96, t.getMessage(), t);
		}
		
		TreeAnalysisMap tam  = new TreeAnalysisMap((TreeAnalysis) root.getChildren()[0]);
	
		try {
			proj.setSessionProperty(GoblinPlugin.RESULT_NAME, tam);
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, "ee.ut.goblin", 95, e.getMessage(), e);
		}

		monitor.done();
		return Status.OK_STATUS;
	}

}
