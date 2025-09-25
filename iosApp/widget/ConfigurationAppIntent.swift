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
    
    @Parameter(title: "Origin Station", default: .closest)
    var originStation: StationChoice
    
    @Parameter(title: "Destination Station", default: .wtc)
    var destinationStation: StationChoice
    
    @Parameter(title: "Time", default: TimeDisplay.relative)
    var timeDisplay: TimeDisplay
    
    //past variables, keeping here for backwards compatibility
    var filter: Filter = .all
    var trainGrouping: TrainGrouping = .ungrouped
    var sortOrder: SortOrder = .alphabetical
    var lines: [LineChoice] = LineChoice.allCases
    
    init() {
        self.originStation = .closest
        self.destinationStation = .wtc
        self.timeDisplay = .relative
    }
    
    init(
        originStation: StationChoice = .closest,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
    }
}
