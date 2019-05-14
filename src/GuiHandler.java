import montefiore.ulg.ac.be.graphics.*;

import java.util.HashMap;

public class GuiHandler implements ExplorerEventsHandler {

	private ExplorerSwingView esv;

	//Last known selected node
	private Node selectedNode;

	//Used to remember last inserted nodes for recursive calls
	private HashMap<Integer, Node> lastInsertedNodes = new HashMap<>();

	private static Logger log;

	private static GuiHandler INSTANCE;
	static GuiHandler getInstance()  {
		if(INSTANCE == null)
			INSTANCE = new GuiHandler();
		log = Logger.getInstance();

		return INSTANCE;
	}
	
    private GuiHandler() {
        try {
			this.esv = new ExplorerSwingView(this);

			// First step to do before anything !!!
            this.esv.setRootNode(new Folder("root")); // set the root node with a silly "A" object
        } catch (RootAlreadySetException | NullHandlerException e) {
        	//We cannot have a NullHandlerException in practice, because "this" can never be null
            e.printStackTrace();
        }
	}
	
	@Override
	public void createAliasEvent(Object selectedNode) {
		log.log("createAliasEvent");

		this.selectedNode = (Node)selectedNode;

		//Selected is NOT a file - show error message
		if (!(selectedNode instanceof FileNode)){
			esv.showPopupError("An alias can only be created on a file");
			return;
		}

		Alias alias = new Alias(this.selectedNode.getName() + "(alias)", (FileNode) selectedNode);
		try {
			this.addNodeToParentNode(alias);
			esv.refreshTree();
		} catch (NoSelectedNodeException | NoParentNodeException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void createArchiveEvent(Object selectedNode) {
		log.log("createArchiveEvent");

		this.selectedNode = (Node)selectedNode;

		if(!(selectedNode instanceof Folder)){
			esv.showPopupError("Can only create archive of folder");
			return;
		}
		else if(esv.isRootNodeSelected()){
			esv.showPopupError("Cannot archive root folder");
			return;
		}

		try {

			//Window dialog
			String archiveName = esv.displayArchiveWindow1();
			if(archiveName == null)
				return;
			if(archiveName.length() == 0){
				esv.showPopupError("You must input a name");
			}

			String archiveType = esv.displayArchiveWindow2();
			if(archiveType == null)
				return;

			int compressionLevel = esv.displayArchiveWindow3();
			if(compressionLevel == -1)
				return;

			//Compress the folder
			Folder folder = (Folder) selectedNode;
			Archiver archiver = new Archiver(Archive.ArchiveTypes.getTypeFromExtension(archiveType));
			Archive archive = archiver.buildArchive(folder, archiveName, compressionLevel);

			this.addNodeToParentNode(archive);

			esv.refreshTree();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	@Override
	public void createCopyEvent(Object selectedNode) {
		log.log("createCopyEvent");

		this.selectedNode = (Node)selectedNode;


		if(esv.isRootNodeSelected()){
			esv.showPopupError("Cannot copy the root directory");
			return;
		}
		else if(selectedNode instanceof Alias){
			esv.showPopupError("Cannot copy a alias");
			return;
		}

		//Create visitor and visit the node
		CopyNodeVisitor visitor = new CopyNodeVisitor(this.selectedNode);
		this.selectedNode.accept(visitor);

		esv.refreshTree();
	}

	@Override
	public void createFileEvent(Object selectedNode) {
		log.log("createFileEvent");

		this.selectedNode = (Node)selectedNode;

		//Dialog window
		String[] ss = esv.fileMenuDialog();
		if(ss == null)
			return;

		//Check if a name was inputted
		if(ss[0].length() == 0){
			esv.showPopupError("You must input a name");
			return;
		}

		//Add file to the tree
		FileNode newFile = new FileNode(ss[0], ss[1]);
		try {
			this.addNodeToSelectedNode(newFile);
		} catch (NoSelectedNodeException e) {
			e.printStackTrace();
		}

		esv.refreshTree();
	}

	@Override
	public void createFolderEvent(Object selectedNode) {
		log.log("createFolderEvent");
		this.selectedNode = (Node)selectedNode;

		if (!(this.selectedNode instanceof Folder)){
			esv.showPopupError("Can only add a folder to another folder");
			return;
		}

		//Dialog window
		String name = esv.folderMenuDialog();
		if(name == null)
			return;

		//Check if a name was inputted
		if(name.length() == 0){
			esv.showPopupError("You must input a name");
			return;
		}

		//Create new folder and add it to the tree
		Folder newFolder = new Folder(name);
		try {
			this.addNodeToSelectedNode(newFolder);
		} catch (NoSelectedNodeException e) {
			e.printStackTrace();
		}

		esv.refreshTree();
	}

	@Override
	public void doubleClickEvent(Object selectedNode) {
		log.log("doubleClickEvent");

		this.selectedNode = (Node)selectedNode;

		//Clear text of AreaManager
		esv.getTextAreaManager().clearAllText();

		//Create visitor and visit the node
		DisplayNodeVisitor visitor = new DisplayNodeVisitor(this.selectedNode, esv.getTextAreaManager());
		this.selectedNode.accept(visitor);
	}

	@Override
	public void eventExit() {
		log.log("eventExit");
	}

	//Same as ExplorerSwingView.addNodeToParentNode, but also add node in internal representation of the tree
	public void addNodeToParentNode(Node node) throws NoParentNodeException, NoSelectedNodeException {

		//Update the swing view first because it will send the needed exceptions
		esv.addNodeToParentNode(node);

		//Update internal representation of tree
		Folder parent = (Folder)this.selectedNode.getParent();
		parent.addChild(node);
		node.setParent(parent);

		//Update lastInserted HashMap
		this.lastInsertedNodes.clear();
		this.lastInsertedNodes.put(0, node);
	}

	//Same as ExplorerSwingView.addNodeToSelectedNode, but also add node in internal representation of the tree
	public void addNodeToSelectedNode(Node node) throws NoSelectedNodeException {
		if (!(this.selectedNode instanceof Folder)){
			esv.showPopupError("Can only add a folder to another folder");
			return;
		}

		//Update the swing view first because it will send the needed exceptions
		esv.addNodeToSelectedNode(node);

		//Update internal representation of tree
		Folder parent = (Folder)this.selectedNode;
		parent.addChild(node);
		node.setParent(parent);

		//Update lastInserted HashMap
		this.lastInsertedNodes.clear();
		this.lastInsertedNodes.put(0, node);
	}

	//Same as ExplorerSwingView.addNodeToLastInsertedNode, but also add node in internal representation of the tree
	public void addNodeToLastInsertedNode(Node node, int level) throws NoPreviousInsertedNodeException, LevelException {
		Node currentNode = this.lastInsertedNodes.get(level - 1);

		if(currentNode == null)
			throw new NullPointerException("NoPreviousInsertedNodeException");

		if (!(currentNode instanceof Folder)){
			throw new NullPointerException("Last inserted is not a folder");
		}

		//Update the swing view first because it will send the needed exceptions
		esv.addNodeToLastInsertedNode(node, level);

		//Update internal representation of tree
		Folder parent = (Folder)currentNode;
		parent.addChild(node);
		node.setParent(parent);

		//Update lastInserted HashMap
		this.lastInsertedNodes.put(level, node);
	}
}
