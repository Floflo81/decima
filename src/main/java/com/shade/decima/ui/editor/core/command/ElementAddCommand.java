package com.shade.decima.ui.editor.core.command;

import com.shade.decima.model.rtti.RTTIType;
import com.shade.decima.model.rtti.types.RTTITypeArray;
import com.shade.decima.ui.editor.core.CoreNodeObject;
import com.shade.platform.model.runtime.VoidProgressMonitor;
import com.shade.platform.ui.commands.BaseCommand;
import com.shade.platform.ui.controls.tree.Tree;
import com.shade.util.NotNull;

public class ElementAddCommand extends BaseCommand {
    private final Tree tree;
    private final CoreNodeObject node;
    private final Object value;
    private final int index;

    public ElementAddCommand(@NotNull Tree tree, @NotNull CoreNodeObject node, @NotNull Object value, int index) {
        this.tree = tree;
        this.node = node;
        this.value = value;
        this.index = index;
    }

    @Override
    public void redo() {
        super.redo();

        try {
            node.setValue(getType().insert(node.getValue(), index, value));
            node.reloadChildren(new VoidProgressMonitor());
            tree.getModel().fireStructureChanged(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void undo() {
        super.undo();

        try {
            node.setValue(getType().remove(node.getValue(), index));
            node.reloadChildren(new VoidProgressMonitor());
            tree.getModel().fireStructureChanged(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    protected String getTitle() {
        return "Add new element";
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private RTTITypeArray<Object> getType() {
        return (RTTITypeArray<Object>) node.getType();
    }
}
