package com.shade.decima.ui.navigator;

import com.shade.decima.model.app.runtime.ProgressMonitor;
import com.shade.decima.model.util.NotNull;
import com.shade.decima.model.util.Nullable;

public abstract class NavigatorNode {
    private final NavigatorNode parent;

    public NavigatorNode(@Nullable NavigatorNode parent) {
        this.parent = parent;
    }

    @Nullable
    public NavigatorNode getParent() {
        return parent;
    }

    @NotNull
    public abstract String getLabel();

    @NotNull
    public abstract NavigatorNode[] getChildren(@NotNull ProgressMonitor monitor) throws Exception;

    @Override
    public String toString() {
        return getLabel();
    }
}
