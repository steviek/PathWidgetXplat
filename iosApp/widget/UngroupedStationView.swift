//
//  UngroupedStationView.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-16.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct UngroupedStationView: EntryView {
    
    let entry: SimpleEntry
    let station: DepartureBoardData.StationData
    let width: CGFloat
    let height: CGFloat
    
    var body: some View {
        let rowCountWith6Spacing = measureRowCount(initialHeight: height, rowSpacing: 6)
        let rowCountWith4Spacing = measureRowCount(initialHeight: height, rowSpacing: 4)
        let rowCount = max(rowCountWith4Spacing, rowCountWith6Spacing)
        let rowSpacing: CGFloat = rowCountWith4Spacing > rowCountWith6Spacing ? 4 : 6
        VStack(alignment: .leading, spacing: 0) {
            StationTitle(title: station.displayName, width: width, maxHeight: height)
                        
            let trains = station.trains
                .filter { train in !train.isPast(now: entry.date.toKotlinInstant())}
                .prefix(rowCount)
            
            if !trains.isEmpty {
                VStack(alignment: .center, spacing: 4) {
                    // First train - prominent display
                    let firstTrain = trains.first!
                    let firstTime = formatArrivalTime(firstTrain)
                    
                    Text(firstTime)
                        .font(Font.system(size: 24, weight: .bold))
                        .foregroundColor(.blue)
                        .multilineTextAlignment(.center)
                    
                    // Remaining trains - secondary line
                    if trains.count > 1 {
                        let remainingTrains = Array(trains.dropFirst())
                        let remainingTimes = remainingTrains.map { formatArrivalTime($0) }
                        let alsoText = entry.configuration.timeDisplay == .clock ? "also at " : "also in "
                        let timesString = alsoText + remainingTimes.joined(separator: ", ")
                        
                        Text(timesString)
                            .font(Font.system(size: 12))
                            .foregroundColor(.blue)
                            .multilineTextAlignment(.center)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            
            if (trains.isEmpty) {
                HStack {
                    Spacer()
                    Text(IosResourceProvider().getNoTrainsText())
                        .font(Font.system(size: 11))
                        .multilineTextAlignment(.center)
                    Spacer()
                }
            }
            
            Spacer()
        }
        .frame(width: width, height: height)
    }
    
    private func formatArrivalTime(_ train: DepartureBoardData.TrainData) -> String {
        var arrivalTime: String
        if (entry.configuration.timeDisplay == .clock) {
            arrivalTime = WidgetDataFormatter().formatTime(instant: train.projectedArrival)
            if (train.isBackfilled) {
                arrivalTime = "~" + arrivalTime
            }
        } else {
            arrivalTime = WidgetDataFormatter().formatRelativeTime(
                now: entry.date.toKotlinInstant(),
                time: train.projectedArrival
            )
        }
        
        return arrivalTime
    }
    
    private func measureRowCount(initialHeight: CGFloat, rowSpacing: CGFloat) -> Int {
        var height = initialHeight
        let headerHeight = measureTextHeight(text: "Updated", font: UIFont.systemFont(ofSize: 14, weight: .bold))
        height -= headerHeight
        
        let rowHeight = measureTextHeight(text: "To", font: UIFont.systemFont(ofSize: 12)) + rowSpacing
        return Int(floor(height / rowHeight))
    }
}


