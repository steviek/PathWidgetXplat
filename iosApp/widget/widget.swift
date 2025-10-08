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
import CoreLocation

struct Provider: AppIntentTimelineProvider {

    func placeholder(in context: Context) -> SimpleEntry {
        let entry = SimpleEntry(
            date: Date(),
            size: context.displaySize,
            configuration: ConfigurationAppIntent(originStation: .jsq, destinationStation: .wtc),
            data: nil,
            hasError: false,
            hasPathError: false,
            hasGlobalPathAlerts: false,
            dataFrom: Date()
        )
        return entry
    }

    func snapshot(
        for configuration: ConfigurationAppIntent,
        in context: Context
    ) async -> SimpleEntry {
        let entry = await createEntries(
            for: configuration,
            in: context,
            count: 1
        ).first!
        return entry
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
        var widgetData: DepartureBoardData?
        let hasError: Bool
        let hasPathError: Bool
        if (context.isPreview) {
            widgetData = Fixtures().widgetData(limit: Int32(stationLimit))
            effectiveConfiguration = ConfigurationAppIntent(
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
                filter: configuration.filter.toTrainFilter(),
                sort: configuration.sortOrder.toStationSort(),
                lines: configuration.lines.map {
                    $0.toLine()
                }
            )
            widgetData = fetchResult.data
            hasError = fetchResult.hasError
            hasPathError = fetchResult.hasPathError
            
            // Update effective configuration with the actual stations used
            effectiveConfiguration = ConfigurationAppIntent(
                originStation: effectiveOrigin,
                destinationStation: effectiveDestination,
                timeDisplay: configuration.timeDisplay,
                autoReverse: configuration.autoReverse,
                reverseStartHour: configuration.reverseStartHour,
                reverseEndHour: configuration.reverseEndHour
            )
        }

        let now = Date()

        var entries: [SimpleEntry] = []
        var date = now

        for _ in 0..<count {
            if (!context.isPreview) {
                widgetData = WidgetDataFetcher().prunePassedDepartures(
                    data: widgetData,
                    time: date.toKotlinInstant()
                )
            }
            entries.append(
                SimpleEntry(
                    date: date,
                    size: context.displaySize,
                    configuration: effectiveConfiguration,
                    data: widgetData,
                    hasError: hasError,
                    hasPathError: hasPathError,
                    hasGlobalPathAlerts: (widgetData?.globalAlerts.count ?? 0) > 0,
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
    let data: DepartureBoardData?
    let hasError: Bool
    let hasPathError: Bool
    let hasGlobalPathAlerts: Bool
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
                if showEmptyView(entry) {
                    EmptyDepartureBoardView(isError: false, isPathError: false)
                } else if entry.hasError, entry.data?.stations.isEmpty != false {
                    EmptyDepartureBoardView(isError: true, isPathError: entry.hasPathError)
                } else {
                    DepartureBoardView(entry: entry)
                }
            }
            .containerBackground(.fill.tertiary, for: .widget)
        }
        .supportedFamilies([.systemSmall])
    }

    private func showEmptyView(_ entry: SimpleEntry) -> Bool {
        // Show empty view if origin is "closest" and we don't have location permission
        if entry.configuration.originStation == .closest {
            return !CLLocationManager().isAuthorizedForWidgetUpdates
        }
        return false
    }
}

