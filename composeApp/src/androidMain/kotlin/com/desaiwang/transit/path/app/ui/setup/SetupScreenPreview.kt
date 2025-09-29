package com.desaiwang.transit.path.app.ui.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desaiwang.transit.path.PreviewTheme

@Preview
@Composable
fun SetupScreenPreview() {
    PreviewTheme {
        SetupScreen(SetupScreenContract.State(), {})
    }
}