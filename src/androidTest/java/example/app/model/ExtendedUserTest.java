package example.app.model;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExtendedUserTest extends InstrumentationTestCase {

    ExtendedUser user;
    
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
        user = new ExtendedUser(); //NOTE: protected
        assertEquals(ExtendedUser.class, user.getClass());
        assertEquals(ExtendedUser.class.getName(), user.getCollectionID());
        assertEquals(0, user.getEstimatedData().size());
        assertEquals(0, user.getFilteredEstimatedData().size());
        assertEquals(0, user.getOriginalData().size());
        assertEquals(0, user.getAddedKeys().size());
        assertEquals(0, user.getAddedKeysAndValues().size());
        assertEquals(0, user.getUpdatedKeys().size());
        assertEquals(0, user.getUpdatedKeysAndValues().size());
        assertEquals(0, user.getRemovedKeys().size());
        assertEquals(0, user.getRemovedKeysAndValues().size());
        assertTrue(user.isNew());
        assertFalse(user.isDirty());
        assertNull(user.getID());
        assertNull(user.getCreated());
        assertNull(user.getCreatedBy());
        assertNull(user.getUpdated());
        assertNull(user.getUpdatedBy());
        assertNull(user.getLoginId());
        assertNull(user.getEmail());
        assertFalse(user.isEmailVerified());
        assertNull(user.getPassword());
        assertNull(user.getAuthData());
        assertNull(user.getState());
    }

    @Test
    public void test__constructor__collectionID() throws Exception {
        user = new ExtendedUser("MyUser");
        assertEquals(ExtendedUser.class, user.getClass());
        assertEquals("MyUser", user.getCollectionID()); //XXX: ExtendedUserはコレクションを持たないので常に com.appiaries.baas.sdk.ExtendedUser が返されるはずなんですが。。。
        assertEquals(0, user.getEstimatedData().size());
        assertEquals(0, user.getFilteredEstimatedData().size());
        assertEquals(0, user.getOriginalData().size());
        assertEquals(0, user.getAddedKeys().size());
        assertEquals(0, user.getAddedKeysAndValues().size());
        assertEquals(0, user.getUpdatedKeys().size());
        assertEquals(0, user.getUpdatedKeysAndValues().size());
        assertEquals(0, user.getRemovedKeys().size());
        assertEquals(0, user.getRemovedKeysAndValues().size());
        assertTrue(user.isNew());
        assertFalse(user.isDirty());
        assertNull(user.getID());
        assertNull(user.getCreated());
        assertNull(user.getCreatedBy());
        assertNull(user.getUpdated());
        assertNull(user.getUpdatedBy());
        assertNull(user.getLoginId());
        assertNull(user.getEmail());
        assertFalse(user.isEmailVerified());
        assertNull(user.getPassword());
        assertNull(user.getAuthData());
        assertNull(user.getState());
    }

    @Test
    public void test__constructor__collectionID__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedUser.Field.ID.getKey(), "8dce4f6cf9682027d889110426d");
                put(ExtendedUser.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedUser.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedUser.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedUser.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedUser.Field.LOGIN_ID.getKey(), "melissa");
                put(ExtendedUser.Field.EMAIL.getKey(), "melissa@example.com");
                put(ExtendedUser.Field.EMAIL_VERIFIED.getKey(), true);
                put(ExtendedUser.Field.PASSWORD.getKey(), "pw123456");
                put(ExtendedUser.Field.AUTH_DATA.getKey(), new HashMap<String, Object>());
                put(ExtendedUser.Field.STATE.getKey(), "locking");
                put("ageGroup",  10);
                put("gender",  "female");
                put("favorites", new HashMap<String, Object>(){{
                    put("brands", Arrays.asList("Appiaries", "BaaS"));
                }});
            }
        };
        user = new ExtendedUser("MyUser", map);
        assertEquals(ExtendedUser.class, user.getClass());
        assertEquals("MyUser", user.getCollectionID()); //XXX: ExtendedUserはコレクションを持たないので常に com.appiaries.baas.sdk.ExtendedUser が返されるはずなんですが。。。
        assertEquals(14, user.getEstimatedData().size());
        assertEquals(14, user.getFilteredEstimatedData().size());
        assertEquals(14, user.getOriginalData().size());
        assertEquals(0, user.getAddedKeys().size());
        assertEquals(0, user.getAddedKeysAndValues().size());
        assertEquals(0, user.getUpdatedKeys().size());
        assertEquals(0, user.getUpdatedKeysAndValues().size());
        assertEquals(0, user.getRemovedKeys().size());
        assertEquals(0, user.getRemovedKeysAndValues().size());
        assertTrue(user.isNew());
        assertFalse(user.isDirty());
        assertEquals("8dce4f6cf9682027d889110426d", user.getID());
        assertEquals(1436951840123L / 1000, user.getCreated().getTime());
        assertEquals("fooman", user.getCreatedBy());
        assertEquals(1436951850987L / 1000, user.getUpdated().getTime());
        assertEquals("barman", user.getUpdatedBy());
        assertEquals("melissa", user.getLoginId());
        assertEquals("melissa@example.com", user.getEmail());
        assertEquals(true, user.isEmailVerified());
        assertEquals("pw123456", user.getPassword());
        assertNotNull(user.getAuthData());
        assertEquals("locking", user.getState());
        assertEquals(10, user.get("ageGroup"));
        assertEquals("female", user.get("gender"));
        assertEquals(1, ((Map<String, Object>) user.get("favorites")).size());
    }

    @Test
    public void test__constructor__map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(){
            {
                put(ExtendedUser.Field.ID.getKey(), "8dce4f6cf9682027d889110426d");
                put(ExtendedUser.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ExtendedUser.Field.CREATED_BY.getKey(), "fooman");
                put(ExtendedUser.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ExtendedUser.Field.UPDATED_BY.getKey(), "barman");

                put(ExtendedUser.Field.LOGIN_ID.getKey(), "melissa");
                put(ExtendedUser.Field.EMAIL.getKey(), "melissa@example.com");
                put(ExtendedUser.Field.EMAIL_VERIFIED.getKey(), true);
                put(ExtendedUser.Field.PASSWORD.getKey(), "pw123456");
                put(ExtendedUser.Field.AUTH_DATA.getKey(), new HashMap<String, Object>());
                put(ExtendedUser.Field.STATE.getKey(), "locking");
                put("ageGroup",  10);
                put("gender",  "female");
                put("favorites", new HashMap<String, Object>(){{
                    put("brands", Arrays.asList("Appiaries", "BaaS"));
                }});
            }
        };
        user = new ExtendedUser(map);
        assertEquals(ExtendedUser.class, user.getClass());
        assertEquals(ExtendedUser.class.getName(), user.getCollectionID());
        assertEquals(14, user.getEstimatedData().size());
        assertEquals(14, user.getFilteredEstimatedData().size());
        assertEquals(14, user.getOriginalData().size());
        assertEquals(0, user.getAddedKeys().size());
        assertEquals(0, user.getAddedKeysAndValues().size());
        assertEquals(0, user.getUpdatedKeys().size());
        assertEquals(0, user.getUpdatedKeysAndValues().size());
        assertEquals(0, user.getRemovedKeys().size());
        assertEquals(0, user.getRemovedKeysAndValues().size());
        assertTrue(user.isNew());
        assertFalse(user.isDirty());
        assertEquals("8dce4f6cf9682027d889110426d", user.getID());
        assertEquals(1436951840123L / 1000, user.getCreated().getTime());
        assertEquals("fooman", user.getCreatedBy());
        assertEquals(1436951850987L / 1000, user.getUpdated().getTime());
        assertEquals("barman", user.getUpdatedBy());
        assertEquals("melissa", user.getLoginId());
        assertEquals("melissa@example.com", user.getEmail());
        assertEquals(true, user.isEmailVerified());
        assertEquals("pw123456", user.getPassword());
        assertNotNull(user.getAuthData());
        assertEquals("locking", user.getState());
        assertEquals(10, user.get("ageGroup"));
        assertEquals("female", user.get("gender"));
        assertEquals(1, ((Map<String, Object>)user.get("favorites")).size());
    }

    /*@Test
    public void testGetNickname() throws Exception {

    }*/

    /*@Test
    public void testSetNickname() throws Exception {

    }*/
}