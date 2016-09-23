package com.example.android.forecast;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class FullTestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }

    public FullTestSuite () {
        super();
    }

//    @Test
//    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
//    }


}
