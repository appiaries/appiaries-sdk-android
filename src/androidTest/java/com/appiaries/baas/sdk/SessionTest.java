package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SessionTest extends InstrumentationTestCase {

    ABDevice device;
    ABUser user;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }
/*
    void sandbox() {

        //Session
        String token = AB.Session.getToken();
        device = AB.Session.getDevice();
        user = AB.Session.getUser();
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
    public void test__getToken() throws Exception {

    }

    @Test
    public void test__setToken() throws Exception {

    }

    @Test
    public void test__getDevice() throws Exception {

    }

    @Test
    public void test__setDevice() throws Exception {

    }

    @Test
    public void test__getUser() throws Exception {

    }

    @Test
    public void test__setUser() throws Exception {

    }*/

}