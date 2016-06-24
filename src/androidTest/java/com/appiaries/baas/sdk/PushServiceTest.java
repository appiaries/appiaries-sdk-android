package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PushServiceTest extends InstrumentationTestCase {

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
    public void test__openMessageSynchronously() throws Exception {

    }

    @Test
    public void test__openMessageSynchronously1() throws Exception {

    }

    @Test
    public void test__openMessage() throws Exception {

    }

    @Test
    public void test__openMessage1() throws Exception {

    }

    @Test
    public void test__openMessage2() throws Exception {

    }

    @Test
    public void test__openMessage3() throws Exception {

    }

    @Test
    public void testQuery() throws Exception {

    }*/

}