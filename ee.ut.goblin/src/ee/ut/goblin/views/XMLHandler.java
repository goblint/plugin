/*
 * Created on Mar 15, 2005
 *
 */
package ee.ut.goblin.views;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses XML output from the Goblin.
 * 
 * @author vesal
 */
public class XMLHandler extends DefaultHandler {
    private Stack<TreeLeaf> stack;

    public XMLHandler(TreeNode root) {
        super();
        stack = new Stack<TreeLeaf>();
        stack.push(root);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        TreeLeaf t;

        if (qName.equals("Analysis")) {
            t = new TreeAnalysis ();
        } else if (qName.equals("Leaf")) {
            t = new TreeLeaf(attributes);
        } else if (qName.equals("Loc")) {
            t = new TreeLoc(attributes);
        } else {
            t = new TreeNode(attributes);
        }
        ((TreeNode) stack.peek()).addChild(t);
        stack.push(t);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        stack.pop();
    }

}