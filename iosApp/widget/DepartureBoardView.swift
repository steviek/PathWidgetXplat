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
    
    private func measureRowCount(initialHeight: CGFloat, pad: Bool, rowSpacing: CGFloat) -> Int {
        var height = initialHeight
        let headerHeight = measureTextHeight(text: "Updated", font: UIFont.systemFont(ofSize: 14, weight: .bold))
        height -= headerHeight
        
        let rowHeight = measureTextHeight(text: "To", font: UIFont.systemFont(ofSize: 12)) + rowSpacing
        return Int(floor(height / rowHeight))
    }
    
    private func measureTextHeight(text: String, font: UIFont) -> CGFloat {
        measureTextSize(text: text, font: font).height
    }
    
    private func measureTextWidth(text: String, font: UIFont) -> CGFloat {
        measureTextSize(text: text, font: font).width
    }
    
    private func measureTextSize(text: String, font: UIFont) -> CGRect {
        text.boundingRect(
            with: entry.size,
            options: [.usesLineFragmentOrigin, .usesFontLeading, .truncatesLastVisibleLine],
            attributes: [.font: font],
            context: nil
        )
    }
    
    var body: some View {
        return ZStack {
            VStack(spacing: 0) {
                if let data = entry.data {
                    let footerHeight = max(
                        measureTextHeight(
                            text: "Updated",
                            font: UIFont.systemFont(ofSize: 12)
                        ),
                        32
                    )
                    let innerWidth = entry.size.width - 32
                    let innerHeight = entry.size.height - 20 - footerHeight
                    let tileWidth = data.stations.count > 1 ? (innerWidth / 2) - 8 : innerWidth
                    let tileHeight = data.stations.count > 2 ? (innerHeight / 2) - 8 : innerHeight
                    let pad = data.stations.count > 1
                    HStack(spacing: 0) {
                        VStack(alignment: .leading, spacing: 0) {
                            if (data.stations.count > 0) {
                                stationViewByTrain(data.stations[0], width: tileWidth, height: tileHeight, pad: pad)
                            }
                            
                            if (data.stations.count > 2) {
                                Spacer().frame(height: 16)
                                stationViewByTrain(data.stations[2], width: tileWidth, height: tileHeight, pad: pad)
                            }
                        }
                        .frame(width: tileWidth, height: innerHeight)
                        
                        if (data.stations.count > 1) {
                            Spacer().frame(width: 16)
                            VStack(alignment: .leading, spacing: 0) {
                                if (data.stations.count > 1) {
                                    stationViewByTrain(data.stations[1], width: tileWidth, height: tileHeight, pad: pad)
                                }
                                
                                if (data.stations.count > 3) {
                                    Spacer().frame(height: 16)
                                    stationViewByTrain(data.stations[3], width: tileWidth, height: tileHeight, pad: pad)
                                } else if (data.stations.count > 2) {
                                    Spacer()
                                }
                            }
                            .frame(width: tileWidth, height: innerHeight)
                        }
                    }
                    .frame(width: innerWidth, height: innerHeight)
                    
                    HStack(alignment: .center, spacing: 0) {
                        Button(intent: RefreshIntent()) {
                            Image(systemName: "arrow.2.circlepath")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 24, height: 24)
                        }
                        .padding(4)
                        .buttonStyle(.borderless)
                        .hidden()
                        
                        Spacer()
                        
                        Text(getFooterText())
                            .font(Font.system(size: 12))
                        
                        Spacer()
                            
                        Button(intent: RefreshIntent()) {
                            Image(systemName: "arrow.triangle.2.circlepath")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 24, height: 24)
                        }
                        .padding(4)
                        .buttonStyle(.borderless)
                    }
                }
            }
        }
        .padding([.horizontal], 12)
        .padding([.bottom], 4)
        .padding([.top], 16)
        .frame(width: entry.size.width, height: entry.size.height, alignment: .topLeading)
    }
    
    @ViewBuilder
    private func stationViewByTrain(
        _ station: WidgetData.StationData,
        width: CGFloat,
        height: CGFloat,
        pad: Bool
    ) -> some View {
        let rowCountWith6Spacing = measureRowCount(initialHeight: height, pad: pad, rowSpacing: 6)
        let rowCountWith4Spacing = measureRowCount(initialHeight: height, pad: pad, rowSpacing: 4)
        let rowCount = max(rowCountWith4Spacing, rowCountWith6Spacing)
        let rowSpacing: CGFloat = rowCountWith4Spacing > rowCountWith6Spacing ? 4 : 6
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Spacer()
                Text(
                    WidgetDataFormatter().formatHeadSign(
                        title: station.displayName,
                        fits: {
                            let titleSpace = width - (pad ? 16 : 0) - 16
                            let textWidth = measureTextWidth(text: $0, font: UIFont.systemFont(ofSize: 14, weight: .bold))
                            return (textWidth <= titleSpace).toKotlinBoolean()
                        }
                    )
                )
                .multilineTextAlignment(.center)
                .font(Font.system(size: 14))
                .fontWeight(.bold)
                Spacer()
            }
                        
            let trains = station.trains
                .filter { train in !train.isPast(now: entry.date.toKotlinInstant())}
                .prefix(rowCount)
            VStack(alignment: .leading, spacing: 0) {
                ForEach(trains, id: \.id) { train in
                    Spacer().frame(height: rowSpacing)
                    HStack(alignment: .center, spacing: 0) {
                        let destination = WidgetDataFormatter().formatHeadSign(title: train.title, width: HeadSignWidth.narrow)
                        colorCircle(size: 12, colors: train.colors, isDark: colorScheme == .dark)
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
            .padding([.horizontal], pad ? 8 : 0)
            
            Spacer()
        }
        .frame(width: width, height: height)
    }
    
    @ViewBuilder
    private func colorCircle(size: CGFloat, colors: [ColorWrapper], isDark: Bool) -> some View {
        let color1 = colors.first?.toColor(isDark: isDark) ?? Color.black
        let color2 = colors.count > 1 ? colors[1].toColor(isDark: isDark) : color1
        ZStack {
            Circle()
                .fill(color1)
                .frame(width: size, height: size)
            
            SemiCircle()
                .fill(color2)
                .rotationEffect(.degrees(90))
                .frame(width: size, height: size)
        }
        .overlay(isDark ? Circle().stroke(Color.white, lineWidth: 1) : nil)
    }
    
    
    private struct SemiCircle: Shape {
        func path(in rect: CGRect) -> Path {
            var path = Path()
            
            path.move(to: CGPoint(x: rect.minX, y: rect.midY))
            path.addArc(center: CGPoint(x: rect.midX, y: rect.midY), radius: rect.width / 2, startAngle: .degrees(0), endAngle: .degrees(180), clockwise: true)
            path.closeSubpath()
            
            return path
        }
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
    
    private func getFooterText() -> String {
        let maxUpdatedWidth = entry.size.width - 64 - 24 - 8
        let formattedFetchTime = WidgetDataFormatter().formatTime(instant: entry.dataFrom.toKotlinInstant())
        
        if entry.hasError {
            return if footerTextFits(IosResourceProvider().getErrorLong()) {
                IosResourceProvider().getErrorLong()
            } else {
                IosResourceProvider().getErrorShort()
            }
        }
        
        if entry.configuration.timeDisplay == .clock {
            let longText = IosResourceProvider().getUpdatedAtTime(formattedFetchTime: formattedFetchTime)
            
            return if footerTextFits(longText) {
                longText
            } else {
                formattedFetchTime
            }
        }
        
        let displayTime = WidgetDataFormatter().formatTime(instant: entry.date.toKotlinInstant())
        let fullText = IosResourceProvider().getFullRelativeUpdatedAtTime(
            displayTime: displayTime,
            dataTime: formattedFetchTime
        )
        if footerTextFits(fullText) {
            return fullText
        }
        
        let shorterText = IosResourceProvider().getShorterRelativeUpdatedAtTime(
            displayTime: displayTime,
            dataTime: formattedFetchTime
        )
        if footerTextFits(shorterText) {
            return shorterText
        }
        
        return displayTime
    }
    
    private func footerTextFits(_ text: String) -> Bool {
        let maxWidth = entry.size.width - 64 - 24 - 8
        return measureTextWidth(text: text, font: UIFont.systemFont(ofSize: 12)) <= maxWidth
        
    }
}
