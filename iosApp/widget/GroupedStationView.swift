//
//  GroupedStationView.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct GroupedStationView: EntryView {
    
    let entry: SimpleEntry
    let station: DepartureBoardData.StationData
    let width: CGFloat
    let height: CGFloat
    let textColor: Color
    
    var body: some View {
        let layoutInfo = GroupedWidgetLayoutHelper(
            station: station,
            displayAt: entry.date.toKotlinInstant(),
            timeDisplay: entry.configuration.timeDisplay.toKotlinTimeDisplay(),
            width: width,
            height: height,
            measure: { text, font in
                let weight: UIFont.Weight = font.isBold ? .bold : .regular
                let uiFont = if font.isMonospacedDigit {
                    UIFont.monospacedSystemFont(ofSize: font.size, weight: weight)
                } else {
                    UIFont.systemFont(ofSize: font.size, weight: weight)
                }
                let size = measureTextSize(text: text, font: uiFont)
                return SizeWrapper(width: size.width, height: size.height)
            }
        ).computeLayoutInfo()
        
        VStack(alignment: .leading, spacing: 0) {
            StationTitle(
                title: station.displayName, 
                destinationStation: entry.configuration.destinationStation.toStation()?.displayName,
                width: width, 
                maxHeight: height,
                textColor: textColor
            )
            Spacer().frame(height: layoutInfo.spacingBelowTitle)
            
            if (layoutInfo.signs.isEmpty) {
                HStack {
                    Spacer()
                    Text(IosResourceProvider().getNoTrainsText())
                        .font(Font.system(size: 11))
                        .multilineTextAlignment(.center)
                    Spacer()
                }
            }
            
            ForEach(layoutInfo.signs.indices, id : \.self) { index in
                if (index > 0) {
                    Spacer().frame(height: layoutInfo.spacingBelowTitle)
                }

                let sign = layoutInfo.signs[index]
                VStack {
                    HStack {
                        ColorCircle(size: 12, colors: sign.colors)
                        
                        Spacer().frame(width: 4)
                        
                        Text(sign.title)
                            .font(Font.system(size: 12))
                            .fontWeight(.bold)
                            .lineLimit(1)
                        
                        Spacer().frame(width: 4)
                        Spacer()
                        Text(sign.nextArrival)
                            .monospacedDigit()
                            .font(Font.system(size: 12))
                            .fontWeight(.bold)
                            .lineLimit(1)
                        
                    }
                    
                    Spacer().frame(height: layoutInfo.spacingBelowHeadSign)
                    
                    if (layoutInfo.lineCountBelowTitle > 0) {
                        HStack {
                            Text(sign.subtext ?? "")
                                .font(Font.system(size: 11))
                                .monospacedDigit()
                                .lineLimit(Int(layoutInfo.lineCountBelowTitle))
                            Spacer()
                        }
                    }
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
