//
//  Interop.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-18.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import ComposeApp
import SwiftUI
import WidgetKit

extension Date {
    func toKotlinInstant() -> Kotlinx_datetimeInstant {
        Kotlinx_datetimeInstant.Companion().fromEpochMilliseconds(
            epochMilliseconds: Int64(self.timeIntervalSince1970 * 1000)
        )
    }
}

extension Kotlinx_datetimeInstant {
    func toDate() -> Date {
        Date(timeIntervalSince1970: Double(self.toEpochMilliseconds()) / 1000.0)
    }
}

extension Bool {
    func toKotlinBoolean() -> KotlinBoolean {
        KotlinBoolean(bool: self)
    }
}

extension KotlinBoolean {
    func toBool() -> Bool {
        self.boolValue
    }
}

extension ColorWrapper {
    func toColor(isDark: Bool) -> SwiftUI.Color {
        let adjusted = adjustForDarkMode(isDark: isDark)
        return SwiftUI.Color(
            red: Double(adjusted.red),
            green: Double(adjusted.green),
            blue: Double(adjusted.blue)
        )
    }
}
