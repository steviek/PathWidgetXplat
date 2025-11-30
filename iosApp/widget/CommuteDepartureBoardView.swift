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
            // Seasonal background sized directly from the widget context
            Image(seasonalBackground)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: entry.size.width, height: entry.size.height)
                .clipped()
                .edgesIgnoringSafeArea(.all)
                        
            VStack(alignment: .leading, spacing: 0) {
                // Main content
                if let data = entry.data, let station = data.stations.first {
                    CommuteStationView(
                        station: station,
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
                destinationStation: entry.configuration.destinationStation.getCommuteWidgetDestinationName(),
                textColor: textColor,
                maxWidth: entry.size.width
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
    let maxWidth: CGFloat
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Station name
            Text(
                WidgetDataFormatter().formatHeadSign(
                    title: title,
                    fits: { text in
                        // Use the widget's available width (or smaller) instead of a hardcoded value
                        let availableWidth = maxWidth
                        let textWidth = measureTextWidth(
                            maxSize: CGSize(width: availableWidth, height: 50),
                            text: text,
                            font: UIFont.arimaStyleMediumItalic(size: 14)
                        )
                        return (textWidth <= availableWidth).toKotlinBoolean()
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
                    // For relative time, show condensed format with "min" suffix
                    let condensedTime = formatCondensedArrivalTime(firstTrain)
                    let minSuffix = getMinSuffix(firstTrain)
                    Text(condensedTime)
                        .font(Font.arimaStyleBold(size: 32))
                        .foregroundColor(textColor)
                    Text(minSuffix)
                        .font(Font.arimaStyleBold(size: 24))
                        .foregroundColor(textColor)
                }
            }
            
            // Remaining trains - secondary line
            if trains.count > 1 {
                let remainingTrains = Array(trains.dropFirst().prefix(5))
                let remainingArrivals = remainingTrains.map { $0.projectedArrival }
                let timesString = GroupedWidgetLayoutHelper.Companion().joinAdditionalTimes(
                    timeDisplay: entry.configuration.timeDisplay.toKotlinTimeDisplay(),
                    times: remainingArrivals,
                    displayAt: entry.date.toKotlinInstant()
                )
                
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
    
    private func formatArrivalTime(_ train: DepartureBoardData.TrainData) -> String {
        if entry.configuration.timeDisplay == .clock {
            return WidgetDataFormatter().formatTime(instant: train.projectedArrival)
        } else {
            return WidgetDataFormatter().formatRelativeTime(
                now: entry.date.toKotlinInstant(),
                time: train.projectedArrival
            )
        }
    }
    
    private func formatCondensedArrivalTime(_ train: DepartureBoardData.TrainData) -> String {
        return WidgetDataFormatter().formatCondensedRelativeTime(
            now: entry.date.toKotlinInstant(),
            time: train.projectedArrival
        )
    }
    
    private func getMinSuffix(_ train: DepartureBoardData.TrainData) -> String {
        let minutes = WidgetDataFormatter().getMinutesBetween(
            now: entry.date.toKotlinInstant(),
            time: train.projectedArrival
        )
        // Only show " min" suffix if under an hour (and not "due")
        if minutes >= 1 && minutes < 60 {
            return " min"
        }
        return ""
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
            Text("Open the app, go to Settings, and enable 'Closest station' to grant permission.")
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
    
    private var buttonSize: CGFloat {
        entry.configuration.showLastRefreshedTime ? 14 : 16
    }
    
    var body: some View {
        HStack(spacing: 0) {
            // Time text grouped with refresh button (only if enabled)
            if entry.configuration.showLastRefreshedTime {
                Text(getFooterText(
                    dataFrom: entry.dataFrom,
                    displayDate: entry.date,
                    hasError: entry.hasError,
                    timeDisplay: entry.configuration.timeDisplay,
                    footerTextFits: footerTextFits
                ))
                    .font(Font.arimaStyle(size: 12))
                    .italic()
                    .foregroundColor(textColor)
                    .padding(.trailing, 4)
            }
        
            Button(intent: RefreshIntent()) {
                Image(systemName: "arrow.2.circlepath")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: buttonSize, height: buttonSize)
                    .foregroundColor(textColor)
            }
            .padding(.horizontal, 2)
            .buttonStyle(.borderless)

        }
        .frame(maxWidth: .infinity, alignment: .trailing)
    }
    
    /// Checks if the footer text fits within the available space
    /// Accounts for the refresh button, destination station, and padding
    private func footerTextFits(_ text: String) -> Bool {
        // Calculate available width: total width - refresh button (24) - destination station text - padding (16)
        let destinationStationWidth = measureTextWidth(maxSize: entry.size, text: getDestinationStationName(), font: UIFont.arimaStyleItalic(size: 12))
        let maxWidth = entry.size.width - (24 + 16 + 8 + destinationStationWidth) // 8 for spacing between refresh and time
        
        return measureTextWidth(maxSize: entry.size, text: text, font: UIFont.arimaStyleItalic(size: 12)) <= maxWidth
    }

    /// Gets the display name for the destination station from the configuration
    private func getDestinationStationName() -> String {
        return entry.configuration.destinationStation.getCommuteWidgetDestinationName()
    }
}
