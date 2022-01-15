package com.shade.decima.ui;

import com.shade.decima.Compressor;
import com.shade.decima.archive.Archive;
import com.shade.decima.archive.ArchiveManager;
import com.shade.decima.rtti.RTTIType;
import com.shade.decima.rtti.objects.RTTIObject;
import com.shade.decima.rtti.registry.RTTITypeRegistry;
import com.shade.decima.ui.handlers.ValueCollectionHandler;
import com.shade.decima.ui.handlers.ValueHandler;
import com.shade.decima.ui.handlers.ValueHandlerProvider;
import com.shade.decima.ui.navigator.NavigatorFileNode;
import com.shade.decima.ui.navigator.NavigatorFolderNode;
import com.shade.decima.ui.navigator.NavigatorNode;
import com.shade.decima.util.NotNull;
import com.shade.decima.util.Nullable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class ApplicationFrame extends JFrame {
    private final Path path;
    private final Compressor compressor;
    private final JTree properties;

    public ApplicationFrame() {
        this.path = Path.of("E:/SteamLibrary/steamapps/common/Death Stranding");
        this.compressor = new Compressor(path.resolve("oo2core_7_win64.dll"));
        this.properties = new JTree((TreeModel) null);
        this.properties.setCellRenderer(new StyledListCellRenderer());

        setTitle("Decima Explorer");

        try {
            initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initialize() throws Exception {
        initializeMenuBar();
        loadArchives();
    }

    private void loadArchives() throws IOException {
        Files.walkFileTree(path.resolve("data"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                ArchiveManager.getInstance().load(file);
                return FileVisitResult.CONTINUE;
            }
        });

        final RTTIObject prefetch = ArchiveManager.getInstance().readFileObjects(compressor, "prefetch/fullgame.prefetch").get(0);
        final Node root = new Node("root", null);

        for (RTTIObject file : (RTTIObject[]) prefetch.getMemberValue("Files")) {
            final String path = file.getMemberValue("Path");
            final Archive.FileEntry entry = ArchiveManager.getInstance().getFileEntry(path);

            if (entry != null) {
                populate(root, entry.archive().getName() + '/' + path, entry);
            }
        }

        final JTree navigator = new JTree(new DefaultTreeModel(root.toTreeNode(null)));
        navigator.setToggleClickCount(0);
        navigator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() != 2) {
                    return;
                }
                if (navigateFromPath(navigator.getSelectionPath())) {
                    event.consume();
                }
            }
        });
        navigator.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() != KeyEvent.VK_ENTER) {
                    return;
                }
                if (navigateFromPath(navigator.getSelectionPath())) {
                    event.consume();
                }
            }
        });

        final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setBorder(null);
        pane.add(new JScrollPane(navigator));
        pane.add(new JScrollPane(properties));

        final Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout("insets dialog", "[grow,fill]", "[grow,fill]"));
        contentPane.add(pane);
    }

    private boolean navigateFromPath(@Nullable TreePath path) {
        if (path != null) {
            final Object component = path.getLastPathComponent();

            if (component instanceof NavigatorFileNode file) {
                navigate(file);
                return true;
            }
        }

        return false;
    }

    private void initializeMenuBar() {
        final JMenuBar menuBar = new JMenuBar();

        initializeFileMenu(menuBar);
        initializeEditMenu(menuBar);
        initializeHelpMenu(menuBar);

        setJMenuBar(menuBar);
    }

    private void initializeFileMenu(@NotNull JMenuBar menuBar) {
        final JMenuItem menuItemOpen = new JMenuItem("Open\u2026", KeyEvent.VK_O);
        menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuItemOpen.addActionListener(e -> performOpenAction());

        final JMenuItem menuItemQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
        menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuItemQuit.addActionListener(e -> performQuitAction());

        final JMenu menuItemFile = new JMenu("File");
        menuItemFile.setMnemonic(KeyEvent.VK_F);
        menuItemFile.add(menuItemOpen);
        menuItemFile.addSeparator();
        menuItemFile.add(menuItemQuit);

        menuBar.add(menuItemFile);
    }

    private void initializeEditMenu(JMenuBar menuBar) {
        final JMenu menuItemEdit = new JMenu("Edit");
        menuItemEdit.setMnemonic(KeyEvent.VK_E);

        menuBar.add(menuItemEdit);
    }

    private void initializeHelpMenu(JMenuBar menuBar) {
        final JMenu menuItemHelp = new JMenu("Help");
        menuItemHelp.setMnemonic(KeyEvent.VK_H);

        menuBar.add(menuItemHelp);
    }

    private void performOpenAction() {
        System.out.println("To be done eventually");
    }

    private void performQuitAction() {
        dispose();
    }

    @Override
    public void dispose() {
        try {
            ArchiveManager.getInstance().close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing opened archives", e);
        }

        super.dispose();
    }

    private static void populate(@NotNull Node node, @NotNull String path, @Nullable Archive.FileEntry entry) {
        Node root = node;

        for (String part : path.split("/")) {
            Node child = root.children.get(part);

            if (child == null) {
                child = new Node(part, entry);
                root.children.put(part, child);
            }

            root = child;
        }
    }

    private static class Node {
        private final String name;
        private final Map<String, Node> children;
        private final Archive.FileEntry entry;

        public Node(@NotNull String name, @Nullable Archive.FileEntry entry) {
            this.name = name;
            this.entry = entry;
            this.children = new TreeMap<>();
        }

        @NotNull
        public NavigatorNode toTreeNode(@Nullable NavigatorNode parent) {
            if (!children.isEmpty()) {
                final List<NavigatorNode> nodes = new ArrayList<>();
                final NavigatorFolderNode node = new NavigatorFolderNode(parent, nodes, name);

                for (Node child : children.values()) {
                    nodes.add(child.toTreeNode(node));
                }

                return node;
            }

            return new NavigatorFileNode(parent, name, entry);
        }
    }

    public void navigate(@NotNull NavigatorFileNode node) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

        try {
            for (RTTIObject object : ArchiveManager.getInstance().readFileObjects(compressor, node.getFile())) {
                append(root, object.getType(), object);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        properties.setModel(new DefaultTreeModel(root));
        properties.expandPath(new TreePath(root.getPath()));
    }

    public void append(@NotNull DefaultMutableTreeNode root, @NotNull RTTIType<?> type, @NotNull Object value) {
        append(root, RTTITypeRegistry.getFullTypeName(type), type, value);
    }

    @SuppressWarnings("unchecked")
    public void append(@NotNull DefaultMutableTreeNode root, @Nullable String name, @NotNull RTTIType<?> type, @NotNull Object value) {
        final ValueHandler handler = ValueHandlerProvider.getValueHandler(type);
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        final StringBuilder sb = new StringBuilder("<html>");
        final String inline = handler.getInlineValue(type, value);

        if (name != null) {
            sb.append("<font color=#7f0000>%s</font> = ".formatted(escapeLabelName(name)));
        }

        if (inline != null) {
            sb.append(inline);
        } else {
            sb.append("<font color=gray>{%s}</font>".formatted(escapeLabelName(RTTITypeRegistry.getFullTypeName(type))));
        }

        if (handler instanceof ValueCollectionHandler) {
            final ValueCollectionHandler<Object, Object> container = (ValueCollectionHandler<Object, Object>) handler;
            final Collection<?> children = container.getChildren(type, value);

            if (type.getKind() == RTTIType.Kind.CONTAINER) {
                sb.append(" size = ").append(children.size());
            }

            for (Object child : children) {
                append(
                    node,
                    container.getChildName(type, value, child),
                    container.getChildType(type, value, child),
                    container.getChildValue(type, value, child)
                );
            }
        }

        sb.append("</html>");

        node.setUserObject(sb.toString());

        root.add(node);
    }

    @NotNull
    private static String escapeLabelName(@NotNull String label) {
        return label.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @NotNull
    private static String unescapeLabelName(@NotNull String label) {
        return label.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    private static class StyledListCellRenderer extends DefaultTreeCellRenderer {
        private static final Pattern TAG_PATTERN = Pattern.compile("<.*?>");

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value != null && selected) {
                value = unescapeLabelName(TAG_PATTERN.matcher(value.toString()).replaceAll(""));
            }

            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }
}