public abstract class NodeVisitor {
    abstract void visitFolder(Folder node);
    abstract void visitFile(FileNode node);
    abstract void visitArchive(Archive node);
    abstract void visitAlias(Alias node);
}

