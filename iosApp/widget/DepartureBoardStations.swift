//
//  DepartureBoardStations.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-16.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct DepartureBoardStations: View {
    
    let entry: SimpleEntry
    let data: WidgetData
    let width: CGFloat
    let height: CGFloat
    
    var body: some View {
        let tileWidth = data.stations.count > 1 ? (width / 2) - 8 : width
        let tileHeight = data.stations.count > 2 ? (height / 2) - 8 : height
        let pad = data.stations.count > 1
        HStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 0) {
                if (data.stations.count > 0) {
                    stationView(data.stations[0], width: tileWidth, height: tileHeight, pad: pad)
                }
                
                if (data.stations.count > 2) {
                    Spacer().frame(height: 16)
                    stationView(data.stations[2], width: tileWidth, height: tileHeight, pad: pad)
                }
            }
            .frame(width: tileWidth, height: height)
            
            if (data.stations.count > 1) {
                Spacer().frame(width: 16)
                VStack(alignment: .leading, spacing: 0) {
                    if (data.stations.count > 1) {
                        stationView(data.stations[1], width: tileWidth, height: tileHeight, pad: pad)
                    }
                    
                    if (data.stations.count > 3) {
                        Spacer().frame(height: 16)
                        stationView(data.stations[3], width: tileWidth, height: tileHeight, pad: pad)
                    } else if (data.stations.count > 2) {
                        Spacer()
                    }
                }
                .frame(width: tileWidth, height: height)
            }
        }
        .frame(width: width, height: height)
    }
    
    @ViewBuilder
    private func stationView(
        _ station: WidgetData.StationData,
        width: CGFloat,
        height: CGFloat,
        pad: Bool
    ) -> some View {
        if (entry.configuration.trainGrouping == .ungrouped) {
            UngroupedStationView(
                entry: entry,
                station: station,
                width: width,
                height: height,
                pad: pad
            )
        } else {
            UngroupedStationView(
                entry: entry,
                station: station,
                width: width,
                height: height,
                pad: pad
            )
        }
    }
}
