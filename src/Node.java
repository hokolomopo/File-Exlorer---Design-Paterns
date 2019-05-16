import java.util.ArrayList;
import java.util.HashMap;

public abstract class Node implements Cloneable{
    protected int level = 0;
    protected String name;
    protected Node parent;

    public Node(String name) {
        this.name = name;
    }

    public abstract void accept(NodeVisitor v);

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
        this.level = parent.getLevel() + 1;
    }

    @Override
    public String toString() {
        return name ;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

class Alias extends Node{
    private FileNode fileNode;

    public Alias(String name, FileNode fileNode) {
        super(name);
        this.fileNode = fileNode;
    }

    @Override
    public void accept(NodeVisitor v) {
        v.visitAlias(this);
    }

    public FileNode getFileNode() {
        return fileNode;
    }
}


//Class named FileNode and not File to avoid confusion with java.io.File
class FileNode extends Node{
    private String content;

    public FileNode(String name, String content) {
        super(name);
        this.content = content;
    }

    @Override
    public void accept(NodeVisitor v) {
        v.visitFile(this);
    }

    public String getContent() {
        return content;
    }
}

class Folder extends Node{
    private ArrayList<Node> children = new ArrayList<>();

    public Folder(String name) {
        super(name);
    }

    public void addChild(Node child){
        children.add(child);
    }

    private String getCopyName(Node node, int copyNumber){
        return String.format("%s(copy_%d)", node.name, copyNumber);
    }

    @Override
    public void accept(NodeVisitor v) {
        v.visitFolder(this);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Folder clone = (Folder) super.clone();
        clone.children = new ArrayList<>();
        for(Node n : children)
            clone.children.add((Node)n.clone());
        return clone;
    }
}

class Archive extends Node{
    public enum ArchiveTypes{
        ZIP(".zip"),
        TAR(".tar"),
        RAR(".rar");

        private String extension;

        ArchiveTypes(String extension) {
            this.extension = extension;
        }

        //Return the Compressor corresponding to this type of archive
        private Compressor getCompressor(int compressionLevel){
            switch (this){
                case ZIP:
                    return new ZipCompressor(compressionLevel);
                case TAR:
                    return new TarCompressor(compressionLevel);
                case RAR:
                    return new RarCompressor(compressionLevel);
            }

            return null;
        }

        public String getExtension() {
            return extension;
        }

        //Return the ArchiveTypes given the extension of the archive
        public static ArchiveTypes getTypeFromExtension(String extension){
            for(ArchiveTypes archiveType : ArchiveTypes.values())
                if(archiveType.extension.equals(extension))
                    return archiveType;
            return null;
        }

    }
    private Object content;
    private ArchiveTypes type;
    private int compressionLevel;

    public Archive(String name, ArchiveTypes type, int compressionLevel) {
        super(name);
        this.type = type;
        this.compressionLevel = compressionLevel;
    }

    @Override
    public void accept(NodeVisitor v) {
        v.visitArchive(this);
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Object getContent() {
        return content;
    }

    public ArchiveTypes getType() {
        return type;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public Compressor getCompressor(){
        return type.getCompressor(this.compressionLevel);
    }
}
