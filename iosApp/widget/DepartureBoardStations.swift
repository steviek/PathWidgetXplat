//
//  DepartureBoardStations.swift
//  widgetExtension
//
//  Created by Steven Kideckel on 2024-06-16.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp
import Foundation

struct DepartureBoardStations: View {
    
    let entry: SimpleEntry
    let data: DepartureBoardData
    let width: CGFloat
    let height: CGFloat
    
    /// Gets the seasonal text color
    private var seasonalTextColor: Color {
        SeasonalUtils.getSeasonalTextColor(for: entry.date)
    }
    
    var body: some View {
        // We only show one station (the origin station)
        if data.stations.count > 0 {
            stationView(data.stations[0], width: width, height: height)
        }
    }
    
    @ViewBuilder
    private func stationView(
        _ station: DepartureBoardData.StationData,
        width: CGFloat,
        height: CGFloat
    ) -> some View {
        switch entry.configuration.trainGrouping {
        case .ungrouped:
            UngroupedStationView(
                entry: entry,
                station: station,
                width: width,
                height: height,
                textColor: seasonalTextColor
            )
        case .byHeadsign:
            GroupedStationView(
                entry: entry,
                station: station,
                width: width,
                height: height,
                textColor: seasonalTextColor
            )
        }
    }
}
