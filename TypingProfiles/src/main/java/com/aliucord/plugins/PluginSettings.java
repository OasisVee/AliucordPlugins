package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Button;
import com.aliucord.views.TextInput;

@SuppressLint("SetTextI18n")
public final class PluginSettings extends SettingsPage {
    private final SettingsAPI settings;

    public PluginSettings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        setActionBarTitle("Typing Profiles");

        var ctx = requireContext();

        var input = new TextInput(ctx);
        input.setHint("Replace \"typing\" with...");

        var editText = input.getEditText();

        var button = new Button(ctx);
        button.setText("Save");
        button.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) {
                text = "typing";
            }
            settings.setString("typingReplacement", text);
            Utils.showToast("Saved!");
            close();
        });

        editText.setMaxLines(1);
        editText.setText(settings.getString("typingReplacement", "typing"));

        addView(input);
        addView(button);
    }
}