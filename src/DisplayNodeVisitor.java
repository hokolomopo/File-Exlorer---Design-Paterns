import montefiore.ulg.ac.be.graphics.TextAreaManager;

/**
 * Visitor used to display files in the TextArea
 */
public class DisplayNodeVisitor extends NodeVisitor{
    private static final String RETURN = "\n";

    private Node displayRootNode;
    private TextAreaManager textAreaManager;

    public DisplayNodeVisitor(Node displayRootNode, TextAreaManager textAreaManager) {
        this.displayRootNode = displayRootNode;
        this.textAreaManager = textAreaManager;
    }

    @Override
    public void visitFolder(Folder node) {
        //This is NOT the root node of the display - display its name in addition to its children
        if(this.displayRootNode != node) {
            String toDisplay = getPrefix(node.getLevel(), this.displayRootNode.getLevel());
            toDisplay += node.getName() + RETURN;

            this.display(toDisplay);
        }

        for(Node n : node.getChildren())
            n.accept(this);

    }

    @Override
    public void visitFile(FileNode node) {
        //This is the root node of the display - we want to display its content
        if(this.displayRootNode == node)
            this.display(node.getContent());

        //This is NOT the root node of the display - we want to display its name
        else {
            String toDisplay = getPrefix(node.getLevel(), this.displayRootNode.getLevel());
            toDisplay +=  node.toString() + RETURN;

            this.display(toDisplay);
        }
    }


    @Override
    public void visitAlias(Alias node) {
        //This is the root node of the display - set the file pointed from the alias as root and visit the file
        if(this.displayRootNode == node) {
            this.displayRootNode = node.getFileNode();
            node.getFileNode().accept(this);
        }

        //This is NOT the root node of the display - we want to display its name
        else {
            String toDisplay = getPrefix(node.getLevel(), this.displayRootNode.getLevel());
            toDisplay +=  node.toString() + RETURN;

            this.display(toDisplay);
        }
    }

    @Override
    public void visitArchive(Archive node) {

        //This is the root node of the display - display content of the archive
        if(this.displayRootNode == node) {
            ArchiveHeader header = node.getCompressor().getStructure(node.getContent());

            this.displayArchiveHeader(header, 1);
        }

        //This is NOT the root node of the display - we want to display its name
        else {
            String toDisplay = getPrefix(node.getLevel(), this.displayRootNode.getLevel());
            toDisplay +=  node.toString() + RETURN;

            this.display(toDisplay);
        }

    }

    private void displayArchiveHeader(ArchiveHeader header, int level){
        String toDisplay = getPrefix(level, 0);
        toDisplay +=  header.getName() + RETURN;

        this.display(toDisplay);

        for(ArchiveHeader h : header.getChildren())
            displayArchiveHeader(h, level + 1);
    }

    /**
     * Append the string to the TextAreaManager
     * @param s the string to display
     */
    private void display(String s){
        this.textAreaManager.appendText(s);
    }

    /**
     * Return prefix(spacing) for pretty printing
     *
     * @param startingLevel Starting level of the print
     * @param currentLevel Current level of the print
     * @return A string with the needed tabulations
     */
    private String getPrefix(int currentLevel, int startingLevel){
        StringBuilder builder = new StringBuilder();

        //Number of tabs = difference in levels between startingNode (the "root" of the display action) and the
        //current node, minus 1 because we are not displaying the folder "root" of the display
        for(int i = 0;i < currentLevel - startingLevel - 1;i++)
            builder.append("    ");

        builder.append(" - ");
        return builder.toString();
    }

}
