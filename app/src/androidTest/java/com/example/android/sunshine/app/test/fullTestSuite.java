package com.example.android.sunshine.app.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by Naledi Madlopha on 2016/11/03.
 * TODO: Add a class header comment!
 */

public class fullTestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(TestSuite.class)
                .includeAllPackagesUnderHere().build();
    }
}
