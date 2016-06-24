package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ConfigTest extends InstrumentationTestCase {

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

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
    public void test__getSdkVersion() throws Exception {

    }

    @Test
    public void test__setSdkVersion() throws Exception {

    }

    @Test
    public void test__getApiVersion() throws Exception {

    }

    @Test
    public void test__setApiVersion() throws Exception {

    }

    @Test
    public void test__getApplicationID() throws Exception {

    }

    @Test
    public void test__setApplicationID() throws Exception {

    }

    @Test
    public void test__getApplicationToken() throws Exception {

    }

    @Test
    public void test__setApplicationToken() throws Exception {

    }

    @Test
    public void test__getDatastoreID() throws Exception {

    }

    @Test
    public void test__setDatastoreID() throws Exception {

    }

    @Test
    public void test__discard() throws Exception {

    }*/

}