package com.shade.decima.ui.data.managers;

import com.shade.decima.ui.data.ValueController;
import com.shade.decima.ui.data.ValueEditor;
import com.shade.decima.ui.data.ValueManager;
import com.shade.decima.ui.data.ValueManagerRegistration;
import com.shade.decima.ui.data.editors.StringValueEditor;
import com.shade.util.NotNull;

@ValueManagerRegistration(names = {"String", "WString"})
public class StringValueManager implements ValueManager<String> {
    @NotNull
    @Override
    public ValueEditor<String> createEditor(@NotNull ValueController<String> controller) {
        return new StringValueEditor(controller);
    }
}