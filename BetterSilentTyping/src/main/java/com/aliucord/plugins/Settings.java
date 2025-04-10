package com.aliucord.plugins;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;
import com.aliucord.widgets.LinearLayout;
import com.discord.utilities.colors.ColorPickerUtils;
import com.discord.views.CheckedSetting;
import com.lytefast.flexinput.R;

import b.k.a.a.f;

public class Settings extends SettingsPage {
    private final SettingsAPI settings;

    public Settings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarTitle("BetterSilentTyping");

        int p = com.aliucord.utils.DimenUtils.defaultPadding;

        LinearLayout layout = new LinearLayout(view.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(p, p, p, p);

        // Enable/Disable plugin
        var enableToggle = Utils.createCheckedSetting(
            view.getContext(),
            CheckedSetting.ViewType.SWITCH,
            "Enable Silent Typing",
            "Whether to silently show typing without sending it to Discord."
        );
        enableToggle.setChecked(settings.getBool("enabled", true));
        enableToggle.setOnCheckedListener(checked -> settings.setBool("enabled", checked));
        layout.addView(enableToggle);

        // Delay setting
        var delayInput = new TextInput(view.getContext());
        delayInput.setHint("Typing Delay (ms)");
        delayInput.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        delayInput.getEditText().setText(String.valueOf(settings.getInt("delay", 500)));
        layout.addView(delayInput);

        // Color picker enable
        var enableTint = Utils.createCheckedSetting(
            view.getContext(),
            CheckedSetting.ViewType.SWITCH,
            "Enable Tint",
            "Enable custom keyboard tint color."
        );
        enableTint.setChecked(settings.getBool("0enabled", false));
        enableTint.setOnCheckedListener(checked -> settings.setBool("0enabled", checked));
        layout.addView(enableTint);

        // Color picker button
        var selectColor = new Button(view.getContext());
        selectColor.setText("Select Tint Color");
        selectColor.setOnClickListener(v -> {
            int currentColor = settings.getInt("0colorInt", Color.BLACK);
            var builder = ColorPickerUtils.INSTANCE.buildColorPickerDialog(
                view.getContext(),
                Utils.getResId("color_picker_title", "string"),
                currentColor
            );
            if (builder.getArguments() != null)
                builder.getArguments().putBoolean("alpha", true);

            builder.k = new f() {
                @Override
                public void onColorSelected(int i, int i2) {
                    settings.setInt("0colorInt", i2);
                    BetterSilentTyping.keyboard.setTint(i2);
                    Utils.showToast("Color selected: " + i2);
                }

                @Override
                public void onColorReset(int i) { }

                @Override
                public void onDialogDismissed(int i) { }
            };

            builder.show(getParentFragmentManager(), "COLOR_PICKER_KEYBOARD");
        });
        layout.addView(selectColor);

        // Optional: Hex input for color
        var hexInput = new TextInput(view.getContext());
        hexInput.setHint("Hex Color (e.g. #FF4081)");
        hexInput.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        hexInput.getEditText().setText("#" + Integer.toHexString(settings.getInt("0colorInt", Color.BLACK)));
        hexInput.setOnTextChangeListener(s -> {
            try {
                int parsed = Color.parseColor(s);
                settings.setInt("0colorInt", parsed);
                BetterSilentTyping.keyboard.setTint(parsed);
            } catch (Exception ignored) { }
        });
        layout.addView(hexInput);

        // Save delay value on back
        setOnBackPressed(() -> {
            try {
                int delay = Integer.parseInt(delayInput.getEditText().getText().toString());
                settings.setInt("delay", delay);
            } catch (NumberFormatException e) {
                Utils.showToast("Invalid delay input");
            }
            return false;
        });

        addView(layout);
    }
}