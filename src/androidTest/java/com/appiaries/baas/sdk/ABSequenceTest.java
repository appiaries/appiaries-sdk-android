package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ABSequenceTest extends InstrumentationTestCase {

    ABSequence sequence;

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
    public void test__constructor__default() throws Exception {
        sequence = new ABSequence(); //NOTE: protected
        assertEquals(ABSequence.class, sequence.getClass());
        assertEquals(ABSequence.class.getName(), sequence.getCollectionID());
        assertEquals(0, sequence.getEstimatedData().size());
        assertEquals(0, sequence.getFilteredEstimatedData().size());
        assertEquals(0, sequence.getOriginalData().size());
        assertEquals(0, sequence.getAddedKeys().size());
        assertEquals(0, sequence.getAddedKeysAndValues().size());
        assertEquals(0, sequence.getUpdatedKeys().size());
        assertEquals(0, sequence.getUpdatedKeysAndValues().size());
        assertEquals(0, sequence.getRemovedKeys().size());
        assertEquals(0, sequence.getRemovedKeysAndValues().size());
        assertTrue(sequence.isNew());
        assertFalse(sequence.isDirty());
        assertNull(sequence.getID());
        assertNull(sequence.getCreated());
        assertNull(sequence.getCreatedBy());
        assertNull(sequence.getUpdated());
        assertNull(sequence.getUpdatedBy());
    }

    @Test
    public void test__constructor__collectionID() throws Exception {
        sequence = new ABSequence("MySequence");
        assertEquals(ABSequence.class, sequence.getClass());
        assertEquals("MySequence", sequence.getCollectionID()); //XXX: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返されるはずなんですが。。。
        assertEquals(0, sequence.getEstimatedData().size());
        assertEquals(0, sequence.getFilteredEstimatedData().size());
        assertEquals(0, sequence.getOriginalData().size());
        assertEquals(0, sequence.getAddedKeys().size());
        assertEquals(0, sequence.getAddedKeysAndValues().size());
        assertEquals(0, sequence.getUpdatedKeys().size());
        assertEquals(0, sequence.getUpdatedKeysAndValues().size());
        assertEquals(0, sequence.getRemovedKeys().size());
        assertEquals(0, sequence.getRemovedKeysAndValues().size());
        assertTrue(sequence.isNew());
        assertFalse(sequence.isDirty());
        assertNull(sequence.getID());
        assertNull(sequence.getCreated());
        assertNull(sequence.getCreatedBy());
        assertNull(sequence.getUpdated());
        assertNull(sequence.getUpdatedBy());
    }

    @Test
    public void test__constructor__collectionID__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ABSequence.Field.ID.getKey(), "fdfccb76fedede56e9e494a31139b73f59c0Aed613596b3b8cec8f67da2b7a79");
                put(ABFile.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.CREATED_BY.getKey(), "fooman");
                put(ABFile.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.UPDATED_BY.getKey(), "barman");

                put(ABSequence.Field.VALUE.getKey(), 100L);
                put(ABSequence.Field.INITIAL_VALUE.getKey(), 1L);
            }
        };
        sequence = new ABSequence("MySequence", map);
        assertEquals(ABSequence.class, sequence.getClass());
        assertEquals("MySequence", sequence.getCollectionID()); //XXX: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返されるはずなんですが。。。
        assertEquals(7, sequence.getEstimatedData().size());
        assertEquals(7, sequence.getFilteredEstimatedData().size());
        assertEquals(7, sequence.getOriginalData().size());
        assertEquals(0, sequence.getAddedKeys().size());
        assertEquals(0, sequence.getAddedKeysAndValues().size());
        assertEquals(0, sequence.getUpdatedKeys().size());
        assertEquals(0, sequence.getUpdatedKeysAndValues().size());
        assertEquals(0, sequence.getRemovedKeys().size());
        assertEquals(0, sequence.getRemovedKeysAndValues().size());
        assertTrue(sequence.isNew());
        assertFalse(sequence.isDirty());
        assertEquals("fdfccb76fedede56e9e494a31139b73f59c0Aed613596b3b8cec8f67da2b7a79", sequence.getID());
        assertEquals(1436951840123L / 1000, sequence.getCreated().getTime());
        assertEquals("fooman", sequence.getCreatedBy());
        assertEquals(1436951850987L / 1000, sequence.getUpdated().getTime());
        assertEquals("barman", sequence.getUpdatedBy());
        assertEquals(100L, sequence.getValue());
        assertEquals(1L, sequence.getInitialValue());
    }

    @Test
    public void test__constructor__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ABSequence.Field.ID.getKey(), "fdfccb76fedede56e9e494a31139b73f59c0Aed613596b3b8cec8f67da2b7a79");
                put(ABFile.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.CREATED_BY.getKey(), "fooman");
                put(ABFile.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABFile.Field.UPDATED_BY.getKey(), "barman");

                put(ABSequence.Field.VALUE.getKey(), 100L);
                put(ABSequence.Field.INITIAL_VALUE.getKey(), 1L);
            }
        };
        sequence = new ABSequence(map);
        assertEquals(ABSequence.class, sequence.getClass());
        assertEquals(ABSequence.class.getName(), sequence.getCollectionID()); //NOTE: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返される。
        assertEquals(7, sequence.getEstimatedData().size());
        assertEquals(7, sequence.getFilteredEstimatedData().size());
        assertEquals(7, sequence.getOriginalData().size());
        assertEquals(0, sequence.getAddedKeys().size());
        assertEquals(0, sequence.getAddedKeysAndValues().size());
        assertEquals(0, sequence.getUpdatedKeys().size());
        assertEquals(0, sequence.getUpdatedKeysAndValues().size());
        assertEquals(0, sequence.getRemovedKeys().size());
        assertEquals(0, sequence.getRemovedKeysAndValues().size());
        assertTrue(sequence.isNew());
        assertFalse(sequence.isDirty());
        assertEquals("fdfccb76fedede56e9e494a31139b73f59c0Aed613596b3b8cec8f67da2b7a79", sequence.getID());
        assertEquals(1436951840123L / 1000, sequence.getCreated().getTime());
        assertEquals("fooman", sequence.getCreatedBy());
        assertEquals(1436951850987L / 1000, sequence.getUpdated().getTime());
        assertEquals("barman", sequence.getUpdatedBy());
        assertEquals(100L, sequence.getValue());
        assertEquals(1L, sequence.getInitialValue());
    }

    /*@Test
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
    public void test__fetch2() throws Exception {

    }

    @Test
    public void test__fetch3() throws Exception {

    }

    @Test
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