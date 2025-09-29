package com.desaiwang.transit.path.test

import com.desaiwang.transit.path.util.IsTest
import com.desaiwang.transit.path.util.TestRemoteFileProvider

object TestSetupHelper {
    fun setUp() {
        IsTest = true
        TestRemoteFileProvider.install()
        TestPreferences.install()
    }
}
