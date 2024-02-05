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
            hasError: false
        )
    }
    
    func snapshot(
        for configuration: ConfigurationAppIntent,
        in context: Context
    ) async -> SimpleEntry {
        return await liveSnapshot(for: configuration, in: context)
    }
    
    func timeline(
        for configuration: ConfigurationAppIntent,
        in context: Context
    ) async -> Timeline<SimpleEntry> {
        var entries: [SimpleEntry] = []
        let entry = await liveSnapshot(for: configuration, in: context)
        entries.append(entry)
        return Timeline(entries: entries, policy: .atEnd)
    }
    
    private func liveSnapshot(
        for configuration: ConfigurationAppIntent,
        in context: Context
    ) async -> SimpleEntry {
        let limit =
        WidgetConfigurationUtils.getWidgetLimit(family: context.family)
        
        let effectiveConfiguration: ConfigurationAppIntent
        let widgetData: WidgetData?
        let hasError: Bool
        if (context.isPreview) {
            widgetData = Fixtures().widgetData(limit: Int32(limit))
            effectiveConfiguration = ConfigurationAppIntent(
                stations: [.jsq, .wtc]
            )
            hasError = false
        } else {
            let fetchResult = await WidgetDataFetcher().fetchWidgetDataAsync(
                limit: Int32(limit),
                stations: configuration.stations.map { $0.toStation()},
                filter: configuration.filter.toTrainFilter(),
                sort: configuration.sortOrder.toStationSort()
            )
            widgetData = fetchResult.data
            hasError = fetchResult.hasError
            effectiveConfiguration = configuration
        }
        
        return SimpleEntry(
            date: Date(),
            size: context.displaySize,
            configuration: effectiveConfiguration,
            data: widgetData,
            hasError: hasError
        )
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let size: CGSize
    let configuration: ConfigurationAppIntent
    let data: WidgetData?
    let hasError: Bool
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

