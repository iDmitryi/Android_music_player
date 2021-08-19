package com.example.audioplayer;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.test.filters.SdkSuppress;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiCollection;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)


public class AudioplayerTest {

    private static final String SAMPLE_PACKAGE =
            InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
            //"com.example.audioplayer";

    private static final int LAUNCH_TIMEOUT = 5000;

    private UiDevice mDevice;

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // Launch the main app
        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);


        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(SAMPLE_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void checkPreconditions(){ assertThat(mDevice, notNullValue());}

/*
    @Test
    public void checkIfScreenIsON() throws RemoteException {
        assertTrue(mDevice.isScreenOn());
    }

 */


    @Test
    public void checkAppLauched() { // ↑ verificare ca aplicatia este pornita cu succes
        // get context
        Context context = getApplicationContext();
        // get intent
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(SAMPLE_PACKAGE);
        // check the package
        assertThat("com.example.audioplayer", is(context.getPackageName()));
        // check if the intent is not null
        assertThat(intent, notNullValue());
    }

    @Test
    public void checkPlaylist() throws UiObjectNotFoundException { // ↑ verificare ca s-au incarcat melodii in playlist

        // check for a random song in playlist
        //UiCollection songs = new UiCollection(new UiSelector().className("android.widget.RelativeLayout"));
        //assertThat(songs, notNullValue());

        // check for a specific song in playlist
        UiScrollable songItem = new UiScrollable(new UiSelector().className("androidx.recyclerview.widget.RecyclerView"));
        UiObject song = songItem.getChildByText(new UiSelector().className("android.widget.RelativeLayout"), "16");
        //song.click();

        String text = song.getChild(new UiSelector().resourceId("com.example.audioplayer:id/music_file_name")).getText();
        assertThat(text, is("16"));
    }


    @Test
    public void checkFileDeleted() throws UiObjectNotFoundException { // ↑ verificare ca un fisier este sters din lista cu succes

        UiCollection songs = new UiCollection(new UiSelector().className("android.widget.RelativeLayout"));

        // Find menuMore and simulate a user-click on it
        UiObject menuMore = songs.getChild(new UiSelector().resourceId("com.example.audioplayer:id/menuMore"));
        menuMore.click();

        // Find "delete" button and click to delete the file
        UiObject delete = new UiObject(new UiSelector().className("android.widget.LinearLayout"));
        // if "delete" button was clicked a Toast will appear
        delete.click();

        // Find Toast message if exist
        UiObject wasDel = new UiObject(new UiSelector().resourceId("com.example.audioplayer:id/snackbar_text"));

        // check if file was deleted
        assertThat(wasDel.getText(), is("File Deleted"));
    }

    @Test
    public void checkID3tag() throws UiObjectNotFoundException { // ↑ verificare ca un fisier media contine tag id3

        UiCollection songs = new UiCollection(new UiSelector().className("android.widget.RelativeLayout"));

        // Find menuMore and simulate a user-click on it
        UiObject menuMore = songs.getChild(new UiSelector().resourceId("com.example.audioplayer:id/menuMore"));
        menuMore.click();

        // Find "info" button and click
        UiObject info = new UiObject(new UiSelector().className("android.widget.ListView"));
        info.getChild(new UiSelector().description("info")).click();

        // Find Toast message if exist
        UiObject id3 = new UiObject(new UiSelector().resourceId("com.example.audioplayer:id/snackbar_text"));

        assertThat(id3.getText(), is("ID3 tag Found"));
    }


    @Test
    public void checkNextBtnFromNotificationBar() throws UiObjectNotFoundException { // ↑ verificare ca se poate schimba melodia folosind meniul de notificari al os-ului

        // Find all songs and click one randomly
        UiCollection songs = new UiCollection(new UiSelector().className("android.widget.RelativeLayout"));
        songs.click();

        // open notification bar
        mDevice.openNotification();

        // get the resource ID for music controller
        UiObject btn = new UiObject(new UiSelector().resourceId("android:id/media_actions"));

        // find next, pause, previous and play buttons, click and check if works fine every click
        UiObject next = btn.getChild(new UiSelector().description("Next"));
        next.click();
        assertThat(next.getContentDescription(), is("Next"));

        UiObject pause = btn.getChild(new UiSelector().description("Pause"));
        pause.click();
        assertThat(pause.getContentDescription(), is("Pause"));

        UiObject prev = btn.getChild(new UiSelector().description("Previous"));
        prev.click();
        assertThat(prev.getContentDescription(), is("Previous"));

        UiObject play = btn.getChild(new UiSelector().description("Pause"));
        play.click();
        assertThat(play.getContentDescription(), is("Pause"));

    }



    // TODO → alte teste la care va puteti gandi pentru a verifica o anumita functionalitate

    // Uses package manager to find the package name of the device launcher.
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

}

