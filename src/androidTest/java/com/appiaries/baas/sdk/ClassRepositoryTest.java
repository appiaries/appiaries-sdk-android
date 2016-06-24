package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import example.app.model.ExtendedDBObject;
import example.app.model.ExtendedFile;

@RunWith(AndroidJUnit4.class)
public class ClassRepositoryTest extends InstrumentationTestCase {

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

    @Test
    public void test__registerClass() throws Exception {
        AB.ClassRepository.registerClass(ExtendedDBObject.class);
        assertEquals(1, AB.ClassRepository.size());
    }

    @Test
    public void test__registerClasses() throws Exception {
        AB.ClassRepository.registerClasses(Arrays.asList(ExtendedDBObject.class, ExtendedFile.class));
        assertEquals(2, AB.ClassRepository.size());
    }

    /*@Test
    public void test__reset() throws Exception {

    }*/

}