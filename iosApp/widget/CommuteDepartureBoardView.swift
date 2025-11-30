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
        if showSameStationError(entry) {
            CommuteSameStationView(entry: entry)
        } else if showEmptyView(entry) {
            CommuteEmptyView(entry: entry)
        } else {
            CommuteDepartureBoardContent(entry: entry)
        }
    }
    
    private func showSameStationError(_ entry: CommuteProvider.Entry) -> Bool {
        let origin = entry.configuration.getEffectiveOrigin()
        let destination = entry.configuration.getEffectiveDestination()
        
        if origin == destination {
            return true
        }
        
        if let data = entry.data, let station = data.stations.first {
            if let fixed = destination.toSharedStationChoice() as? StationChoiceFixed {
                return fixed.station.pathApiName == station.id
            }
        }
        
        return false
    }

    private func showEmptyView(_ entry: CommuteProvider.Entry) -> Bool {
        // Show empty view if origin is "closest" and we don't have location permission
        if entry.configuration.originStation == .closest {
            return !CLLocationManager().isAuthorizedForWidgetUpdates
        }
        return false
    }
    
    fileprivate enum TextStyles {
        enum Station {
            static let titleSize: CGFloat = 14
            static let title = Font.arimaStyleMedium(size: titleSize)
            // Italicized for measurement to match view modifier
            static let titleMeasurement = UIFont.arimaStyleMediumItalic(size: titleSize)
            
            static let destinationSize: CGFloat = 14
            static let destination = Font.arimaStyle(size: destinationSize)
        }
        
        enum Train {
            static let timeLarge = Font.arimaStyleBold(size: 32)
            static let timeMedium = Font.arimaStyleBold(size: 24)
            static let info = Font.arimaStyle(size: 12)
        }
        
        enum Footer {
            static let textSize: CGFloat = 12
            static let text = Font.arimaStyle(size: textSize)
            // Italicized for measurement to match view modifier
            static let textMeasurement = UIFont.arimaStyleItalic(size: textSize)
        }
        
        enum Message {
            static let title = Font.arimaStyleMedium(size: 14)
            static let body = Font.arimaStyle(size: 12)
            static let tiny = Font.arimaStyle(size: 11)
        }
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
                            font: CommuteDepartureBoardView.TextStyles.Station.titleMeasurement
                        )
                        return (textWidth <= availableWidth).toKotlinBoolean()
                    }
                )
            )
            .font(CommuteDepartureBoardView.TextStyles.Station.title)
            .foregroundColor(textColor)
            .italic()
            
            // Destination station
            if let destination = destinationStation {
                HStack(spacing: 2) {
                    Image(systemName: "arrow.right")
                        .font(.system(size: 12))
                        .foregroundColor(textColor)
                    Text(destination)
                        .font(CommuteDepartureBoardView.TextStyles.Station.destination)
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
    let trains: [DepartureBoardData.TrainData]?
    let entry: CommuteProvider.Entry
    let textColor: Color
    let errorMessage: String?
    
    init(
        trains: [DepartureBoardData.TrainData]? = nil,
        entry: CommuteProvider.Entry,
        textColor: Color,
        errorMessage: String? = nil
    ) {
        self.trains = trains
        self.entry = entry
        self.textColor = textColor
        self.errorMessage = errorMessage
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: -2) {
            if let trains = trains, let firstTrain = trains.first {
                // Train times display
                let firstTime = formatArrivalTime(firstTrain)
                
                HStack(alignment: .firstTextBaseline, spacing: 0) {
                    if entry.configuration.timeDisplay == .clock {
                        // For clock time, display as single text
                        Text(firstTime)
                            .font(CommuteDepartureBoardView.TextStyles.Train.timeLarge)
                            .foregroundColor(textColor)
                    } else {
                        // For relative time, show condensed format with "min" suffix
                        let condensedTime = formatCondensedArrivalTime(firstTrain)
                        let minSuffix = getMinSuffix(firstTrain)
                        Text(condensedTime)
                            .font(CommuteDepartureBoardView.TextStyles.Train.timeLarge)
                            .foregroundColor(textColor)
                        Text(minSuffix)
                            .font(CommuteDepartureBoardView.TextStyles.Train.timeMedium)
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
                        .font(CommuteDepartureBoardView.TextStyles.Train.info)
                        .foregroundColor(textColor)
                        .multilineTextAlignment(.leading)
                        .fixedSize(horizontal: false, vertical: true)
                        .frame(maxWidth: 200, alignment: .leading)
                        .padding(.leading, 2)
                }
            } else if let errorMessage = errorMessage {
                // Error message display
                VStack(alignment: .leading, spacing: 4) {
                    Text(errorMessage)
                        .font(CommuteDepartureBoardView.TextStyles.Message.body)
                        .foregroundColor(textColor)
                        .multilineTextAlignment(.leading)
                }
                .padding(.top, 4)
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
                .font(CommuteDepartureBoardView.TextStyles.Message.tiny)
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
                .font(CommuteDepartureBoardView.TextStyles.Message.title)
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
                .font(CommuteDepartureBoardView.TextStyles.Message.title)
                .foregroundColor(.secondary)
            Text("Open the app settings and enable 'Closest station' to grant permission.")
                .font(CommuteDepartureBoardView.TextStyles.Message.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

struct CommuteSameStationView: View {
    let entry: CommuteProvider.Entry
    
    var body: some View {
        let seasonalBackground = SeasonalUtils.getSeasonalBackgroundName(for: entry.date)
        let textColor = SeasonalUtils.getSeasonalTextColor(for: entry.date)
        let originName = entry.configuration.getEffectiveOrigin().getCommuteWidgetDestinationName()
        let destinationName = entry.configuration.getEffectiveDestination().getCommuteWidgetDestinationName()
        
        ZStack {
            // Seasonal background sized directly from the widget context
            Image(seasonalBackground)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: entry.size.width, height: entry.size.height)
                .clipped()
                .edgesIgnoringSafeArea(.all)
            
            VStack(alignment: .leading, spacing: 0) {
                // Main content - show station titles and error message
                VStack(alignment: .leading, spacing: 4) {
                    CommuteStationTitle(
                        title: originName,
                        destinationStation: destinationName,
                        textColor: textColor,
                        maxWidth: entry.size.width
                    )
                    
                    // Error message instead of train times
                    CommuteTrainDisplay(
                        entry: entry,
                        textColor: textColor,
                        errorMessage: "Origin and destination stations cannot be the same."
                    )
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
                    .font(CommuteDepartureBoardView.TextStyles.Footer.text)
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
        let destinationStationWidth = measureTextWidth(maxSize: entry.size, text: getDestinationStationName(), font: CommuteDepartureBoardView.TextStyles.Footer.textMeasurement)
        let maxWidth = entry.size.width - (24 + 16 + 8 + destinationStationWidth) // 8 for spacing between refresh and time
        
        return measureTextWidth(maxSize: entry.size, text: text, font: CommuteDepartureBoardView.TextStyles.Footer.textMeasurement) <= maxWidth
    }

    /// Gets the display name for the destination station from the configuration
    private func getDestinationStationName() -> String {
        return entry.configuration.destinationStation.getCommuteWidgetDestinationName()
    }
}
