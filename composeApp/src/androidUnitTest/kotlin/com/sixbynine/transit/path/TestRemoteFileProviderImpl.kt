package com.sixbynine.transit.path

import com.sixbynine.transit.path.util.TestRemoteFileProvider

object TestRemoteFileProviderImpl : TestRemoteFileProvider {
    override fun getText(url: String): Result<String> {
        val lastSlash = url.lastIndexOf('/')
        val path = url.substring(lastSlash + 1)
        return runCatching {
            TestRemoteFileProviderImpl::class.java.getResource(path)!!.readText()
        }
    }
}

fun TestRemoteFileProvider.Companion.install() {
    instance = TestRemoteFileProviderImpl
}
