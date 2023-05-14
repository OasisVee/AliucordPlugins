package com.aliucord.plugins;

import static com.aliucord.plugins.ReviewDBAPI.API_URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;

import com.aliucord.Constants;
import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.NotificationsAPI;
import com.aliucord.api.PatcherAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.NotificationData;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.discord.stores.StoreStream;
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheet;
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheetViewModel;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@AliucordPlugin
public class ReviewDB extends Plugin {
    public static SettingsAPI staticSettings;
    public static Logger logger = new Logger("UserReviews");
    int viewID = View.generateViewId();
    public static List<Long> AdminList = new ArrayList<>();
    public static FragmentManager fragmentManager;
    public static PatcherAPI staticPatcher;

    @SuppressLint("SetTextI18n")
    @Override
    public void start(Context context) {

        staticSettings = settings;
        staticPatcher = patcher;

        settingsTab = new SettingsTab(BottomShit.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);

        var userReviewFile = new File(Constants.PLUGINS_PATH , "UserReviews.zip");
        var serverReviewFile = new File(Constants.PLUGINS_PATH , "ServerReviews.zip");

        if (userReviewFile.exists() || serverReviewFile.exists()) {
            PluginManager.stopPlugin("UserReviews");
            PluginManager.disablePlugin("UserReviews");
            PluginManager.stopPlugin("ServerReviews");
            PluginManager.disablePlugin("ServerReviews");

            userReviewFile.delete();
            serverReviewFile.delete();

            var reviewDBFile = new File(Constants.PLUGINS_PATH ,"ReviewDB.zip");

            if (!reviewDBFile.exists()) {

                Utils.threadPool.execute(() ->{
                    try {
                        var response = new Http.Request("https://github.com/mantikafasi/AliucordPlugins/raw/builds/ReviewDB.zip").execute();
                        response.saveToFile(reviewDBFile);

                        PluginManager.loadPlugin(Utils.getAppContext(), reviewDBFile);
                        PluginManager.startPlugin("ReviewDB");

                    } catch (IOException e) {
                        logger.error(e);
                    }
                });
            }
        }

        new SettingsAPI("ReviewDBCache").resetSettings();

        if (settings.getBool("notifyNewReviews", true)) {
            Utils.threadPool.execute(() -> {
                try {
                    AdminList = new Http.Request(API_URL + "/admins").execute().json(TypeToken.getParameterized(List.class, Long.class).type);
                } catch (IOException e) {
                    logger.error(e);
                }

                var userid = StoreStream.getUsers().getMe().getId();
                try { Thread.sleep(6000); } catch (InterruptedException e) { e.printStackTrace(); }
                int id = ReviewDBAPI.getLastReviewID(userid);
                int lastReviewID = settings.getInt("lastreviewid",0);
                if (id > lastReviewID) {
                    settings.setInt("lastreviewid",id);

                    if (lastReviewID != 0) {
                        NotificationsAPI.display(new NotificationData()
                                    .setTitle("ReviewDB")
                                .setBody("You Have New Reviews On Your Profile")
                                .setOnClick(view -> {
                                    WidgetUserSheet.Companion.show(userid,Utils.widgetChatList.getParentFragmentManager());
                                    return null;
                                }));
                    }
                }
            });
        }

        try {
            patcher.patch(WidgetGuildProfileSheet.class.getDeclaredMethod("configureUI", WidgetGuildProfileSheetViewModel.ViewState.Loaded.class), new Hook(cf -> {
                var viewstate = (WidgetGuildProfileSheetViewModel.ViewState.Loaded) cf.args[0];
                fragmentManager = ((WidgetGuildProfileSheet) cf.thisObject).getChildFragmentManager();

                var linearLayout = (LinearLayout) (WidgetGuildProfileSheet.access$getGuildActionBinding$p((WidgetGuildProfileSheet) cf.thisObject)).getRoot();
                var ctx = linearLayout.getContext();
                if (linearLayout.findViewById(viewID) == null) {

                    var root = new ReviewDBView(ctx, viewstate.getGuildId(), ReviewDBView.PaddingType.Server);
                    root.setId(viewID);
                    linearLayout.addView(root);
                }

            }));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


        try {
            patcher.patch(WidgetUserSheet.class.getDeclaredMethod("configureUI", WidgetUserSheetViewModel.ViewState.class), new Hook(cf -> {
                var viewstate = (WidgetUserSheetViewModel.ViewState.Loaded) cf.args[0];

                var scrollView = (NestedScrollView) (WidgetUserSheet.access$getBinding$p((WidgetUserSheet) cf.thisObject)).getRoot();
                var ctx = scrollView.getContext();
                fragmentManager = ((WidgetUserSheet) cf.thisObject).getChildFragmentManager();

                if (scrollView.findViewById(viewID) == null) {

                    var root = new ReviewDBView(ctx, viewstate.getUser().getId());
                    root.setId(viewID);
                    ((LinearLayout) scrollView.findViewById(Utils.getResId("user_sheet_content", "id"))).addView(root);
                }

            }));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
        commands.unregisterAll();
    }
}
