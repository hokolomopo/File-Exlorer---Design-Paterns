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
    private Logger logger = Logger.getInstance();

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
            logger.log(e.toString());
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
            logger.log(e.toString());
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
            logger.log(e.toString());
        }
    }

    @Override
    void visitAlias(Alias node) {
        //Cannot copy aliases - do nothing
    }

    //Get the name of the copy
    private String getCopyName(Node node){
        //Simply parse the name to see if the file is a copy (if it contains the string "(copy_"
        //If the file is a copy, increment the copy number ( "copy_1" becomes "copy_2)

        String name = node.getName();
        String strCopy = "(copy_";

        int index = name.lastIndexOf(strCopy);

        String copyName = name + strCopy + "1)";

        //This is a copy
        if(index != -1){
            try {
                //Get the copy number
                String tmp = name.substring(index + strCopy.length());
                int closingParIndex = tmp.indexOf(")");
                if(closingParIndex != - 1) {
                    int copyNum = Integer.parseInt(tmp.substring(0, closingParIndex));

                    //Creating new name with incremented copy number
                    copyName = name.replace(strCopy + copyNum, strCopy + ++copyNum);
                }

            //NumberFormatException if the char following the String "(copy_" is not a number
            //It will happen if the user put "(copy_" in the name of its file
            }catch (NumberFormatException ignored){

            }
        }


        return copyName;
    }
}
