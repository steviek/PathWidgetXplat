//
//  EntryView.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

protocol EntryView : View {
    var entry: DepartureBoardWidgetEntry { get }
}

extension EntryView {
    func measureTextHeight(text: String, font: UIFont) -> CGFloat {
        measureTextSize(text: text, font: font).height
    }
    
    func measureTextWidth(text: String, font: UIFont) -> CGFloat {
        measureTextSize(text: text, font: font).width
    }
    
    func measureTextSize(text: String, font: UIFont) -> CGRect {
        measureTextSize(maxSize: entry.size, text: text, font: font)
    }
    
    func formatTrainTime(_ time: KotlinInstant) -> String {
        if (entry.configuration.timeDisplay == .clock) {
            return WidgetDataFormatter().formatTime(instant: time)
        } else {
            return WidgetDataFormatter().formatRelativeTime(
                now: entry.date.toKotlinInstant(),
                time: time
            )
        }
    }
}
