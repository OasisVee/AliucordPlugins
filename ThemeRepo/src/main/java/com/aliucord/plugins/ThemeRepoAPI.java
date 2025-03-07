package com.aliucord.plugins;

import static com.aliucord.plugins.ThemeRepoUtils.THEME_DIR;
import static com.aliucord.plugins.ThemeRepoUtils.getFileNameWithName;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.SettingsUtilsJSON;
import com.aliucord.Utils;
import com.aliucord.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThemeRepoAPI {
    public static final String API_URL = "https://mantikralligi1.pythonanywhere.com";
    public static final String GITHIB_THEMEREPO_URL = "https://raw.githubusercontent.com/OasisVee/AliucordThemeRepo/main/";
    public static HashMap<String, Object> localFilters = new HashMap<>();
    public static HashMap<String, String> filters;

    public static List<Theme> getThemes(){
        try {
            var response = Http.simpleGet(GITHIB_THEMEREPO_URL + "themeList.json");
            var themes = (List<Theme>)GsonUtils.fromJson(response, TypeToken.getParameterized(ArrayList.class,Theme.class).getType());
            return themes;
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean addTheme(Theme theme,String token,String themeJSON) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("token",token);
            payload.put("themeInfo",GsonUtils.toJson(theme));
            payload.put("theme",themeJSON);
        } catch (JSONException e) { e.printStackTrace(); }

        try {
            String response = Http.simplePost(API_URL + "addTheme",payload.toString());
            if (response.equals("successful")) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean setThemeStatus(String name, int transparency, boolean status) {
        try {
            var settings = new SettingsUtilsJSON("Themer");
            settings.setBool(name + "-enabled", status);
            if (transparency != -1) settings.setInt("transparencyMode", transparency);
            return true;
        } catch (Exception ignored) { }
        return false;
    }

    public static boolean isThemeEnabled(String themeName) {
        return new SettingsUtilsJSON("Themer").getBool(themeName + "-enabled", false);
    }

    public static boolean exists(String name) {
        var themesMap = ThemeRepoUtils.getThemeNames();
        return (themesMap.containsKey(name) || themesMap.containsValue(name));

    }

    public static boolean installTheme(Theme theme) {
        try {
            new Http.Request(GITHIB_THEMEREPO_URL + "/themes/" + theme.fileName).execute().saveToFile(new File(THEME_DIR, theme.fileName));

            Utils.showToast("Successfully installed Theme");
            setThemeStatus(theme.name, theme.transparencyMode, true);
            Utils.promptRestart();
            return true;
        } catch (IOException e) {
            new Logger("ThemeRepoInstaller").error(e);
            Utils.showToast("Failed to install theme (definiletly not my fault)");
            return false;
        }
    }

    public static boolean deleteTheme(String name) {
        var fileName = getFileNameWithName(name);

        boolean status = false;
        if (fileName != null) status = new File(THEME_DIR, fileName).delete();
        if (status) {
            Utils.showToast("Successfully Uninstalled Theme");
            ThemeRepoUtils.themes.remove(fileName);
        } else Utils.showToast("Failed to uninstall theme");
        setThemeStatus(name, -1, false);
        return status;
    }
}
