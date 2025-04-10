package com.aliucord.plugins;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.discord.app.AppBottomSheet;
import com.discord.views.CheckedSetting;
import com.discord.utilities.colors.ColorPickerUtils;
import com.discord.widgets.dialogs.ColorPickerDialog;

public class Settings extends AppBottomSheet {
    SettingsAPI settings;
    BetterSilentTyping plugin;

    public Settings(SettingsAPI set, BetterSilentTyping plugin) {
        settings = set;
        this.plugin = plugin;
    }

    @Override
    public int getContentViewResId() {
        return 0;
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        Context context = inflater.getContext();
        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);

        // Standard settings, e.g., show toast message and hide keyboard icon toggles:
        CheckedSetting showToastSetting = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Show Toast Message When Silent Typing Toggled",
            ""
        );
        showToastSetting.setChecked(settings.getBool("showToast", false));
        showToastSetting.setOnCheckedListener(bool -> settings.setBool("showToast", bool));
        lay.addView(showToastSetting);

        CheckedSetting hideKeyboardSetting = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Hide Keyboard Icon",
            ""
        );
        hideKeyboardSetting.setChecked(settings.getBool("hideKeyboard", false));
        hideKeyboardSetting.setOnCheckedListener(bool -> {
            settings.setBool("hideKeyboard", bool);
            plugin.setHideKeyboard(bool);
        });
        lay.addView(hideKeyboardSetting);

        CheckedSetting hideOnTextSetting = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Hide Keyboard When Text Gets Entered",
            ""
        );
        hideOnTextSetting.setChecked(settings.getBool("hideOnText", false));
        hideOnTextSetting.setOnCheckedListener(bool -> {
            settings.setBool("hideOnText", bool);
            if (bool) {
                plugin.patchHideKeybordOnText();
            } else {
                plugin.unpatchHideKeybordOnText();
            }
        });
        lay.addView(hideOnTextSetting);

        // --- Use Discord's default color picker for the keyboard icon ---
        Button keyboardColorButton = new Button(context);
        keyboardColorButton.setText("Keyboard Color");
        keyboardColorButton.setOnClickListener(v -> {
            // Retrieve the saved keyboard color (or a default value)
            int currentColor = settings.getInt("0colorInt", Color.parseColor("#BABBBF"));
            // Build the color picker dialog using Discord's default utility.
            // (This code follows the pattern shown in your Kotlin example plugin.)
            ColorPickerDialog pickerDialog = ColorPickerUtils.INSTANCE.buildColorPickerDialog(
                context,
                Utils.getResId("color_picker_title", "string"),
                currentColor
            );
            // Enable alpha support if desired:
            if (pickerDialog.getArguments() != null)
                pickerDialog.getArguments().putBoolean("alpha", true);
            // Set up a callback to receive the selected color.
            pickerDialog.setCallback(new ColorPickerDialog.Callback() {
                @Override
                public void onColorSelected(int primary, int color) {
                    settings.setInt("0colorInt", color);
                    BetterSilentTyping.keyboard.setTint(color);
                    Toast.makeText(context, "Keyboard color saved", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onColorReset(int color) {
                    // Optional: handle reset if needed.
                }
                @Override
                public void onDialogDismissed(int color) {
                    // Optional: perform any cleanup.
                }
            });
            // Show the dialog using the fragment manager; since this Settings class extends AppBottomSheet
            // (which in turn is a Fragment), you can call getParentFragmentManager().
            FragmentManager fm = getParentFragmentManager();
            pickerDialog.show(fm, "COLOR_PICKER_KEYBOARD");
        });
        lay.addView(keyboardColorButton);

        // --- Use Discord's default color picker for the disabled icon ---
        Button disabledIconColorButton = new Button(context);
        disabledIconColorButton.setText("Disabled Icon Color");
        disabledIconColorButton.setOnClickListener(v -> {
            int currentColor = settings.getInt("1colorInt", Color.RED);
            ColorPickerDialog pickerDialog = ColorPickerUtils.INSTANCE.buildColorPickerDialog(
                context,
                Utils.getResId("color_picker_title", "string"),
                currentColor
            );
            if (pickerDialog.getArguments() != null)
                pickerDialog.getArguments().putBoolean("alpha", true);
            pickerDialog.setCallback(new ColorPickerDialog.Callback() {
                @Override
                public void onColorSelected(int primary, int color) {
                    settings.setInt("1colorInt", color);
                    BetterSilentTyping.disableImage.setTint(color);
                    Toast.makeText(context, "Disabled icon color saved", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onColorReset(int color) {
                    // Optional callback method.
                }
                @Override
                public void onDialogDismissed(int color) {
                    // Optional callback method.
                }
            });
            pickerDialog.show(getParentFragmentManager(), "COLOR_PICKER_DISABLED");
        });
        lay.addView(disabledIconColorButton);

        return lay;
    }
}