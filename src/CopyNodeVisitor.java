import montefiore.ulg.ac.be.graphics.LevelException;
import montefiore.ulg.ac.be.graphics.NoParentNodeException;
import montefiore.ulg.ac.be.graphics.NoPreviousInsertedNodeException;
import montefiore.ulg.ac.be.graphics.NoSelectedNodeException;

/**
 * Visitor used to copy files
 */
public class CopyNodeVisitor extends NodeVisitor {

    private Node rootOfCopy;
    private GuiHandler gui = GuiHandler.getInstance();


    public CopyNodeVisitor(Node rootOfCopy) {
        this.rootOfCopy = rootOfCopy;
    }

    @Override
    void visitFolder(Folder node) {
        try {

            //This is the root node of the copy - simply copy the file to its parent
            if(this.rootOfCopy == node) {
                Folder copy = new Folder(getCopyName(node));
                gui.addNodeToParentNode(copy);
            }

            //This is not the root node of the copy - this is a recursive call
            else{
                Folder copy = new Folder(node.getName());
                int currentLevel = node.getLevel() - rootOfCopy.getLevel();
                gui.addNodeToLastInsertedNode(copy, currentLevel);
            }

            for(Node n : node.getChildren()){
                n.accept(this);
            }

        } catch (NoSelectedNodeException | NoParentNodeException | NoPreviousInsertedNodeException | LevelException e) {
            e.printStackTrace();
        }
    }

    @Override
    void visitFile(FileNode node) {
        try {

            //This is the root node of the copy - simply copy the file to its parent
            if(this.rootOfCopy == node) {
                FileNode copy = new FileNode(getCopyName(node), node.getContent());
                gui.addNodeToParentNode(copy);
            }

            //This is not the root node of the copy - this is a recursive call
            else{
                FileNode copy = new FileNode(node.getName(), node.getContent());
                int currentLevel = node.getLevel() - rootOfCopy.getLevel();
                gui.addNodeToLastInsertedNode(copy, currentLevel);
            }

        } catch (NoSelectedNodeException | NoParentNodeException | NoPreviousInsertedNodeException | LevelException e) {
            e.printStackTrace();
        }
    }

    @Override
    void visitArchive(Archive node) {
        try {

            //This is the root node of the copy - simply copy the file to its parent
            if(this.rootOfCopy == node) {
                Archive copy = new Archive(getCopyName(node), node.getType(), node.getCompressionLevel());
                copy.setContent(copy.getCompressor().copyContent(node.getContent()));
                gui.addNodeToParentNode(copy);
            }

            //This is not the root node of the copy - this is a recursive call
            else{
                Archive copy = new Archive(node.getName(), node.getType(), node.getCompressionLevel());
                copy.setContent(copy.getCompressor().copyContent(node.getContent()));
                int currentLevel = node.getLevel() - rootOfCopy.getLevel();
                gui.addNodeToLastInsertedNode(copy, currentLevel);
            }

        } catch (NoSelectedNodeException | NoParentNodeException | NoPreviousInsertedNodeException | LevelException e) {
            e.printStackTrace();
        }
    }

    @Override
    void visitAlias(Alias node) {
        //Cannot copy aliases - do nothing
    }

    private String getCopyName(Node node){
        return node.getName() + "(copy)";
    }
}
