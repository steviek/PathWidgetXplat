//
//  CommuteWidget.swift
//  widgetExtension
//
//  Created by Assistant on 2024-12-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI
import ComposeApp

struct CommuteProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> CommuteSimpleEntry {
        let entry = CommuteSimpleEntry(
            date: Date(),
            size: context.displaySize,
            configuration: CommuteConfigurationAppIntent(originStation: .jsq, destinationStation: .wtc),
            data: nil,
            hasError: false,
            hasPathError: false,
            dataFrom: Date()
        )
        return entry
    }

    func snapshot(for configuration: CommuteConfigurationAppIntent, in context: Context) async -> CommuteSimpleEntry {
        let entry = CommuteSimpleEntry(
            date: Date(),
            size: context.displaySize,
            configuration: configuration,
            data: nil,
            hasError: false,
            hasPathError: false,
            dataFrom: Date()
        )
        return entry
    }
    
    func timeline(for configuration: CommuteConfigurationAppIntent, in context: Context) async -> Timeline<CommuteSimpleEntry> {
        let stationLimit = 1 // We only want to show one station for commute widget
        
        let widgetData: DepartureBoardData?
        let hasError: Bool
        let hasPathError: Bool
        let effectiveConfiguration: CommuteConfigurationAppIntent
        
        if (context.isPreview) {
            widgetData = Fixtures().widgetData(limit: Int32(stationLimit))
            effectiveConfiguration = CommuteConfigurationAppIntent(
                originStation: .jsq,
                destinationStation: .wtc
            )
            hasError = false
            hasPathError = false
        } else {
            // Apply auto-reverse logic if enabled
            let effectiveOrigin = configuration.getEffectiveOrigin()
            let effectiveDestination = configuration.getEffectiveDestination()
            
            let fetchResult = await WidgetDataFetcher().fetchWidgetDataAsync(
                originStation: effectiveOrigin,
                destinationStation: effectiveDestination,
                filter: Filter.all.toTrainFilter(),
                sort: SortOrder.alphabetical.toStationSort(),
                lines: LineChoice.allCases.map { $0.toLine() }
            )
            widgetData = fetchResult.data
            hasError = fetchResult.hasError
            hasPathError = fetchResult.hasPathError
            
            // Update effective configuration with the actual stations used
            effectiveConfiguration = CommuteConfigurationAppIntent(
                originStation: effectiveOrigin,
                destinationStation: effectiveDestination,
                timeDisplay: configuration.timeDisplay,
                autoReverse: configuration.autoReverse,
                reverseStartHour: configuration.reverseStartHour,
                reverseEndHour: configuration.reverseEndHour
            )
        }

        let now = Date()
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 1, to: now)!
        
        let entry = CommuteSimpleEntry(
            date: now,
            size: context.displaySize,
            configuration: effectiveConfiguration,
            data: widgetData,
            hasError: hasError,
            hasPathError: hasPathError,
            dataFrom: widgetData?.fetchTime.toDate() ?? now
        )

        return Timeline(entries: [entry], policy: .after(nextUpdate))
    }
}

struct CommuteWidget: Widget {
    let kind: String = "CommuteWidget"

    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: kind, intent: CommuteConfigurationAppIntent.self, provider: CommuteProvider()) { entry in
            CommuteDepartureBoardView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Commute Tracker")
        .description("Commute-focused PATH widget with auto-reverse")
        .supportedFamilies([.systemSmall])
    }
}
