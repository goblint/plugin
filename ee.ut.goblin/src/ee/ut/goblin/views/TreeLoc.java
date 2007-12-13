/*
 * Created on Apr 1, 2005
 */
package ee.ut.goblin.views;

import org.xml.sax.Attributes;

/**
 * @author Vesal Vojdani
 */
public class TreeLoc extends TreeNode {
	private String file;
	private int    line;
	private String func;

	public TreeLoc(Attributes attr) {
		super("Location");
		file = attr.getValue("file");
		line = Integer.parseInt(attr.getValue("line"));
		func = attr.getValue("fun");
	}
	
	public TreeLoc(String file, int line) {
		super("Location");
		this.file = file;
		this.line = line;
	}
	
	public String toString() {
		return file + "(" + func + "): " + line;
	}
	
    
	public boolean equals(Object o) {
		if (o instanceof TreeLoc) {
			TreeLoc loc = (TreeLoc) o;
			return file.equals(loc.file) && line == loc.line;
		} else
			return false;
	}
	
	public String getFilename(){
		return file;
	}
	
	public String getFunction(){
		return func;
	}
	
	public int getLine(){
		return line;
	}
	
	public int hashCode() {
		return file.hashCode() + line ;
	}
}
