package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceServiceTest extends InstrumentationTestCase {

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
    public void test__registerSynchronously() throws Exception {

    }

    @Test
    public void test__registerSynchronously1() throws Exception {

    }

    @Test
    public void test__register() throws Exception {

    }

    @Test
    public void test__register1() throws Exception {

    }

    @Test
    public void test__register2() throws Exception {

    }

    @Test
    public void test__register3() throws Exception {

    }

    @Test
    public void test__unregisterSynchronously() throws Exception {

    }

    @Test
    public void test__unregisterSynchronously1() throws Exception {

    }

    @Test
    public void test__unregister() throws Exception {

    }

    @Test
    public void test__unregister1() throws Exception {

    }

    @Test
    public void test__unregister2() throws Exception {

    }

    @Test
    public void test__unregister3() throws Exception {

    }

    @Test
    public void test__saveSynchronously() throws Exception {

    }

    @Test
    public void test__saveSynchronously1() throws Exception {

    }

    @Test
    public void test__save() throws Exception {

    }

    @Test
    public void test__save1() throws Exception {

    }

    @Test
    public void test__save2() throws Exception {

    }

    @Test
    public void test__save3() throws Exception {

    }

    @Test
    public void test__saveAllSynchronously() throws Exception {

    }

    @Test
    public void test__saveAllSynchronously1() throws Exception {

    }

    @Test
    public void test__saveAll() throws Exception {

    }

    @Test
    public void test__saveAll1() throws Exception {

    }

    @Test
    public void test__saveAll2() throws Exception {

    }

    @Test
    public void test__saveAll3() throws Exception {

    }

    @Test
    public void test__saveAll4() throws Exception {

    }

    @Test
    public void test__saveAll5() throws Exception {

    }

    @Test
    public void test__query() throws Exception {

    }*/

}