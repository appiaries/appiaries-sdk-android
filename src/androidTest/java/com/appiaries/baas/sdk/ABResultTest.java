package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ABResultTest extends InstrumentationTestCase {

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
    public void test__hasError() throws Exception {

    }

    @Test
    public void test__getData() throws Exception {

    }

    @Test
    public void test__setData() throws Exception {

    }

    @Test
    public void test__getRawData() throws Exception {

    }

    @Test
    public void test__setRawData() throws Exception {

    }

    @Test
    public void test__getStatusCode() throws Exception {

    }

    @Test
    public void test__setStatusCode() throws Exception {

    }

    @Test
    public void test__getException() throws Exception {

    }

    @Test
    public void test__setException() throws Exception {

    }

    @Test
    public void test__getExtra() throws Exception {

    }

    @Test
    public void test__setExtra() throws Exception {

    }

    @Test
    public void test__putExtra() throws Exception {

    }

    @Test
    public void test__getTotal() throws Exception {

    }

    @Test
    public void test__setTotal() throws Exception {

    }

    @Test
    public void test__getStart() throws Exception {

    }

    @Test
    public void test__setStart() throws Exception {

    }

    @Test
    public void test__getEnd() throws Exception {

    }

    @Test
    public void test__setEnd() throws Exception {

    }

    @Test
    public void test__hasNext() throws Exception {

    }

    @Test
    public void test__setNext() throws Exception {

    }

    @Test
    public void test__hasPrevious() throws Exception {

    }

    @Test
    public void test__setPrevious() throws Exception {

    }*/

}