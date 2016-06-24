package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ABTest extends InstrumentationTestCase {

    protected String url;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }
/*
    void sandbox() {

        //SDK初期化
        Context context = getApplicationContext();
        AB.Config.setApplicationID("SDKTestApp");
        AB.Config.setApplicationToken("XXXXXXXXXXXXXXXXXXXXXXXX");
        AB.Config.setDatastoreID("_sandbox");
        AB.activate(context);

        url = AB.serverURL();
        url = AB.baasAPIBaseURL("https://localhost", "v1");
        url = AB.baasTokenAPIURL("/endpoint");
        url = AB.baasTokenAPIURLWithFormat("/endpoint/%s", "dynamicParam");
        url = AB.baasUserAPIURL("/endpoint");
        url = AB.baasUserAPIURLWithFormat("/endpoint/%s", "dynamicParam");
        url = AB.baasDatastoreAPIURL("/endpoint");
        url = AB.baasDatastoreAPIURLWithFormat("/endpoint/%s", "dynamicParam");
        url = AB.baasFileAPIURL("/endpoint");
        url = AB.baasFileAPIURLWithFormat("/endpoint/%s", "dynamicParam");
        url = AB.baasPushAPIURL(AB.Platform.ANDROID, "/endpoint");
        url = AB.baasPushAPIURLWithFormat(AB.Platform.ANDROID, "/endpoint/%s", "dynamicParam");
        url = AB.baasPushAnalyticsAPIURL("/endpoint");
        url = AB.baasPushAnalyticsAPIURLWithFormat("/endpoint/%s", "dynamicParam");
        url = AB.baasSequenceAPIURL("/endpoint");
        url = AB.baasSequenceAPIURLWithFormat("/endpoint/%s", "dynamicParam");
    }

    @SuppressWarnings("all")
    private void sandbox__FileService() {
        ABFile file  = new ABFile("item_images");
        ABFile file2 = new ABFile("item_images");
        List<ABFile> files = Arrays.asList(file, file2);
        ABResult<File> objRet;
        ABResult<Void> voidRet;
        try {
            voidRet = AB.FileService.deleteAllSynchronously(files);
            voidRet = AB.FileService.deleteAllSynchronously(files, AB.FileDeleteOption.NONE);
            voidRet = AB.FileService.deleteAllSynchronously(files, EnumSet.of(AB.FileDeleteOption.NONE));
        } catch (ABException e) {
            e.printStackTrace();
        }
        AB.FileService.deleteAll(files);
        AB.FileService.deleteAll(files, AB.FileDeleteOption.NONE);
        AB.FileService.deleteAll(files, EnumSet.of(AB.FileDeleteOption.NONE));
        AB.FileService.deleteAll(files, new ResultCallback<Void>() {
            @Override
            public void done(ABResult<Void> result, ABException e) {}
        });
        AB.FileService.deleteAll(files, new ResultCallback<Void>() {
            @Override
            public void done(ABResult<Void> result, ABException e) {}
        }, AB.FileDeleteOption.NONE);
        AB.FileService.deleteAll(files, new ResultCallback<Void>() {
            @Override
            public void done(ABResult<Void> result, ABException e) {}
        }, EnumSet.of(AB.FileDeleteOption.NONE));

    }
*/
    @Before
    public void setUp() throws Exception {
        AB.Config.setDatastoreID("ds");
        AB.Config.setApplicationID("app");
        AB.Config.setApplicationToken("tokentokentokentokentokentokentokentokentokentokentoken");
        AB.activate(getApplicationContext());
    }

    @After
    public void tearDown() throws Exception {

    }

    /*@Test
    public void test__activate() throws Exception {

    }

    @Test
    public void test__serverURL() throws Exception {

    }

    @Test
    public void test__baasAPIBaseURL() throws Exception {

    }

    @Test
    public void test__baasTokenAPIURL() throws Exception {

    }

    @Test
    public void test__baasTokenAPIURLWithFormat() throws Exception {

    }

    @Test
    public void test__baasUserAPIURL() throws Exception {

    }

    @Test
    public void test__baasUserAPIURLWithFormat() throws Exception {

    }

    @Test
    public void test__baasDatastoreAPIURL() throws Exception {

    }

    @Test
    public void test__baasDatastoreAPIURLWithFormat() throws Exception {

    }

    @Test
    public void test__baasFileAPIURL() throws Exception {

    }

    @Test
    public void test__baasFileAPIURLWithFormat() throws Exception {

    }

    @Test
    public void test__baasPushAPIURL() throws Exception {

    }

    @Test
    public void test__baasPushAPIURLWithFormat() throws Exception {

    }

    @Test
    public void test__baasPushAnalyticsAPIURL() throws Exception {

    }

    @Test
    public void test__baasPushAnalyticsAPIURLWithFormat() throws Exception {

    }

    @Test
    public void test__baasSequenceAPIURL() throws Exception {

    }

    @Test
    public void test__baasSequenceAPIURLWithFormat() throws Exception {

    }*/

}