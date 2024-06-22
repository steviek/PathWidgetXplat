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
    let station: WidgetData.StationData
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
            VStack(alignment: .leading, spacing: 0) {
                ForEach(trains, id: \.id) { train in
                    Spacer().frame(height: rowSpacing)
                    HStack(alignment: .center, spacing: 0) {
                        let destination = WidgetDataFormatter().formatHeadSign(title: train.title, width: HeadSignWidth.narrow)
                        ColorCircle(size: 12, colors: train.colors)
                        Spacer().frame(width: 4)
                        Text(destination)
                            .font(Font.system(size: 12))
                            .lineLimit(1)
                        
                        let arrivalTime = formatArrivalTime(train)
                        
                        Spacer()
                        Text(arrivalTime)
                            .font(Font.system(size: 12)
                            .monospacedDigit())
                            .lineLimit(1)
                    }
                }
                
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
    
    private func formatArrivalTime(_ train: WidgetData.TrainData) -> String {
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


