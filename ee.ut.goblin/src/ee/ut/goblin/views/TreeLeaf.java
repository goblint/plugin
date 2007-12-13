/*
 * Created on Mar 24, 2005
 *
 */
package ee.ut.goblin.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.Attributes;


/**
 * @author vesal
 *  
 */
public class TreeLeaf implements IAdaptable {
    private String name;
    private String info;
    private String id;
    private TreeNode parent;

    public TreeLeaf (String name) {
    	this.name = name;
    }
    
	public TreeLeaf(Attributes attr) {
		this(attr.getValue("text"));
		id = attr.getValue("id");
		if (id==null)
			id="";
		
		String it = attr.getValue("info");
		if (it != null)
		    this.info = it.replaceAll("; ", "\n");
	}

    public String getName() {
        return name;
    }
    
    public String getInfo() {
        return info;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getParent() {
        return parent;
    }
    
    public String getId(){
    	return id;
    }
    
    public void setId(String i){
    	id = i;
    }

    public String toString() {
        return getName();
    }
    
    public String toShortString() {
    	return toString();
    }
    
    
	public Object getAdapter(Class adapter) {
		return null;
	}

	public Image getImage() {
		String imageKey = ISharedImages.IMG_OBJS_INFO_TSK;	
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}