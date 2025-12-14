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

struct CommuteSimpleEntry: TimelineEntry {
    let date: Date
    let size: CGSize
    let configuration: CommuteConfigurationAppIntent
    let data: DepartureBoardData?
    let hasError: Bool
    let hasPathError: Bool
    let dataFrom: Date
}

struct CommuteProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> CommuteSimpleEntry {
        let entry = CommuteSimpleEntry(
            date: Date(),
            size: context.displaySize,
            configuration: CommuteConfigurationAppIntent(originStation: .exp, destinationStation: .wtc, showLastRefreshedTime: true),
            data: Fixtures().widgetData(limit: 1),
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
            data: Fixtures().widgetData(limit: 1),
            hasError: false,
            hasPathError: false,
            dataFrom: Date()
        )
        return entry
    }
    
    func timeline(for configuration: CommuteConfigurationAppIntent, in context: Context) async -> Timeline<CommuteSimpleEntry> {
        let entries = await createEntries(
            for: configuration,
            in: context,
            count: configuration.timeDisplay == .relative ? 16 : 1
        )
        let refreshTime = Date().addingTimeInterval(10 * 60)
        return Timeline(entries: entries, policy: .after(refreshTime))
    }
    
    private func createEntries(
        for configuration: CommuteConfigurationAppIntent,
        in context: Context,
        count: Int
    ) async -> [CommuteSimpleEntry] {
        let stationLimit = 1 // We only want to show one station for commute widget
        
        let effectiveConfiguration: CommuteConfigurationAppIntent
        var widgetData: DepartureBoardData?
        let hasError: Bool
        let hasPathError: Bool
        let dataFrom: Date
        
        if (context.isPreview) {
            widgetData = Fixtures().widgetData(limit: Int32(stationLimit))
            effectiveConfiguration = CommuteConfigurationAppIntent(
                originStation: .exp,
                destinationStation: .wtc,
                showLastRefreshedTime: true
            )
            hasError = false
            hasPathError = false
            dataFrom = Date()
        } else {
            // Apply auto-reverse logic if enabled
            let effectiveOrigin = configuration.getEffectiveOrigin()
            let effectiveDestination = configuration.getEffectiveDestination()
            
            let fetchResult = await WidgetDataFetcher().fetchWidgetDataAsyncCommute(
                originStation: effectiveOrigin,
                destinationStation: effectiveDestination,
                filter: Filter.all.toTrainFilter(),
                lines: LineChoice.allCases.map { $0.toLine() }
            )
            widgetData = fetchResult.data
            hasError = fetchResult.hasError
            hasPathError = fetchResult.hasPathError
            dataFrom = widgetData?.fetchTime.toDate() ?? Date()
            
            // Update effective configuration with the actual stations used
            // autoReverse doesn't need to be set here because it's handled by the shouldReverseStations() function
            effectiveConfiguration = CommuteConfigurationAppIntent(
                originStation: effectiveOrigin,
                destinationStation: effectiveDestination,
                timeDisplay: configuration.timeDisplay,
                showLastRefreshedTime: configuration.showLastRefreshedTime,
                useSeasonalBackgrounds: configuration.useSeasonalBackgrounds,
                reverseDays: configuration.reverseDays,
                startHour: configuration.startHour,
                endHour: configuration.endHour
            )
        }

        let now = Date()
        var entries: [CommuteSimpleEntry] = []
        var date = now

        for _ in 0..<count {
            if (!context.isPreview) {
                widgetData = WidgetDataFetcher().prunePassedDepartures(
                    data: widgetData,
                    time: date.toKotlinInstant()
                )
            }
            entries.append(
                CommuteSimpleEntry(
                    date: date,
                    size: context.displaySize,
                    configuration: effectiveConfiguration,
                    data: widgetData,
                    hasError: hasError,
                    hasPathError: hasPathError,
                    dataFrom: dataFrom
                )
            )
            date = TimeUtilities().getStartOfNextMinute(time: date.toKotlinInstant()).toDate()
        }

        return entries
    }
}

struct CommuteWidget: Widget {
    let kind: String = "SingleStationCommuteWidget"

    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: kind, intent: CommuteConfigurationAppIntent.self, provider: CommuteProvider()) { entry in
            CommuteDepartureBoardView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("My Commute")
        .description("Route based widget with reverse for commutes home and seasonal themes")
        .supportedFamilies([.systemSmall])
    }
}
