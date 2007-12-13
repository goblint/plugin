/*
 * Created on Mar 24, 2005
 *
 */
package ee.ut.goblin.views;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.Attributes;


/**
 * @author vesal
 *
 */
public class TreeNode extends TreeLeaf {
	
    private ArrayList children;

	public TreeNode(Attributes attr) {
		super(attr);
		children = new ArrayList();
	}
    
    public TreeNode (String name) {
    	super(name);
    	children = new ArrayList();
    }

    public void addChild(TreeLeaf child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(TreeLeaf child) {
        children.remove(child);
        child.setParent(null);
    }

    public TreeLeaf[] getChildren() {
        return (TreeLeaf[]) children.toArray(new TreeLeaf[children.size()]);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

	public TreeNode[] getTopLevel() {
		return new TreeNode[] { this };
	}
	
	public Image getImage() {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
