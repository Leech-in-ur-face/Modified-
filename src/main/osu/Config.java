package main.osu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.edlplan.favorite.FavoriteLibrary;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.util.Debug;

import main.osu.helper.FileUtils;

public class Config {
    private static String corePath,
        defaultCorePath,
        beatmapPath,
        cachePath,
        skinPath,
        skinTopPath,
        scorePath,
        onlineUsername,
        onlinePassword,
        onlineDeviceID;

    private static boolean
        SCAN_DOWNLOAD,
        showFirstApproachCircle,
        comboburst,
        useCustomSkins,
        useCustomSounds,
        corovans,
        showCursor,
        accurateSlider,
        shrinkPlayfieldDownwards,
        stayOnline,
        syncMusic,
        burstEffects,
        hitLighting,
        useTrail,
        useLongTrail,
        forceRomanized,
        fixFrameOffset,
        removeSliderLock,
        calculateSliderPathInGameStart,
        hideReplayMarquee,
        hideInGameUI,
        receiveAnnouncements,
        enableStoryboard,
        safeBeatmapBg,
        showItemBackground;

    private static int RES_WIDTH,
        RES_HEIGHT,
        errorMeter,
        spinnerStyle,
        textureQuality,
        metronomeSwitch;
    
    private static float soundVolume,
        bgmVolume,
        offset,
        backgroundBrightness,
        scaleMultiplier,
        playfieldSize,
        cursorSize;

    private static Map<String, String> skins;

    private static Context context;

    public static void loadConfig(final Context context) {
        Config.context = context;
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        // graphics
        useCustomSkins = prefs.getBoolean("skin", false);
        useCustomSounds = prefs.getBoolean("beatmapSounds", true);
        comboburst = prefs.getBoolean("comboburst", false);
        corovans = prefs.getBoolean("images", false);
        textureQuality = prefs.getBoolean("lowtextures", false) ? 2 : 1;
        errorMeter = Integer.parseInt(prefs.getString("errormeter", "0"));
        spinnerStyle = Integer.parseInt(prefs.getString("spinnerstyle", "0"));
        showFirstApproachCircle = prefs.getBoolean("showfirstapproachcircle", false);
        metronomeSwitch = Integer.parseInt(prefs.getString("metronomeswitch", "1"));
        enableStoryboard = prefs.getBoolean("enableStoryboard", false);

        // UI
        showItemBackground = prefs.getBoolean("ui_showItemBackground", true);

        setSize();

        setPlayfieldSize(Integer.parseInt(prefs.getString(
            "playfieldsize", "100")) / 100f);
        shrinkPlayfieldDownwards = prefs.getBoolean("shrinkPlayfieldDownwards", true);
        accurateSlider = true;

        try {
            int off = prefs.getInt("offset", 0);
            offset = (int) (Math.signum(off) * Math.min(250, Math.abs(off)));
            backgroundBrightness = prefs.getInt("bgbrightness", 25) / 100f;
            soundVolume = prefs.getInt("soundvolume", 100) / 100f;
            bgmVolume = prefs.getInt("bgmvolume", 100) / 100f;
            cursorSize = prefs.getInt("cursorSize", 50) / 100f;
        }catch(RuntimeException e) { // use valid integer since this makes the game crash on android m
            prefs.edit()
                .putInt("offset", 0)
                .putInt("bgbrightness", 25)
                .putInt("soundvolume", 100)
                .putInt("bgmvolume", 100)
                .putInt("cursorSize", 50)
                .commit();
            Config.loadConfig(context);
        }

        //advanced
        syncMusic = prefs.getBoolean("syncMusic", syncMusic);
        if (prefs.getBoolean("lowDelay", true)) {
            Engine.INPUT_PAUSE_DURATION = 0;
        } else {
            Engine.INPUT_PAUSE_DURATION = 20;
        }
        cachePath = context.getCacheDir().getPath();
        burstEffects = prefs.getBoolean("bursts", true);
        hitLighting = prefs.getBoolean("hitlighting", true);
        useTrail = prefs.getBoolean("particles", true);

        // beatmaps
        SCAN_DOWNLOAD = prefs.getBoolean("scandownload", true);
        forceRomanized = prefs.getBoolean("forceromanized", false);

        // other
        showCursor = prefs.getBoolean("showcursor", true);
        fixFrameOffset = prefs.getBoolean("fixFrameOffset", true);
        removeSliderLock = prefs.getBoolean("removeSliderLock", false);
        calculateSliderPathInGameStart = prefs.getBoolean("calculateSliderPathInGameStart", false);
        hideReplayMarquee = prefs.getBoolean("hideReplayMarquee", false);
        hideInGameUI = prefs.getBoolean("hideInGameUI", false);
        receiveAnnouncements = prefs.getBoolean("receiveAnnouncements", true);
        safeBeatmapBg = prefs.getBoolean("safebeatmapbg", false);

        if(receiveAnnouncements) {
            FirebaseMessaging.getInstance().subscribeToTopic("announcements");
        }else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("announcements"); 
        }

        //Init
        onlineDeviceID = prefs.getString("installID", null);
        if (onlineDeviceID == null) {
            onlineDeviceID = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            Editor editor = prefs.edit();
            editor.putString("installID", onlineDeviceID);
            editor.putString("corePath", corePath);
            editor.putString("skinTopPath", skinTopPath);
            editor.putString("skinPath", skinPath);
            editor.commit();
        }

        loadPaths();
        loadOnlineConfig(context);
        FavoriteLibrary.get().load();
    }

    public static void loadPaths() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        defaultCorePath = Environment.getExternalStorageDirectory() + "/osu!droid/";
        corePath = prefs.getString("corePath", defaultCorePath);

        File[] volumes = context.getExternalFilesDirs(null);

        boolean useExternal = prefs.getBoolean("external", false);
        boolean hasExternal = volumes.length > 1 && volumes[1] != null;

        if (useExternal && hasExternal) {
            corePath = volumes[1].getAbsolutePath();
            Log.i("Settings", "Using external storage as core path.");
        }

        if (corePath.length() == 0) {
            corePath = defaultCorePath;
        }
        if (corePath.charAt(corePath.length() - 1) != '/') {
            corePath += "/";
        }
        scorePath = corePath + "Scores/";
        skinTopPath = corePath + "Skin/";
        beatmapPath = corePath + "Songs/";

        skinPath = prefs.getString("skinPath", skinTopPath);
        if (!skinPath.contains(skinTopPath)) {
            skinPath = skinTopPath;
        }
        if (skinPath.length() == 0) {
            skinPath = corePath + "Skin/";
        }
        if (skinPath.charAt(skinPath.length() - 1) != '/') {
            skinPath += "/";
        }
    }

    public static void loadOnlineConfig(final Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        onlineUsername = prefs.getString("onlineUsername", "");
        onlinePassword = prefs.getString("onlinePassword", null);
        stayOnline = prefs.getBoolean("stayOnline", false);
    }

    public static void setSize() {
        final DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = Math.max(dm.widthPixels, dm.heightPixels), height = Math.min(dm.widthPixels, dm.heightPixels);
        setSize(width, height);
    }

    public static void setSize(int width, int height) {
        RES_WIDTH = 1280;
        RES_HEIGHT = 1280 * height / width;
    }

    public static boolean isEnableStoryboard() {
        return backgroundBrightness > 0.02 && enableStoryboard;
    }

    public static void setEnableStoryboard(boolean enableStoryboard) {
        Config.enableStoryboard = enableStoryboard;
    }

    public static boolean isUseSuperSlider() {
        return true;
    }

    public static boolean isFixFrameOffset() {
        return fixFrameOffset;
    }

    public static boolean isRemoveSliderLock() {
        return removeSliderLock;
    }

    public static boolean isCalculateSliderPathInGameStart() {
        return calculateSliderPathInGameStart;
    }

    public static boolean isCorovans() {
        return corovans;
    }

    public static void setCorovans(final boolean corovans) {
        Config.corovans = corovans;
    }

    public static float getSoundVolume() {
        return soundVolume;
    }

    public static void setSoundVolume(final float volume) {
        Config.soundVolume = volume;
    }

    public static float getBgmVolume() {
        return bgmVolume;
    }

    public static void setBgmVolume(float bgmVolume) {
        Config.bgmVolume = bgmVolume;
    }

    public static float getOffset() {
        return offset;
    }

    public static void setOffset(final float offset) {
        Config.offset = offset;
    }

    public static String getCorePath() {
        return corePath;
    }

    public static String getBeatmapPath() {
        return beatmapPath;
    }

    public static void setBeatmapPath(final String path) {
        beatmapPath = path;
    }

    public static int getRES_WIDTH() {
        return RES_WIDTH;
    }

    public static int getRES_HEIGHT() {
        return RES_HEIGHT;
    }

    public static boolean isDELETE_OSZ() {
        return true;
    }

    public static boolean isSCAN_DOWNLOAD() {
        return SCAN_DOWNLOAD;
    }

    public static boolean isUseCustomSkins() {
        return useCustomSkins;
    }

    public static boolean isUseCustomSounds() {
        return useCustomSounds;
    }

    public static int getTextureQuality() {
        return textureQuality;
    }

    public static float getBackgroundBrightness() {
        return backgroundBrightness;
    }

    public static void setBackgroundBrightness(final float backgroundBrightness) {
        Config.backgroundBrightness = backgroundBrightness;
    }

    public static boolean isSliderBorders() {
        return true;
    }

    public static boolean isComplexAnimations() {
        return true;
    }

    public static String getLocalUsername() {
        return "Guest";
    }

    public static boolean isShowCursor() {
        return showCursor;
    }

    public static void setShowCursor(final boolean showCursor) {
        Config.showCursor = showCursor;
    }

    public static boolean isAccurateSlider() {
        return accurateSlider;
    }

    public static void setAccurateSlider(final boolean accurateSlider) {
        Config.accurateSlider = accurateSlider;
    }

    public static float getScaleMultiplier() {
        return scaleMultiplier;
    }

    public static void setScaleMultiplier(final float scaleMultiplier) {
        Config.scaleMultiplier = scaleMultiplier;
    }

    public static String getOnlineUsername() {
        return onlineUsername;
    }

    public static void setOnlineUsername(String onlineUsername) {
        Config.onlineUsername = onlineUsername;
    }

    public static String getOnlinePassword() {
        return onlinePassword;
    }

    public static void setOnlinePassword(String onlinePassword) {
        Config.onlinePassword = onlinePassword;
    }

    public static boolean isStayOnline() {
        return stayOnline && BuildType.hasOnlineAccess();
    }

    public static void setStayOnline(boolean stayOnline) {
        Config.stayOnline = stayOnline;
    }

    public static String getOnlineDeviceID() {
        return onlineDeviceID;
    }

    public static boolean isSyncMusic() {
        return syncMusic;
    }

    public static void setSyncMusic(boolean syncMusic) {
        Config.syncMusic = syncMusic;
    }

    public static String getCachePath() {
        return cachePath;
    }

    public static void setCachePath(String cachePath) {
        Config.cachePath = cachePath;
    }

    public static boolean isBurstEffects() {
        return burstEffects;
    }

    public static void setBurstEffects(boolean burstEffects) {
        Config.burstEffects = burstEffects;
    }

    public static boolean isHitLighting() {
        return hitLighting;
    }

    public static void setHitLighting(boolean hitLighting) {
        Config.hitLighting = hitLighting;
    }

    public static boolean isUseDither() {
        return true;
    }

    public static boolean isUseCursorTrail() {
        return useTrail;
    }

    public static boolean isUseLongTrail() {
        return useLongTrail;
    }

    public static void setUseLongTrail(boolean useLongTrail) {
        Config.useLongTrail = useLongTrail;
    }

    public static String getSkinPath() {
        return skinPath;
    }

    public static void setSkinPath(String skinPath) {
        Config.skinPath = skinPath;
    }

    public static String getSkinTopPath() {
        return skinTopPath;
    }

    public static void setSkinTopPath(String skinTopPath) {
        Config.skinTopPath = skinTopPath;
    }

    public static boolean isHideNaviBar() {
        return true;
    }

    public static boolean isEnablePP() {
        return true;
    }

    public static String getScorePath() {
        return scorePath;
    }

    public static void setScorePath(String scorePath) {
        Config.scorePath = scorePath;
    }

    public static boolean isUseCustomComboColors() {
        return false;
    }

    public static RGBColor[] getComboColors() {
        return new RGBColor[] {
                new RGBColor(0, 255, 0), // Green
                new RGBColor(0, 0, 255), // Blue
                new RGBColor(255, 0, 0), // Red
                new RGBColor(255, 255, 0) // Yellow
        };
    }

    public static int getErrorMeter() {
        return errorMeter;
    }

    public static void setErrorMeter(int errorMeter) {
        Config.errorMeter = errorMeter;
    }

    public static int getSpinnerStyle() {
        return spinnerStyle;
    }

    public static void setSpinnerStyle(int spinnerStyle) {
        Config.spinnerStyle = spinnerStyle;
    }

    public static boolean isShowFirstApproachCircle() {
        return showFirstApproachCircle;
    }

    public static void setShowFirstApproachCircle(boolean showFirstApproachCircle) {
        Config.showFirstApproachCircle = showFirstApproachCircle;
    }

    public static int getMetronomeSwitch() {
        return metronomeSwitch;
    }

    public static void setMetronomeSwitch(int metronomeSwitch) {
        Config.metronomeSwitch = metronomeSwitch;
    }

    public static boolean isComboburst() {
        return true;
    }

    public static boolean isForceRomanized() {
        return forceRomanized;
    }

    public static void setForceRomanized(boolean forceRomanized) {
        Config.forceRomanized = forceRomanized;
    }

    public static float getCursorSize() {
        return cursorSize;
    }

    public static void setCursorSize() {
        Config.cursorSize = cursorSize;
    }

    public static float getPlayfieldSize() {
        return playfieldSize;
    }

    public static void setPlayfieldSize(final float playfieldSize) {
        Config.playfieldSize = playfieldSize;
    }

    public static boolean isShrinkPlayfieldDownwards() {
        return shrinkPlayfieldDownwards;
    }

    public static void setShrinkPlayfieldDownwards(boolean shrinkPlayfieldDownwards) {
        Config.shrinkPlayfieldDownwards = shrinkPlayfieldDownwards;
    }

    public static boolean isHideReplayMarquee() {
        return hideReplayMarquee;
    }

    public static void setHideReplayMarquee(boolean hideReplayMarquee) {
        Config.hideReplayMarquee = hideReplayMarquee;
    }

    public static boolean isHideInGameUI() {
        return hideInGameUI;
    }

    public static void setHideInGameUI(boolean hideInGameUI) {
        Config.hideInGameUI = hideInGameUI;
    }

    public static boolean isReceiveAnnouncements() {
        return receiveAnnouncements;
    }

    public static void setReceiveAnnouncements(boolean receiveAnnouncements) {
        Config.receiveAnnouncements = receiveAnnouncements;
    }

    public static boolean isSafeBeatmapBg() {
        return safeBeatmapBg;
    }

    public static void setSafeBeatmapBg(boolean safeBeatmapBg) {
        Config.safeBeatmapBg = safeBeatmapBg;
    }

    public static String getDefaultCorePath() {
        return defaultCorePath;
    }

    public static void loadSkins() {
        File[] folders = FileUtils.listFiles(new File(skinTopPath), file -> file.isDirectory() && !file.getName().startsWith("."));
        skins = new HashMap<String, String>();
        for(File folder : folders) {
            skins.put(folder.getName(), folder.getPath());
            Debug.i("skins: " + folder.getName() + " - " + folder.getPath());
        }
    }

    public static Map<String, String> getSkins(){
        return skins;
    }

    public static void addSkin(String name, String path) {
        if(skins == null) skins = new HashMap<String, String>();
        skins.put(name, path);
    }

    public static boolean isItemBackground() {
        return showItemBackground;
    }
}