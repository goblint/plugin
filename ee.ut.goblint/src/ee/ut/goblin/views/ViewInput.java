/*
 * Created on Apr 6, 2005
 */
package ee.ut.goblin.views;

import org.eclipse.core.resources.IFile;

/**
 * @author Vesal Vojdani
 */
public class ViewInput {
	
	public int sLine, eLine;
	public IFile file;
	
	public ViewInput(IFile file, int sLine, int eLine) {
		this.file = file;
		this.sLine = sLine+1;
		this.eLine = eLine+1;
	}
	
	public String toString() {
		if (file != null)
			return file.getName() + ": " + eLine;
		else
			return "Unknown file";
	}
	
	public TreeLoc toLoc() {
		return new TreeLoc(file.getName(), eLine);
	}

}
