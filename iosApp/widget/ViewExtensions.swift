//
//  ViewExtensions.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-16.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

extension View {
    func measureTextHeight(maxSize: CGSize, text: String, font: UIFont) -> CGFloat {
        measureTextSize(maxSize: maxSize, text: text, font: font).height
    }
    
    func measureTextWidth(maxSize: CGSize, text: String, font: UIFont) -> CGFloat {
        measureTextSize(maxSize: maxSize, text: text, font: font).width
    }
    
    func measureTextSize(maxSize: CGSize, text: String, font: UIFont) -> CGRect {
        text.boundingRect(
            with: maxSize,
            options: [.usesLineFragmentOrigin, .usesFontLeading, .truncatesLastVisibleLine],
            attributes: [.font: font],
            context: nil
        )
    }
    
    /// Generates footer text for widget display based on time display mode and available space.
    ///
    /// Priority order:
    /// 1. Show error messages if there's an error state
    /// 2. Handle clock time display mode (shows actual clock times)
    /// 3. Handle relative time display mode (shows "X minutes ago" style)
    ///
    /// - Parameters:
    ///   - dataFrom: The date when the data was fetched
    ///   - displayDate: The date the widget is displaying for
    ///   - hasError: Whether there's an error state to display
    ///   - timeDisplay: The time display mode (clock or relative)
    ///   - footerTextFits: Closure to check if text fits in available space
    /// - Returns: The appropriate footer text based on space and display mode
    func getFooterText(
        dataFrom: Date,
        displayDate: Date,
        hasError: Bool,
        timeDisplay: TimeDisplay,
        footerTextFits: (String) -> Bool
    ) -> String {
        let formattedFetchTime = WidgetDataFormatter().formatTime(instant: dataFrom.toKotlinInstant())

        // Priority 1: Show error messages if there's an error state
        if hasError {
            return if footerTextFits(IosResourceProvider().getErrorLong()) {
                // Use the full error message if it fits
                IosResourceProvider().getErrorLong()
            } else {
                // Fall back to shorter error message if space is limited
                IosResourceProvider().getErrorShort()
            }
        }

        // Priority 2: Handle clock time display mode (shows actual clock times)
        if timeDisplay == .clock {
            let longText = IosResourceProvider().getUpdatedAtTime(formattedFetchTime: formattedFetchTime)

            return if footerTextFits(longText) {
                // Show "Updated at [time]" if it fits
                longText
            } else {
                // Just show the time if space is limited
                formattedFetchTime
            }
        }

        // Priority 3: Handle relative time display mode (shows "X minutes ago" style)
        // Format the current display time (when the widget is showing data for)
        let displayTime = WidgetDataFormatter().formatTime(instant: displayDate.toKotlinInstant())
        
        // Try the full relative time text first (e.g., "Updated at 2:30 PM, data from 2:25 PM")
        let fullText = IosResourceProvider().getFullRelativeUpdatedAtTime(
            displayTime: displayTime,
            dataTime: formattedFetchTime
        )
        if footerTextFits(fullText) {
            return fullText
        }

        // Try a shorter version of the relative time text
        let shorterText = IosResourceProvider().getShorterRelativeUpdatedAtTime(
            displayTime: displayTime,
            dataTime: formattedFetchTime
        )
        if footerTextFits(shorterText) {
            return shorterText
        }

        // Final fallback: just show the display time
        return displayTime
    }
}
