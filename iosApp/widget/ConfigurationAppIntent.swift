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

enum HourOfDay : Int, AppEnum {
    case am12 = 0, am1 = 1, am2 = 2, am3 = 3, am4 = 4, am5 = 5, am6 = 6, am7 = 7, am8 = 8, am9 = 9, am10 = 10, am11 = 11
    case pm12 = 12, pm1 = 13, pm2 = 14, pm3 = 15, pm4 = 16, pm5 = 17, pm6 = 18, pm7 = 19, pm8 = 20, pm9 = 21, pm10 = 22, pm11 = 23
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Hour"
    
    static var caseDisplayRepresentations: [HourOfDay : DisplayRepresentation] = [
        .am12: "12 AM",
        .am1: "1 AM",
        .am2: "2 AM",
        .am3: "3 AM",
        .am4: "4 AM",
        .am5: "5 AM",
        .am6: "6 AM",
        .am7: "7 AM",
        .am8: "8 AM",
        .am9: "9 AM",
        .am10: "10 AM",
        .am11: "11 AM",
        .pm12: "12 PM",
        .pm1: "1 PM",
        .pm2: "2 PM",
        .pm3: "3 PM",
        .pm4: "4 PM",
        .pm5: "5 PM",
        .pm6: "6 PM",
        .pm7: "7 PM",
        .pm8: "8 PM",
        .pm9: "9 PM",
        .pm10: "10 PM",
        .pm11: "11 PM"
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
    
    static var parameterSummary: some ParameterSummary {
        When(\.$autoReverse, .equalTo, true) {
            Summary {
                \.$originStation
                \.$destinationStation
                \.$timeDisplay
                \.$autoReverse
                \.$reverseStartHour
                \.$reverseEndHour
            }
        } otherwise: {
            Summary {
                \.$originStation
                \.$destinationStation
                \.$timeDisplay
                \.$autoReverse
            }
        }
    }
    
    @Parameter(title: "Origin Station", default: .closest)
    var originStation: StationChoice
    
    @Parameter(title: "Destination Station", default: .wtc)
    var destinationStation: StationChoice
    
    @Parameter(title: "Time", default: TimeDisplay.relative)
    var timeDisplay: TimeDisplay
    
    @Parameter(title: "Auto-reverse for commute", default: false)
    var autoReverse: Bool
    
    @Parameter(
        title: "Reverse from",
        default: .am11,
        requestValueDialog: IntentDialog("When should stations reverse?")
    )
    var reverseStartHour: HourOfDay?
    
    @Parameter(
        title: "Reverse until",
        default: .pm8,
        requestValueDialog: IntentDialog("When should reversal end?")
    )
    var reverseEndHour: HourOfDay?
    
    //past variables, keeping here for backwards compatibility
    var filter: Filter = .all
    var trainGrouping: TrainGrouping = .ungrouped
    var sortOrder: SortOrder = .alphabetical
    var lines: [LineChoice] = LineChoice.allCases
    
    init() {
        self.originStation = .closest
        self.destinationStation = .wtc
        self.timeDisplay = .relative
        self.autoReverse = false
        self.reverseStartHour = nil
        self.reverseEndHour = nil
    }
    
    init(
        originStation: StationChoice = .closest,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
        autoReverse: Bool = false,
        reverseStartHour: HourOfDay? = nil,
        reverseEndHour: HourOfDay? = nil
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
        self.autoReverse = autoReverse
        self.reverseStartHour = reverseStartHour
        self.reverseEndHour = reverseEndHour
    }
    
    // Helper function to determine if stations should be reversed based on current time
    func shouldReverseStations() -> Bool {
        guard autoReverse else { return false }
        guard let startHour = reverseStartHour?.rawValue,
              let endHour = reverseEndHour?.rawValue else {
            return false
        }
        
        let calendar = Calendar.current
        let now = Date()
        let currentHour = calendar.component(.hour, from: now)
        
        // Check if current hour is within the reverse range
        if startHour <= endHour {
            // Normal range (e.g., 9am to 5pm)
            return currentHour >= startHour && currentHour < endHour
        } else {
            // Overnight range (e.g., 9pm to 6am)
            return currentHour >= startHour || currentHour < endHour
        }
    }
    
    // Get effective origin/destination with auto-reverse applied
    func getEffectiveOrigin() -> StationChoice {
        return shouldReverseStations() ? destinationStation : originStation
    }
    
    func getEffectiveDestination() -> StationChoice {
        return shouldReverseStations() ? originStation : destinationStation
    }
}
