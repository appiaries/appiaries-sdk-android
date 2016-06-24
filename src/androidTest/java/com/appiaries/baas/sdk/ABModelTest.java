package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ABModelTest extends InstrumentationTestCase {

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

    /*
    @Test
    public void test__keySet() throws Exception {

    }

    @Test
    public void test__entrySet() throws Exception {

    }

    @Test
    public void test__get() throws Exception {

    }

    @Test
    public void test__get1() throws Exception {

    }

    @Test
    public void test__put() throws Exception {

    }

    @Test
    public void test__put1() throws Exception {

    }

    @Test
    public void test__remove() throws Exception {

    }

    @Test
    public void test__removeAll() throws Exception {

    }

    @Test
    public void test__clear() throws Exception {

    }

    @Test
    public void test__getAddedKeys() throws Exception {

    }

    @Test
    public void test__getAddedKeysAndValues() throws Exception {

    }

    @Test
    public void test__getRemovedKeys() throws Exception {

    }

    @Test
    public void test__getRemovedKeysAndValues() throws Exception {

    }

    @Test
    public void test__getUpdatedKeys() throws Exception {

    }

    @Test
    public void test__getUpdatedKeysAndValues() throws Exception {

    }

    @Test
    public void test__apply() throws Exception {

    }

    @Test
    public void test__revert() throws Exception {

    }

    @Test
    public void test__postRefreshProcessWithFetchedObject() throws Exception {

    }

    @Test
    public void test__inputDataFilter() throws Exception {

    }

    @Test
    public void test__outputDataFilter() throws Exception {

    }

    @Test
    public void test__getEstimatedData() throws Exception {

    }

    @Test
    public void test__setEstimatedData() throws Exception {

    }

    @Test
    public void test__getOriginalData() throws Exception {

    }

    @Test
    public void test__setOriginalData() throws Exception {

    }

    @Test
    public void test__isRegistered() throws Exception {

    }

    @Test
    public void test__getCollectionID() throws Exception {

    }

    @Test
    public void test__getCollectionID1() throws Exception {

    }

    @Test
    public void test__isNew() throws Exception {

    }

    @Test
    public void test__isDirty() throws Exception {

    }

    @Test
    public void test__getID() throws Exception {

    }

    @Test
    public void test__setID() throws Exception {

    }

    @Test
    public void test__getCreated() throws Exception {

    }

    @Test
    public void test__setCreated() throws Exception {

    }

    @Test
    public void test__getCreatedBy() throws Exception {

    }

    @Test
    public void test__setCreatedBy() throws Exception {

    }

    @Test
    public void test__getUpdated() throws Exception {

    }

    @Test
    public void test__setUpdated() throws Exception {

    }

    @Test
    public void test__getUpdatedBy() throws Exception {

    }

    @Test
    public void test__setUpdatedBy() throws Exception {

    }

    @Test
    public void test__toString() throws Exception {

    }
    */

}