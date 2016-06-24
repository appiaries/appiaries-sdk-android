package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ABExceptionTest extends InstrumentationTestCase {

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

}