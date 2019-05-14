import java.util.ArrayList;

class Archiver {
    private Archive.ArchiveTypes type;

    public Archiver(Archive.ArchiveTypes type) {
        this.type = type;
    }

    public Archive buildArchive(Folder folder, String archiveName, int compressionLevel){

        //Create Archive object
        Archive archive = new Archive(archiveName + type.getExtension(), type, compressionLevel);

        //Compress the data of the folder and put it in the archive
        Compressor compressor = archive.getCompressor();
        archive.setContent(buildArchiveRec(folder, compressor));

        return archive;
    }

    /**
     * Recursive call to compress an folder
     *
     * @param folder the folder to compress
     * @param compressor the compressor
     * @return the compressed data
     */
    private Object buildArchiveRec(Folder folder, Compressor compressor){
        ArrayList<Object> compressed = new ArrayList<>();

        for(Node n : folder.getChildren()){
            if(n instanceof Folder)
                compressed.add(buildArchiveRec((Folder)n, compressor));
            else if (n instanceof FileNode)
                compressed.add(compressor.compressFile(n.getName(), ((FileNode) n).getContent()));
            else if (n instanceof Alias)
                compressed.add(compressor.compressFile(n.getName(), ((Alias) n).getFileNode().getContent()));
            else if (n instanceof Archive)
                compressed.add(((Archive) n).getContent());
        }

        return compressor.mergeCompressed(folder.getName(), compressed);
    }

}

abstract class Compressor{
    protected int compressionLevel;

    public Compressor(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    /**
     * Get a tree of ArchiveHeader representing the internal file structure of the archive
     *
     * @param content the compressed data of the archive
     * @return a tree of ArchiveHeader
     */
    public abstract ArchiveHeader getStructure(Object content);

    /**
     * Copy a compressed file
     *
     * @param content a compressed file
     * @return a copy
     */
    public abstract Object copyContent(Object content);

    /**
     * Compress a file
     *
     * @param fileName the name of the file
     * @param content the content of the file
     * @return the compressed file
     */
    public abstract Object compressFile(String fileName, String content);

    /**
     * Merge several compressed files
     *
     * @param folderName the folder that will contain the compressed file inside the archive
     * @param compressed a list of comrpessed files
     * @return a compressed file
     */
    public abstract Object mergeCompressed(String folderName, ArrayList<Object> compressed);
}

/**
 * The implementation here uses Folder and FileNode because it's simpler, but a real implementation would use classes
 * independent of the rest of the program.
 *
 * The implementation itself isn't very important for this project, we just put everything into files and folders that
 * we casted to Object.
 */
class ZipCompressor extends Compressor{

    public ZipCompressor(int compressionLevel) {
        super(compressionLevel);
    }

    @Override
    public ArchiveHeader getStructure(Object content) {
        Folder compressed = (Folder)content;

        return buildHeader(compressed);
    }

    protected ArchiveHeader buildHeader(Node node){
        ArchiveHeader header = new ArchiveHeader(node.getName());

        if(node instanceof Folder)
            for(Node child : ((Folder) node).getChildren())
                header.addChild(buildHeader(child));

        return header;
    }

    @Override
    public Object copyContent(Object content) {
        Folder folder = (Folder) content;
        try {
            return folder.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object compressFile(String fileName, String content) {
        Folder f = new Folder("tmp");
        f.addChild(new FileNode(fileName, content));
        return f;
    }

    @Override
    public Object mergeCompressed(String folderName, ArrayList<Object> compressed) {
        Folder f = new Folder(folderName);

        for(Object o : compressed) {
            Folder f2 = (Folder) o;
            if(!f2.getName().equals("tmp"))
                f.addChild(f2);
            else
                for (Node n : ((Folder) o).getChildren())
                    f.addChild(n);
        }
        return f;
    }
}

/**
 * Extends ZipCompressor to not copy/paste the code.
 *
 * A real implementation would extends Compressor and use the Rar algorithm for compression.
 */
class RarCompressor extends ZipCompressor{

    public RarCompressor(int compressionLevel) {
        super(compressionLevel);
    }
}

/**
 * Extends ZipCompressor to not copy/paste the code.
 *
 * A real implementation would extends Compressor and use the Tar algorithm for compression.
 */
class TarCompressor extends ZipCompressor{

    public TarCompressor(int compressionLevel) {
        super(compressionLevel);
    }
}

/**
 * Object used to represent the structure of the archive as a tree independently from the trees of the file explorer
 */
class ArchiveHeader{
    private ArrayList<ArchiveHeader> children = new ArrayList<>();
    private String name;

    public ArchiveHeader(String name) {
        this.name = name;
    }

    public void addChild(ArchiveHeader header){
        children.add(header);
    }

    public final ArrayList<ArchiveHeader> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }
}