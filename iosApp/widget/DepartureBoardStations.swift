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
        let columnWidth = data.stations.count > 1 ? (width / 2) - 8 : width
        
        // If we have multiple columns, then pad the sides of the station tiles.
        let pad = data.stations.count > 1
        let tileWidth = columnWidth - (pad ? 16 : 0)
        let tileHeight = data.stations.count > 2 ? (height / 2) - 8 : height
        HStack(spacing: 0) {
            if (pad) {
                Spacer().frame(width: 8)
            }
            
            VStack(alignment: .leading, spacing: 0) {
                if (data.stations.count > 0) {
                    stationView(data.stations[0], width: tileWidth, height: tileHeight)
                }
                
                if (data.stations.count > 2) {
                    Spacer().frame(height: 16)
                    stationView(data.stations[2], width: tileWidth, height: tileHeight)
                }
            }
            .frame(width: tileWidth, height: height)
            
            if (data.stations.count > 1) {
                Spacer().frame(width: 32)
                VStack(alignment: .leading, spacing: 0) {
                    if (data.stations.count > 1) {
                        stationView(data.stations[1], width: tileWidth, height: tileHeight)
                    }
                    
                    if (data.stations.count > 3) {
                        Spacer().frame(height: 16)
                        stationView(data.stations[3], width: tileWidth, height: tileHeight)
                    } else if (data.stations.count > 2) {
                        Spacer()
                    }
                }
                .frame(width: tileWidth, height: height)
            }
            
            if (pad) {
                Spacer().frame(width: 8)
            }
        }
        .frame(width: width, height: height)
    }
    
    @ViewBuilder
    private func stationView(
        _ station: WidgetData.StationData,
        width: CGFloat,
        height: CGFloat
    ) -> some View {
        switch entry.configuration.trainGrouping {
        case .ungrouped:
            UngroupedStationView(
                entry: entry,
                station: station,
                width: width,
                height: height
            )
        case .byHeadsign:
            GroupedStationView(
                entry: entry,
                station: station,
                width: width,
                height: height
            )
        }
    }
}
