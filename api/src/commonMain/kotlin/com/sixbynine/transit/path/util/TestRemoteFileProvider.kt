package com.desaiwang.transit.path.util

interface TestRemoteFileProvider {
    fun getText(url: String): Result<String>

    companion object {
        var instance: TestRemoteFileProvider? = null
    }
}
