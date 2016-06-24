package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SequenceServiceTest extends InstrumentationTestCase {

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
    public void test__addSynchronously() throws Exception {

    }

    @Test
    public void test__addSynchronously1() throws Exception {

    }

    @Test
    public void test__add() throws Exception {

    }

    @Test
    public void test__add1() throws Exception {

    }

    @Test
    public void test__add2() throws Exception {

    }

    @Test
    public void test__add3() throws Exception {

    }

    @Test
    public void test__fetchSynchronously() throws Exception {

    }

    @Test
    public void test__fetchSynchronously1() throws Exception {

    }

    @Test
    public void test__fetch() throws Exception {

    }

    @Test
    public void test__fetch1() throws Exception {

    }

    @Test
    public void test__resetSynchronously() throws Exception {

    }

    @Test
    public void test__resetSynchronously1() throws Exception {

    }

    @Test
    public void test__reset() throws Exception {

    }

    @Test
    public void test__reset1() throws Exception {

    }

    @Test
    public void test__reset2() throws Exception {

    }

    @Test
    public void test__reset3() throws Exception {

    }

    @Test
    public void test__query() throws Exception {

    }*/

}