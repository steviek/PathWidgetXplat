package com.desaiwang.transit.path.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val ApplicationScope = CoroutineScope(Dispatchers.Main)
val IoScope = CoroutineScope(Dispatchers.IO)
val BackgroundScope = CoroutineScope(Dispatchers.Default)