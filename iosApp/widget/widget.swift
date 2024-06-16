//
//  widget.swift
//  widget
//
//  Created by Steven Kideckel on 2023-11-27.
//  Copyright © 2023 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI
import ComposeApp

struct Provider: AppIntentTimelineProvider {
    
    func placeholder(in context: Context) -> SimpleEntry {
        return SimpleEntry(
            date: Date(),
            size: context.displaySize,
            configuration: ConfigurationAppIntent(stations: [.jsq]),
            data: nil,
            hasError: false,
            dataFrom: Date()
        )
    }
    
    func snapshot(
        for configuration: ConfigurationAppIntent,
        in context: Context
    ) async -> SimpleEntry {
        return await createEntries(
            for: configuration,
            in: context,
            count: 1
        ).first!
    }
    
    func timeline(
        for configuration: ConfigurationAppIntent,
        in context: Context
    ) async -> Timeline<SimpleEntry> {
        let entries = await createEntries(
            for: configuration,
            in: context,
            count: configuration.timeDisplay == .relative ? 16 : 1
        )
        let refreshTime = Date().addingTimeInterval(10 * 60)
        return Timeline(entries: entries, policy: .after(refreshTime))
    }
    
    private func createEntries(
        for configuration: ConfigurationAppIntent,
        in context: Context,
        count: Int
    ) async -> [SimpleEntry] {
        let stationLimit =
        WidgetConfigurationUtils.getWidgetLimit(family: context.family)
        
        let effectiveConfiguration: ConfigurationAppIntent
        var widgetData: WidgetData?
        let hasError: Bool
        if (context.isPreview) {
            widgetData = Fixtures().widgetData(limit: Int32(stationLimit))
            effectiveConfiguration = ConfigurationAppIntent(
                stations: [.jsq, .wtc]
            )
            hasError = false
        } else {
            let fetchResult = await WidgetDataFetcher().fetchWidgetDataAsync(
                stationLimit: Int32(stationLimit),
                stations: configuration.stations.map { $0.toStation()},
                lines: configuration.lines.map { $0.toLine() },
                filter: configuration.filter.toTrainFilter(),
                sort: configuration.sortOrder.toStationSort()
            )
            widgetData = fetchResult.data
            hasError = fetchResult.hasError
            effectiveConfiguration = configuration
        }
        
        let now = Date()
        
        var entries: [SimpleEntry] = []
        var date = now
        
        for _ in 0..<count {
            widgetData = WidgetDataFetcher().prunePassedDepartures(
                data: widgetData,
                time: date.toKotlinInstant()
            )
            entries.append(
                SimpleEntry(
                    date: date,
                    size: context.displaySize,
                    configuration: effectiveConfiguration,
                    data: widgetData,
                    hasError: hasError,
                    dataFrom: now
                )
            )
            date = TimeUtilities().getStartOfNextMinute(time: date.toKotlinInstant()).toDate()
        }
        
        return entries
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let size: CGSize
    let configuration: ConfigurationAppIntent
    let data: WidgetData?
    let hasError: Bool
    let dataFrom: Date
}

struct widget: Widget {
    let kind: String = "widget"
    
    var body: some WidgetConfiguration {
        AppIntentConfiguration(
            kind: kind,
            intent: ConfigurationAppIntent.self,
            provider: Provider()
        ) { entry in
            ZStack {
                if (entry.configuration.stations.isEmpty) {
                    EmptyDepartureBoardView()
                } else {
                    DepartureBoardView(entry: entry)
                }
            }
            .containerBackground(.fill.tertiary, for: .widget)
        }
    }
}

