package com.appiaries.baas.sdk;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class UserServiceTest extends InstrumentationTestCase {

    ABResult ret;
    ABUser user;
    List<ABUser> users;
    ABQuery query;

    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }
/*
    void sandbox() {

        try {
            //UserService
            ret = AB.UserService.saveSynchronously(user);
            AB.UserService.save(user);

            ret = AB.UserService.saveAllSynchronously(users);
            AB.UserService.saveAll(users);

            ret = AB.UserService.deleteSynchronously(user);
            AB.UserService.delete(user, new ResultCallback<Void>() {
                @Override
                public void done(ABResult<Void> result, ABException e) {
                    if (e == null) {

                    }
                }
            });

            ret = AB.UserService.deleteAllSynchronously(users);
            AB.UserService.deleteAll(users);

            ret = AB.UserService.deleteSynchronouslyWithQuery(query);
            AB.UserService.deleteWithQuery(query);

        } catch (ABException e) {
            e.printStackTrace();
        }
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

    /*@Test
    public void test__signUpSynchronously() throws Exception {

    }

    @Test
    public void test__signUpSynchronously1() throws Exception {

    }

    @Test
    public void test__signUp() throws Exception {

    }

    @Test
    public void test__signUp1() throws Exception {

    }

    @Test
    public void test__signUp2() throws Exception {

    }

    @Test
    public void test__logInSynchronously() throws Exception {

    }

    @Test
    public void test__logInSynchronously1() throws Exception {

    }

    @Test
    public void test__logIn() throws Exception {

    }

    @Test
    public void test__logIn1() throws Exception {

    }

    @Test
    public void test__logIn2() throws Exception {

    }

    @Test
    public void test__logInAsAnonymousSynchronously() throws Exception {

    }

    @Test
    public void test__logInAsAnonymousSynchronously1() throws Exception {

    }

    @Test
    public void test__logInAsAnonymous() throws Exception {

    }

    @Test
    public void test__logInAsAnonymous1() throws Exception {

    }

    @Test
    public void test__logInAsAnonymous2() throws Exception {

    }

    @Test
    public void test__logOutSynchronously() throws Exception {

    }

    @Test
    public void test__logOutSynchronously1() throws Exception {

    }

    @Test
    public void test__logOut() throws Exception {

    }

    @Test
    public void test__logOut1() throws Exception {

    }

    @Test
    public void test__logOut2() throws Exception {
    }

    @Test
    public void test__requestVerificationEmailSynchronously() throws Exception {

    }

    @Test
    public void test__requestVerificationEmailSynchronously1() throws Exception {

    }

    @Test
    public void test__requestVerificationEmail() throws Exception {

    }

    @Test
    public void test__requestVerificationEmail1() throws Exception {

    }

    @Test
    public void test__requestVerificationEmail2() throws Exception {

    }

    @Test
    public void test__resetPasswordSynchronously() throws Exception {

    }

    @Test
    public void test__resetPasswordSynchronously1() throws Exception {

    }

    @Test
    public void test__resetPassword() throws Exception {

    }

    @Test
    public void test__resetPassword1() throws Exception {

    }

    @Test
    public void test__resetPassword2() throws Exception {

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
    public void test__deleteSynchronouslyWithQuery1() throws Exception {

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

    }*/

}