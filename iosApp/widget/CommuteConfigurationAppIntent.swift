//
//  CommuteConfigurationAppIntent.swift
//  widgetExtension
//
//  Created by Assistant on 2024-12-19.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import AppIntents
import ComposeApp

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

struct CommuteConfigurationAppIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Commute Tracker for PATH"
    static var description = IntentDescription("Commute-focused PATH widget with auto-reverse")
    
    static var parameterSummary: some ParameterSummary {
        When(\.$autoReverse, .equalTo, true) {
            Summary {
                \.$originStation
                \.$destinationStation
                \.$timeDisplay
                \.$autoReverse
                \.$reverseStartHour
                \.$reverseEndHour
                \.$showLastRefreshedTime
            }
        } otherwise: {
            Summary {
                \.$originStation
                \.$destinationStation
                \.$timeDisplay
                \.$autoReverse
                \.$showLastRefreshedTime
            }
        }
    }
    
    @Parameter(title: "Origin Station", default: .exp)
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

    @Parameter(title: "Show last refreshed time", default: false)
    var showLastRefreshedTime: Bool
    
    init() {
        self.originStation = .closest
        self.destinationStation = .wtc
        self.timeDisplay = .relative
        self.autoReverse = false
        self.reverseStartHour = .am11
        self.reverseEndHour = .pm8
    }
    
    init(
        originStation: StationChoice = .exp,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
        autoReverse: Bool = false,
        showLastRefreshedTime: Bool = false,
        reverseStartHour: HourOfDay? = nil,
        reverseEndHour: HourOfDay? = nil
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
        self.autoReverse = autoReverse
        self.showLastRefreshedTime = showLastRefreshedTime
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
