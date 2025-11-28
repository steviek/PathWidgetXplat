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

enum Weekday: String, AppEnum, CaseIterable {
    case sunday = "sunday"
    case monday = "monday"
    case tuesday = "tuesday"
    case wednesday = "wednesday"
    case thursday = "thursday"
    case friday = "friday"
    case saturday = "saturday"
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Day"
    
    static var caseDisplayRepresentations: [Weekday: DisplayRepresentation] = [
        .sunday: "Sunday",
        .monday: "Monday",
        .tuesday: "Tuesday",
        .wednesday: "Wednesday",
        .thursday: "Thursday",
        .friday: "Friday",
        .saturday: "Saturday"
    ]
    
    static var weekdays: [Weekday] {
        [.monday, .tuesday, .wednesday, .thursday, .friday]
    }
    
    // Calendar weekday: 1 = Sunday, 2 = Monday, ..., 7 = Saturday
    var calendarWeekday: Int {
        switch self {
        case .sunday: return 1
        case .monday: return 2
        case .tuesday: return 3
        case .wednesday: return 4
        case .thursday: return 5
        case .friday: return 6
        case .saturday: return 7
        }
    }
}

struct CommuteConfigurationAppIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "My Commute"
    static var description = IntentDescription("Route based widget with seasonal themes and auto-reverse for commutes")
    
    static var parameterSummary: some ParameterSummary {
        When(\.$autoReverse, .equalTo, true) {
            Summary {
                \.$originStation
                \.$destinationStation
                \.$timeDisplay
                \.$autoReverse
                \.$reverseDays
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
        default: .pm12,
        requestValueDialog: IntentDialog("When should stations reverse?")
    )
    var reverseStartHour: HourOfDay?
    
    @Parameter(
        title: "Reverse until",
        default: .am3,
        requestValueDialog: IntentDialog("When should reversal end?")
    )
    var reverseEndHour: HourOfDay?

        @Parameter(
        title: "Reverse on days",
        default: [Weekday.monday, Weekday.tuesday, Weekday.wednesday, Weekday.thursday, Weekday.friday]
    )
    var reverseDays: [Weekday]

    @Parameter(title: "Show last refreshed time", default: false)
    var showLastRefreshedTime: Bool
    
    init() {
        self.originStation = .closest
        self.destinationStation = .wtc
        self.timeDisplay = .relative
        self.autoReverse = false
        self.reverseDays = Weekday.weekdays
        self.reverseStartHour = .pm12
        self.reverseEndHour = .am3
    }
    
    init(
        originStation: StationChoice = .exp,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
        autoReverse: Bool = false,
        showLastRefreshedTime: Bool = false,
        reverseDays: [Weekday] = Weekday.weekdays,
        reverseStartHour: HourOfDay? = nil,
        reverseEndHour: HourOfDay? = nil
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
        self.autoReverse = autoReverse
        self.showLastRefreshedTime = showLastRefreshedTime
        self.reverseDays = reverseDays
        self.reverseStartHour = reverseStartHour
        self.reverseEndHour = reverseEndHour
    }
    
    // Helper function to determine if stations should be reversed based on current time and day
    func shouldReverseStations() -> Bool {
        guard autoReverse else { return false }
        guard let startHour = reverseStartHour?.rawValue,
              let endHour = reverseEndHour?.rawValue else {
            return false
        }
        
        let calendar = Calendar.current
        let now = Date()
        let currentHour = calendar.component(.hour, from: now)
        let currentWeekday = calendar.component(.weekday, from: now)
        
        // Check if current day is in the selected days
        // Use weekdays as default if array is empty (fallback for parameter default issues)
        let activeDays = reverseDays.isEmpty ? Weekday.weekdays : reverseDays
        let isActiveDay = activeDays.contains { $0.calendarWeekday == currentWeekday }
        guard isActiveDay else { return false }
        
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
        //return destinationStation
        return shouldReverseStations() ? destinationStation : originStation
    }
    
    func getEffectiveDestination() -> StationChoice {
        //return originStation
        return shouldReverseStations() ? originStation : destinationStation
    }
}
