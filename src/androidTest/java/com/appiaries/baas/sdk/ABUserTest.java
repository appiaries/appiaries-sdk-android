package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
public class ABUserTest extends InstrumentationTestCase {

    ABUser user;
    Object val;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }
/*
    void sandbox() {

        //User
        user = new ABUser();
        //>> access with keys
        val = user.get("_id");
        val = user.get("_cts");
        val = user.get("_cby");
        val = user.get("_uts");
        val = user.get("_uby");
        val = user.get("email");
        val = user.get("email_verified");
        val = user.get("password");
        val = user.get("authData");
        user.put("_id", "obj1");
        user.put("_cts", new Date());
        user.put("_cby", "foobar");
        user.put("_uts", new Date());
        user.put("_uby", "foobar");
        user.put("login_id", "foobar");
        user.put("email", "foobar@example.com");
        user.put("email_verified", true);
        user.put("password", "pw123456");
        user.put("authData", new HashMap<String, Object>());
        //>> access with fields
        val = user.get(ABUser.Field.ID);
        val = user.get(ABUser.Field.CREATED);
        val = user.get(ABUser.Field.CREATED_BY);
        val = user.get(ABUser.Field.UPDATED);
        val = user.get(ABUser.Field.UPDATED_BY);
        val = user.get(ABUser.Field.EMAIL);
        val = user.get(ABUser.Field.EMAIL_VERIFIED);
        val = user.get(ABUser.Field.PASSWORD);
        val = user.get(ABUser.Field.AUTH_DATA);
        user.put(ABUser.Field.ID, "obj1");
        user.put(ABUser.Field.CREATED, new Date());
        user.put(ABUser.Field.CREATED_BY, "foobar");
        user.put(ABUser.Field.UPDATED, new Date());
        user.put(ABUser.Field.UPDATED_BY, "foobar");
        user.put(ABUser.Field.LOGIN_ID, "foobar");
        user.put(ABUser.Field.EMAIL, "foobar@example.com");
        user.put(ABUser.Field.EMAIL_VERIFIED, true);
        user.put(ABUser.Field.PASSWORD, "pw123456");
        user.put(ABUser.Field.AUTH_DATA, new HashMap<String, Object>());
        //>> access via getter/setter
        val = user.getID();
        val = user.getCreated();
        val = user.getCreatedBy();
        val = user.getUpdated();
        val = user.getUpdatedBy();
        val = user.getLoginId();
        val = user.getEmail();
        val = user.isEmailVerified();
        val = user.getPassword();
        val = user.getAuthData();
        user.setID("obj1");
        user.setCreated(new Date());
        user.setCreatedBy("foobar");
        user.setUpdated(new Date());
        user.setUpdatedBy("foobar");
        user.setLoginId("foobar");
        user.setEmail("foobar@exampmle.com");
        user.setEmailVerified(true);
        user.setPassword("pw123456");
        user.setAuthData(new HashMap<String, Object>());
    }
*/
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
        user = new ABUser(); //NOTE: protected
        assertEquals(ABUser.class, user.getClass());
        assertEquals(ABUser.class.getName(), user.getCollectionID());
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
        user = new ABUser("MyUser");
        assertEquals(ABUser.class, user.getClass());
        assertEquals("MyUser", user.getCollectionID()); //XXX: ABUserはコレクションを持たないので常に com.appiaries.baas.sdk.ABUser が返されるはずなんですが。。。
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
                put(ABUser.Field.ID.getKey(), "8dce4f6cf9682027d889110426d");
                put(ABUser.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABUser.Field.CREATED_BY.getKey(), "fooman");
                put(ABUser.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABUser.Field.UPDATED_BY.getKey(), "barman");

                put(ABUser.Field.LOGIN_ID.getKey(), "melissa");
                put(ABUser.Field.EMAIL.getKey(), "melissa@example.com");
                put(ABUser.Field.EMAIL_VERIFIED.getKey(), true);
                put(ABUser.Field.PASSWORD.getKey(), "pw123456");
                put(ABUser.Field.AUTH_DATA.getKey(), new HashMap<String, Object>());
                put(ABUser.Field.STATE.getKey(), "locking");
                put("ageGroup",  10);
                put("gender",  "female");
                put("favorites", new HashMap<String, Object>(){{
                    put("brands", Arrays.asList("Appiaries", "BaaS"));
                }});
            }
        };
        user = new ABUser("MyUser", map);
        assertEquals(ABUser.class, user.getClass());
        assertEquals("MyUser", user.getCollectionID()); //XXX: ABUserはコレクションを持たないので常に com.appiaries.baas.sdk.ABUser が返されるはずなんですが。。。
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
                put(ABUser.Field.ID.getKey(), "8dce4f6cf9682027d889110426d");
                put(ABUser.Field.CREATED.getKey(), new Date(){{ setTime(1436951840123L / 1000); }}.getTime() * 1000);
                put(ABUser.Field.CREATED_BY.getKey(), "fooman");
                put(ABUser.Field.UPDATED.getKey(), new Date(){{ setTime(1436951850987L / 1000); }}.getTime() * 1000);
                put(ABUser.Field.UPDATED_BY.getKey(), "barman");

                put(ABUser.Field.LOGIN_ID.getKey(), "melissa");
                put(ABUser.Field.EMAIL.getKey(), "melissa@example.com");
                put(ABUser.Field.EMAIL_VERIFIED.getKey(), true);
                put(ABUser.Field.PASSWORD.getKey(), "pw123456");
                put(ABUser.Field.AUTH_DATA.getKey(), new HashMap<String, Object>());
                put(ABUser.Field.STATE.getKey(), "locking");
                put("ageGroup",  10);
                put("gender",  "female");
                put("favorites", new HashMap<String, Object>(){{
                    put("brands", Arrays.asList("Appiaries", "BaaS"));
                }});
            }
        };
        user = new ABUser(map);
        assertEquals(ABUser.class, user.getClass());
        assertEquals(ABUser.class.getName(), user.getCollectionID());
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
    public void test__refreshSynchronously() throws Exception {

    }

    @Test
    public void test__refreshSynchronously1() throws Exception {

    }

    @Test
    public void test__refresh() throws Exception {

    }

    @Test
    public void test__refresh1() throws Exception {

    }

    @Test
    public void test__refresh2() throws Exception {

    }

    @Test
    public void test__refresh3() throws Exception {

    }

    @Test
    public void test__query() throws Exception {

    }

    @Test
    public void test__keySet() throws Exception {

    }

    @Test
    public void test__getLoginId() throws Exception {

    }

    @Test
    public void test__setLoginId() throws Exception {

    }

    @Test
    public void test__getEmail() throws Exception {

    }

    @Test
    public void test__setEmail() throws Exception {

    }

    @Test
    public void test__isEmailVerified() throws Exception {

    }

    @Test
    public void test__setEmailVerified() throws Exception {

    }

    @Test
    public void test__getPassword() throws Exception {

    }

    @Test
    public void test__setPassword() throws Exception {

    }

    @Test
    public void test__getAuthData() throws Exception {

    }

    @Test
    public void test__setAuthData() throws Exception {

    }

    @Test
    public void test__getState() throws Exception {

    }

    @Test
    public void test__setState() throws Exception {

    }

    @Test
    public void test__clone() throws Exception {

    }*/

}