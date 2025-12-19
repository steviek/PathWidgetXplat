package com.sixbynine.transit.path.app.external

object JvmExternalRoutingManager : ExternalRoutingManager {
    override suspend fun openEmail(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun openUrl(url: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun shareTextToSystem(text: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun launchAppRating(): Boolean {
        TODO("Not yet implemented")
    }
}

actual fun ExternalRoutingManager(): ExternalRoutingManager = JvmExternalRoutingManager
