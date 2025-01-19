package com.sixbynine.transit.path.test

import com.sixbynine.transit.path.util.IsTest
import com.sixbynine.transit.path.util.TestRemoteFileProvider

object TestSetupHelper {
    fun setUp() {
        IsTest = true
        TestRemoteFileProvider.install()
        TestPreferences.install()
    }
}
