//
//  ConfigurationAppIntent.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-18.
//  Copyright © 2023 orgName. All rights reserved.
//

import AppIntents
import WidgetKit
import SwiftUI
import ComposeApp

enum StationChoice : String, AppEnum {
    case closest, exp, grove, harrison, hoboken, jsq, newark, newport, christopher, ninth, fourteenth, twentyThird, thirtyThird, wtc
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Station Choice"
    
    static var caseDisplayRepresentations: [StationChoice : DisplayRepresentation] = [
        .closest: "Closest station (requires location access)",
        .exp: "Exchange Place",
        .grove: "Grove Street",
        .harrison: "Harrison",
        .hoboken: "Hoboken",
        .jsq: "Journal Square",
        .newark: "Newark",
        .newport: "Newport",
        .christopher: "Christopher Street",
        .ninth: "9th Street",
        .fourteenth: "14th Street",
        .twentyThird: "23rd Street",
        .thirtyThird: "33rd Street",
        .wtc: "World Trade Center"
    ]
}

enum LineChoice : String, AppEnum {
    case nwkWtc, hobWtc, jsq33, hob33
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Lines"
    
    static var caseDisplayRepresentations: [LineChoice : DisplayRepresentation] = [
        .nwkWtc: "Newark ⇆ World Trade Center",
        .hobWtc: "Hoboken ⇆ World Trade Center",
        .jsq33: "Journal Square ⇆ 33rd Street",
        .hob33: "Hoboken ⇆ 33rd Street",
    ]
}

enum SortOrder : String, AppEnum {
    case alphabetical, njAm, nyAm
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Order"
    
    static var caseDisplayRepresentations: [SortOrder : DisplayRepresentation] = [
        .alphabetical: "A-Z",
        .njAm: "NJ morning, NY evening",
        .nyAm: "NY morning, NJ evening"
    ]
    
}

enum Filter : String, AppEnum {
    case all, interstate
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Filter"
    
    static var caseDisplayRepresentations: [Filter : DisplayRepresentation] = [
        .all: "All trains",
        .interstate: "Only interstate trains"
    ]
    
}

enum TimeDisplay : String, AppEnum {
    case relative, clock
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Time Display"
    
    static var caseDisplayRepresentations: [TimeDisplay : DisplayRepresentation] = [
        .relative: "5 min",
        .clock: "12:30",
    ]
}

enum TrainGrouping : String, AppEnum {
    case ungrouped, byHeadsign
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Group By Destination"
    
    static var caseDisplayRepresentations: [TrainGrouping : DisplayRepresentation] = [
        .ungrouped: "Off",
        .byHeadsign: "On",
    ]
}

struct ConfigurationAppIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Departure board for PATH"
    static var description = IntentDescription("Departure board for PATH trains")
    
    @Parameter(title: "Stations", default: [])
    var stations: [StationChoice]
    
    @Parameter(title: "Lines", default: [LineChoice.nwkWtc, LineChoice.hobWtc, LineChoice.jsq33, LineChoice.hob33])
    var lines: [LineChoice]
    
    @Parameter(title: "Filter", default: Filter.all)
    var filter: Filter
    
    @Parameter(title: "Time", default: TimeDisplay.relative)
    var timeDisplay: TimeDisplay
    
    @Parameter(title: "Group By Destination", default: TrainGrouping.byHeadsign)
    var trainGrouping: TrainGrouping
    
    @Parameter(title: "Order", default: SortOrder.alphabetical)
    var sortOrder: SortOrder
    
    init() {
        self.stations = []
        self.lines = LineChoice.allCases
        self.filter = .all
        self.timeDisplay = .relative
        self.trainGrouping = .byHeadsign
        self.sortOrder = .alphabetical
    }
    
    init(
        stations: [StationChoice] = [],
        lines: [LineChoice] = LineChoice.allCases,
        filter: Filter = .all,
        timeDisplay: TimeDisplay = .relative,
        trainGrouping: TrainGrouping = .byHeadsign,
        sortOrder: SortOrder = .alphabetical
    ) {
        self.stations = stations
        self.lines = lines
        self.filter = filter
        self.timeDisplay = timeDisplay
        self.trainGrouping = trainGrouping
        self.sortOrder = sortOrder
    }
}
