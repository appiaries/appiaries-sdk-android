package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FileServiceTest extends InstrumentationTestCase {

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
    public void test__save4() throws Exception {

    }

    @Test
    public void test__save5() throws Exception {

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
    public void test__deleteSynchronously() throws Exception {

    }

    @Test
    public void test__deleteSynchronously1() throws Exception {

    }

    @Test
    public void test__delete() throws Exception {

    }

    @Test
    public void test__delete1() throws Exception {

    }

    @Test
    public void test__delete2() throws Exception {

    }

    @Test
    public void test__delete3() throws Exception {

    }

    @Test
    public void test__deleteAllSynchronously() throws Exception {

    }

    @Test
    public void test__deleteAllSynchronously1() throws Exception {

    }

    @Test
    public void test__deleteAll() throws Exception {

    }

    @Test
    public void test__deleteAll1() throws Exception {

    }

    @Test
    public void test__deleteAll2() throws Exception {

    }

    @Test
    public void test__deleteAll3() throws Exception {

    }

    @Test
    public void test__deleteSynchronouslyWithQuery() throws Exception {

    }

    @Test
    public void test__dDeleteSynchronouslyWithQuery1() throws Exception {

    }

    @Test
    public void test__deleteWithQuery() throws Exception {

    }

    @Test
    public void test__deleteWithQuery1() throws Exception {

    }

    @Test
    public void test__deleteWithQuery2() throws Exception {

    }

    @Test
    public void test__deleteWithQuery3() throws Exception {

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
    public void test__findSynchronouslyWithQuery() throws Exception {

    }

    @Test
    public void test__findSynchronouslyWithQuery1() throws Exception {

    }

    @Test
    public void test__findWithQuery() throws Exception {

    }

    @Test
    public void test__findWithQuery1() throws Exception {

    }

    @Test
    public void test__downloadSynchronously() throws Exception {

    }

    @Test
    public void test__downloadSynchronously1() throws Exception {

    }

    @Test
    public void test__download() throws Exception {

    }

    @Test
    public void test__download1() throws Exception {

    }

    @Test
    public void test__download2() throws Exception {

    }

    @Test
    public void test__download3() throws Exception {

    }

    @Test
    public void test__query() throws Exception {

    }*/

}