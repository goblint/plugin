/*
 * Created on Apr 6, 2005
 *
 */
package ee.ut.goblin.views;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author vesal
 *
 */
public class TreeError extends TreeLeaf {

    /**
     * @param name
     */
    public TreeError(String name) {
        super(name);
    }

	public Object[] getTopLevel() {
		return new Object[] { this };
	}

	public Image getImage() {
		String imageKey = ISharedImages.IMG_OBJS_ERROR_TSK;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
