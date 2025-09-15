//
//  SwiftUIView.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-18.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct DepartureBoardView: View {

    @Environment(\.colorScheme) var colorScheme

    let entry: SimpleEntry

    var body: some View {
        return ZStack {
            // Background image
            GeometryReader { geometry in
                Image("WidgetBackground")
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .clipped()
                    .edgesIgnoringSafeArea(.all)
            }
            
            // Semi-transparent overlay for better readability
            //Color.black.opacity(0.3)
            
            VStack(spacing: 0) {
                if let data = entry.data {
                    let footerHeight = max(
                        measureTextHeight(
                            maxSize: entry.size,
                            text: "Updated",
                            font: UIFont.systemFont(ofSize: 12)
                        ),
                        32
                    )
                    let innerWidth = entry.size.width - 16
                    let innerHeight = entry.size.height - 16 - footerHeight

                    DepartureBoardStations(
                        entry: entry,
                        data: data,
                        width: innerWidth,
                        height: innerHeight
                    )
                        .frame(width: innerWidth, height: innerHeight)

                    ZStack(alignment: .bottom) {
                        // Left side: Group refresh button with time text
                        HStack(spacing: 8) {
                            if (entry.hasGlobalPathAlerts) {
                                // Show the error indicator if there are alerts. Clicking anywhere will open the app, which is fine.
                                let isDark = colorScheme == .dark
                                ZStack {
                                    Image(systemName: "exclamationmark.triangle.fill")
                                        .resizable()
                                        .foregroundStyle(isDark ? .red : .orange)
                                        .aspectRatio(contentMode: .fit)
                                        .frame(width: 20, height: 20)
                                        .padding(4)
                                }
                                .padding(4)
                                
                            } else {
                                Button(intent: RefreshIntent()) {
                                    Image(systemName: "arrow.2.circlepath")
                                        .resizable()
                                        .aspectRatio(contentMode: .fit)
                                        .frame(width: 16, height: 16)
                                        .foregroundColor(.white)
                                }
                                .padding(2)
                                .buttonStyle(.borderless)
                            }
                            
                            // Time text grouped with refresh button
                            Text(getFooterText())
                                .font(Font.system(size: 12))
                                .foregroundColor(.white)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)

                        // Right side: Destination station - always aligned to the right edge
                        Text(getDestinationStationName())
                            .font(Font.system(size: 12, weight: .medium))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                }
            }
        }
        .frame(width: entry.size.width, height: entry.size.height)
    }

    /// Generates the footer text that appears at the bottom of the widget
    /// The text shows either error messages or time information based on the widget state
    /// and user preferences. It uses a progressive fallback system to fit within available space.
    private func getFooterText() -> String {
        // Format the time when the data was last fetched from the API
        let formattedFetchTime = WidgetDataFormatter().formatTime(instant: entry.dataFrom.toKotlinInstant())

        // Priority 1: Show error messages if there's an error state
        if entry.hasError {
            return if footerTextFits(IosResourceProvider().getErrorLong()) {
                // Use the full error message if it fits
                IosResourceProvider().getErrorLong()
            } else {
                // Fall back to shorter error message if space is limited
                IosResourceProvider().getErrorShort()
            }
        }

        // Priority 2: Handle clock time display mode (shows actual clock times)
        if entry.configuration.timeDisplay == .clock {
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
        let displayTime = WidgetDataFormatter().formatTime(instant: entry.date.toKotlinInstant())
        
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

    /// Gets the display name for the destination station from the configuration
    private func getDestinationStationName() -> String {
        return entry.configuration.destinationStation.toStation()?.displayName ?? "Unknown"
    }

    /// Checks if the footer text fits within the available space
    /// Accounts for the refresh button, destination station, and padding
    private func footerTextFits(_ text: String) -> Bool {
        // Calculate available width: total width - refresh button (24) - destination station text - padding (16)
        let destinationStationWidth = measureTextWidth(maxSize: entry.size, text: getDestinationStationName(), font: UIFont.systemFont(ofSize: 12))
        let maxWidth = entry.size.width - (24 + 16 + 8 + destinationStationWidth) // 8 for spacing between refresh and time
        return measureTextWidth(maxSize: entry.size, text: text, font: UIFont.systemFont(ofSize: 12)) <= maxWidth
    }
}
