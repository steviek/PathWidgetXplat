//
//  CommuteDepartureBoardView.swift
//  widgetExtension
//
//  Created by Assistant on 2024-12-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI
import ComposeApp
import CoreLocation

struct CommuteDepartureBoardView: View {
    let entry: CommuteProvider.Entry
    
    private func measureTextWidth(maxSize: CGSize, text: String, font: UIFont) -> CGFloat {
        return measureTextSize(maxSize: maxSize, text: text, font: font).width
    }
    
    private func measureTextSize(maxSize: CGSize, text: String, font: UIFont) -> CGRect {
        let attributes = [NSAttributedString.Key.font: font]
        let boundingRect = text.boundingRect(
            with: maxSize,
            options: [.usesLineFragmentOrigin, .usesFontLeading],
            attributes: attributes,
            context: nil
        )
        return boundingRect
    }
    
    var body: some View {
        if showEmptyView(entry) {
            CommuteEmptyView(entry: entry)
        } else {
            CommuteDepartureBoardContent(entry: entry)
        }
    }
    
    private func showEmptyView(_ entry: CommuteProvider.Entry) -> Bool {
        // Show empty view if origin is "closest" and we don't have location permission
        if entry.configuration.originStation == .closest {
            return !CLLocationManager().isAuthorizedForWidgetUpdates
        }
        return false
    }
}

struct CommuteDepartureBoardContent: View {
    let entry: CommuteProvider.Entry
    
    var body: some View {
        let seasonalBackground = SeasonalUtils.getSeasonalBackgroundName(for: entry.date)
        let textColor = SeasonalUtils.getSeasonalTextColor(for: entry.date)
        
        ZStack {
            // Seasonal background
            GeometryReader { geometry in
                Image(seasonalBackground)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .clipped()
                    .edgesIgnoringSafeArea(.all)
            }
            
            VStack(alignment: .leading, spacing: 0) {
                // Main content
                if let data = entry.data {
                    CommuteStationView(
                        station: data.stations.first!,
                        entry: entry,
                        textColor: textColor
                    )
                } else {
                    CommuteErrorView(entry: entry, textColor: textColor)
                }
                
                Spacer()
                
                // Footer
                CommuteFooterView(entry: entry, textColor: textColor)
            }
            .padding(12)
        }
        .frame(width: entry.size.width, height: entry.size.height)
    }
}

struct CommuteStationView: View {
    let station: DepartureBoardData.StationData
    let entry: CommuteProvider.Entry
    let textColor: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            CommuteStationTitle(
                title: station.displayName,
                destinationStation: entry.configuration.destinationStation.toStation()?.displayName,
                textColor: textColor
            )
            
            if !station.trains.isEmpty {
                CommuteTrainDisplay(
                    trains: Array(station.trains.prefix(6)),
                    entry: entry,
                    textColor: textColor
                )
            } else {
                CommuteNoTrainsView(textColor: textColor)
            }
        }
    }
}

struct CommuteStationTitle: View {
    let title: String
    let destinationStation: String?
    let textColor: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Station name
            Text(
                WidgetDataFormatter().formatHeadSign(
                    title: title,
                    fits: { text in
                        let textWidth = measureTextWidth(
                            maxSize: CGSize(width: 200, height: 50),
                            text: text,
                            font: UIFont.arimaStyleMedium(size: 14)
                        )
                        return (textWidth <= 200).toKotlinBoolean()
                    }
                )
            )
            .font(Font.arimaStyleMedium(size: 14))
            .foregroundColor(textColor)
            .italic()
            
            // Destination station
            if let destination = destinationStation {
                HStack(spacing: 2) {
                    Image(systemName: "arrow.right")
                        .font(.system(size: 12))
                        .foregroundColor(textColor)
                    Text(destination)
                        .font(Font.arimaStyle(size: 14))
                        .italic()
                        .foregroundColor(textColor)
                }
            }
        }
        .padding(.leading, 4)
        .padding(.top, 2)
    }
}

struct CommuteTrainDisplay: View {
    let trains: [DepartureBoardData.TrainData]
    let entry: CommuteProvider.Entry
    let textColor: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: -2) {
            // First train - prominent display
            let firstTrain = trains.first!
            let firstTime = formatArrivalTime(firstTrain)
            
            HStack(alignment: .firstTextBaseline, spacing: 0) {
                if entry.configuration.timeDisplay == .clock {
                    // For clock time, display as single text
                    Text(firstTime)
                        .font(Font.arimaStyleBold(size: 32))
                        .foregroundColor(textColor)
                } else {
                    // For relative time, split number and "min"
                    let (numberPart, minPart) = splitTimeString(firstTime)
                    Text(numberPart)
                        .font(Font.arimaStyleBold(size: 32))
                        .foregroundColor(textColor)
                    Text(minPart)
                        .font(Font.arimaStyleBold(size: 24))
                        .foregroundColor(textColor)
                }
            }
            
            // Remaining trains - secondary line
            if trains.count > 1 {
                let remainingTrains = Array(trains.dropFirst().prefix(5))
                let remainingTimes = remainingTrains.map { formatArrivalTime($0) }
                let alsoText = entry.configuration.timeDisplay == .clock ? "also at " : "also in "
                let timesString = formatRemainingTimes(remainingTimes: remainingTimes, alsoText: alsoText)
                
                Text(timesString)
                    .font(Font.arimaStyle(size: 12))
                    .foregroundColor(textColor)
                    .multilineTextAlignment(.leading)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: 200, alignment: .leading)
                    .padding(.leading, 2)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.leading, 4)
    }
    
    private func splitTimeString(_ timeString: String) -> (String, String) {
        // Handle cases like "5 min", "1 min", "~5 min", "now"
        if timeString == "now" {
            return ("now", "")
        }
        
        // Check if string contains "min"
        if let minRange = timeString.range(of: " min") {
            let numberPart = String(timeString[..<minRange.lowerBound])
            let minPart = String(timeString[minRange.lowerBound...])
            return (numberPart, minPart)
        }
        
        // If no "min" found, return the whole string as number part
        return (timeString, "")
    }
    
    private func formatArrivalTime(_ train: DepartureBoardData.TrainData) -> String {
        var arrivalTime: String
        if (entry.configuration.timeDisplay == .clock) {
            arrivalTime = WidgetDataFormatter().formatTime(instant: train.projectedArrival)
        } else {
            arrivalTime = WidgetDataFormatter().formatRelativeTime(
                now: entry.date.toKotlinInstant(),
                time: train.projectedArrival
            )
        }
        return arrivalTime
    }
    
    private func formatRemainingTimes(remainingTimes: [String], alsoText: String) -> String {
        if entry.configuration.timeDisplay == .clock {
            // For clock times, remove "~" from all except the first one
            let processedTimes = remainingTimes.enumerated().map { index, time in
                if index == 0 {
                    return time // Keep first time as-is (with or without ~)
                } else {
                    // Remove "~" from subsequent times
                    return time.hasPrefix("~") ? String(time.dropFirst()) : time
                }
            }
            return alsoText + processedTimes.joined(separator: ", ")
        } else {
            // For relative times, extract numbers and add "mins" at the end
            let timeNumbers = remainingTimes.enumerated().map { index, time in
                var processedTime = time
                // Remove "~" from all except the first one
                if index > 0 && processedTime.hasPrefix("~") {
                    processedTime = String(processedTime.dropFirst())
                }
                // Remove " mins" suffix if present
                if processedTime.hasSuffix(" min") {
                    return String(processedTime.dropLast(4))
                }
                return processedTime
            }
            return alsoText + timeNumbers.joined(separator: ", ") + " min"
        }
    }
}

struct CommuteNoTrainsView: View {
    let textColor: Color
    
    var body: some View {
        HStack {
            Spacer()
            Text(IosResourceProvider().getNoTrainsText())
                .font(Font.arimaStyle(size: 11))
                .foregroundColor(textColor)
                .multilineTextAlignment(.leading)
            Spacer()
        }
    }
}

struct CommuteErrorView: View {
    let entry: CommuteProvider.Entry
    let textColor: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(IosResourceProvider().getErrorLong())
                .font(Font.arimaStyleMedium(size: 14))
                .foregroundColor(textColor)
                .multilineTextAlignment(.leading)
        }
    }
}

struct CommuteEmptyView: View {
    let entry: CommuteProvider.Entry
    
    var body: some View {
        VStack {
            Text("Location access required")
                .font(Font.arimaStyleMedium(size: 14))
                .foregroundColor(.secondary)
            Text("Enable location access to show nearest station")
                .font(Font.arimaStyle(size: 12))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

struct CommuteFooterView: View {
    let entry: CommuteProvider.Entry
    let textColor: Color
    
    private func measureTextWidth(maxSize: CGSize, text: String, font: UIFont) -> CGFloat {
        let attributes = [NSAttributedString.Key.font: font]
        let boundingRect = text.boundingRect(
            with: maxSize,
            options: [.usesLineFragmentOrigin, .usesFontLeading],
            attributes: attributes,
            context: nil
        )
        return boundingRect.width
    }
    
    var body: some View {
        HStack(spacing: 0) {
            // Time text grouped with refresh button (only if enabled)
            if entry.configuration.showLastRefreshedTime {
                Text(getFooterText())
                    .font(Font.arimaStyle(size: 12))
                    .italic()
                    .foregroundColor(textColor)
                    .padding(.trailing, 4)

                Button(intent: RefreshIntent()) {
                    Image(systemName: "arrow.2.circlepath")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 14, height: 14)
                        .foregroundColor(textColor)
                }
                .padding(.horizontal, 2)
                .buttonStyle(.borderless)
            } else{
                Button(intent: RefreshIntent()) {
                    Image(systemName: "arrow.2.circlepath")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 16, height: 16)
                        .foregroundColor(textColor)
                }
                .padding(.horizontal, 2)
                .buttonStyle(.borderless)
            }


        }
        .frame(maxWidth: .infinity, alignment: .trailing)
    }
    
    private func getFooterText() -> String {
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
