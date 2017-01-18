package com.peeterst.android.hourtracker;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 9/06/12
 * Time: 15:40
 * Tests
 */
public class AllTests extends TestSuite {
    public static Test suite(){
        TestSuiteBuilder builder = new TestSuiteBuilder(AllTests.class);
        builder.includePackages("com.peeterst.android.data","data");
        System.err.println(builder.toString());

        return builder.build();
    }
}
