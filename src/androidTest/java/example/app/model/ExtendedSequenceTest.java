package example.app.model;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;

import com.appiaries.baas.sdk.ABQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExtendedSequenceTest extends InstrumentationTestCase {

    ExtendedSequence sequence;
    
    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test__constructor__default() throws Exception {
        sequence = new ExtendedSequence(); //NOTE: protected
        assertEquals(ExtendedSequence.class, sequence.getClass());
        assertEquals("ExtendedSequence", sequence.getCollectionID());
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
        sequence = new ExtendedSequence("MySequence");
        assertEquals(ExtendedSequence.class, sequence.getClass());
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
                put(ExtendedSequence.Field.ID.getKey(), "fdfccb76fedede56e9e494a31139b73f59c0Aed613596b3b8cec8f67da2b7a79");
                put(ExtendedSequence.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedSequence.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedSequence.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedSequence.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedSequence.Field.VALUE.getKey(), 100L);
                put(ExtendedSequence.Field.INITIAL_VALUE.getKey(), 1L);
            }
        };
        sequence = new ExtendedSequence("MySequence", map);
        assertEquals(ExtendedSequence.class, sequence.getClass());
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
                put(ExtendedSequence.Field.ID.getKey(), "fdfccb76fedede56e9e494a31139b73f59c0Aed613596b3b8cec8f67da2b7a79");
                put(ExtendedSequence.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedSequence.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedSequence.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedSequence.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedSequence.Field.VALUE.getKey(), 100L);
                put(ExtendedSequence.Field.INITIAL_VALUE.getKey(), 1L);
            }
        };
        sequence = new ExtendedSequence(map);
        assertEquals(ExtendedSequence.class, sequence.getClass());
        assertEquals("ExtendedSequence", sequence.getCollectionID()); //NOTE: ABDeviceはコレクションを持たないので常に com.appiaries.baas.sdk.ABDevice が返される。
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
    public void test__query() throws Exception {
        ABQuery query = ExtendedSequence.query();
        assertEquals("ExtendedSequence", query.getCollectionID());
        assertEquals("/ExtendedSequence/-", query.toString());
    }

}