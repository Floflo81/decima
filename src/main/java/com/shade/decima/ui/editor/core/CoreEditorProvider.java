package com.shade.decima.ui.editor.core;

import com.shade.decima.ui.editor.FileEditorInput;
import com.shade.platform.ui.editors.Editor;
import com.shade.platform.ui.editors.EditorInput;
import com.shade.platform.ui.editors.EditorProvider;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

import javax.swing.*;

public class CoreEditorProvider implements EditorProvider {
    @NotNull
    @Override
    public Editor createEditor(@NotNull EditorInput input) {
        return new CoreEditor((FileEditorInput) input);
    }

    @NotNull
    @Override
    public Match matches(@NotNull EditorInput input) {
        return input instanceof FileEditorInput ? Match.APPLIES : Match.NONE;
    }

    @NotNull
    @Override
    public String getName() {
        return "Core Editor";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return UIManager.getIcon("Navigator.coreIcon");
    }
}
