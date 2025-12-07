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

enum DayOfWeek: String, AppEnum, CaseIterable {
    case sunday = "sunday"
    case monday = "monday"
    case tuesday = "tuesday"
    case wednesday = "wednesday"
    case thursday = "thursday"
    case friday = "friday"
    case saturday = "saturday"
    
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Day"
    
    static var caseDisplayRepresentations: [DayOfWeek: DisplayRepresentation] = [
        .sunday: "Sunday",
        .monday: "Monday",
        .tuesday: "Tuesday",
        .wednesday: "Wednesday",
        .thursday: "Thursday",
        .friday: "Friday",
        .saturday: "Saturday"
    ]
    
    static var weekdays: [DayOfWeek] {
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
    static var description = IntentDescription("Route based widget with reverse for commutes home and seasonal themes")
    
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
                \.$useSeasonalBackgrounds
            }
        } otherwise: {
            Summary {
                \.$originStation
                \.$destinationStation
                \.$timeDisplay
                \.$autoReverse
                \.$showLastRefreshedTime
                \.$useSeasonalBackgrounds
            }
        }
    }
    
    @Parameter(title: "Origin Station", default: .exp)
    var originStation: StationChoice

    @Parameter(title: "Destination Station", default: .wtc)
    var destinationStation: StationChoice
    
    @Parameter(title: "Time", default: TimeDisplay.relative)
    var timeDisplay: TimeDisplay
    
    @Parameter(title: "Reverse for commute home", default: true)
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
        title: "Reverse from",
        requestValueDialog: IntentDialog("When should stations reverse?")
    )
    var reverseStartTime: Date?
    
    @Parameter(
        title: "Reverse until",
        requestValueDialog: IntentDialog("When should reversal end?")
    )
    var reverseEndTime: Date?
    
    // Helper to create default date for 12:00 PM (noon)
    private static func defaultStartTime() -> Date {
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year, .month, .day], from: Date())
        components.hour = 12
        components.minute = 0
        return calendar.date(from: components) ?? Date()
    }
    
    // Helper to create default date for 3:00 AM
    private static func defaultEndTime() -> Date {
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year, .month, .day], from: Date())
        components.hour = 3
        components.minute = 0
        return calendar.date(from: components) ?? Date()
    }

    @Parameter(
    title: "Reverse on days",
    default: [DayOfWeek.monday, DayOfWeek.tuesday, DayOfWeek.wednesday, DayOfWeek.thursday, DayOfWeek.friday]
    )
    var reverseDays: [DayOfWeek]

    @Parameter(title: "Show last refreshed time", default: false)
    var showLastRefreshedTime: Bool

    @Parameter(title: "Seasonal themed backgrounds", default: true)
    var useSeasonalBackgrounds: Bool
    
    init() {
        self.originStation = .closest
        self.destinationStation = .wtc
        self.timeDisplay = .relative
        self.autoReverse = false
        self.reverseDays = DayOfWeek.weekdays
        self.reverseStartHour = .pm12
        self.reverseEndHour = .am3
        self.useSeasonalBackgrounds = true
        
        let calendar = Calendar.current
        // Default: 12:00 PM (noon)
        var components = calendar.dateComponents([.year, .month, .day], from: Date())
        components.hour = 12
        components.minute = 0
        self.reverseStartTime = calendar.date(from: components)
        
        // Default: 3:00 AM
        components.hour = 3
        components.minute = 0
        self.reverseEndTime = calendar.date(from: components)
    }
    
    init(
        originStation: StationChoice = .exp,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
        autoReverse: Bool = false,
        showLastRefreshedTime: Bool = false,
        useSeasonalBackgrounds: Bool = true,
        reverseDays: [DayOfWeek] = DayOfWeek.weekdays,
        reverseStartHour: HourOfDay? = nil,
        reverseEndHour: HourOfDay? = nil
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
        self.autoReverse = autoReverse
        self.showLastRefreshedTime = showLastRefreshedTime
        self.useSeasonalBackgrounds = useSeasonalBackgrounds
        self.reverseDays = reverseDays
        self.reverseStartHour = reverseStartHour
        self.reverseEndHour = reverseEndHour
        
        // Convert HourOfDay to Date if provided, otherwise use defaults
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year, .month, .day], from: Date())
        
        if let startHour = reverseStartHour {
            components.hour = startHour.rawValue
            components.minute = 0
            self.reverseStartTime = calendar.date(from: components)
        } else {
            // Default: 12:00 PM (noon)
            components.hour = 12
            components.minute = 0
            self.reverseStartTime = calendar.date(from: components)
        }
        
        if let endHour = reverseEndHour {
            components.hour = endHour.rawValue
            components.minute = 0
            self.reverseEndTime = calendar.date(from: components)
        } else {
            // Default: 3:00 AM
            components.hour = 3
            components.minute = 0
            self.reverseEndTime = calendar.date(from: components)
        }
    }
    
    @available(iOS 18, *)
    init(
        originStation: StationChoice = .exp,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
        autoReverse: Bool = false,
        showLastRefreshedTime: Bool = false,
        useSeasonalBackgrounds: Bool = true,
        reverseDays: [DayOfWeek] = DayOfWeek.weekdays,
        reverseStartTime: Date? = nil,
        reverseEndTime: Date? = nil
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
        self.autoReverse = autoReverse
        self.showLastRefreshedTime = showLastRefreshedTime
        self.useSeasonalBackgrounds = useSeasonalBackgrounds
        self.reverseDays = reverseDays
        self.reverseStartTime = reverseStartTime
        self.reverseEndTime = reverseEndTime
        
        // Convert Date to HourOfDay for backward compatibility
        if let startTime = reverseStartTime {
            let hour = Calendar.current.component(.hour, from: startTime)
            self.reverseStartHour = HourOfDay(rawValue: hour)
        } else {
            self.reverseStartHour = .pm12
        }
        
        if let endTime = reverseEndTime {
            let hour = Calendar.current.component(.hour, from: endTime)
            self.reverseEndHour = HourOfDay(rawValue: hour)
        } else {
            self.reverseEndHour = .am3
        }
    }
    
    // Helper methods to get hour value (0-23) from either Date (iOS 18+) or HourOfDay (iOS < 18)
    // On iOS 18+, Date parameters automatically format based on user's 12hr/24hr preference
    func getReverseStartHour() -> Int? {
        if #available(iOS 18, *) {
            // On iOS 18+, prefer Date parameter if set, otherwise fall back to HourOfDay
            if let date = reverseStartTime {
                return Calendar.current.component(.hour, from: date)
            }
        }
        return reverseStartHour?.rawValue
    }
    
    func getReverseEndHour() -> Int? {
        if #available(iOS 18, *) {
            // On iOS 18+, prefer Date parameter if set, otherwise fall back to HourOfDay
            if let date = reverseEndTime {
                return Calendar.current.component(.hour, from: date)
            }
        }
        return reverseEndHour?.rawValue
    }
    
    // Helper function to determine if stations should be reversed based on current time and day
    func shouldReverseStations() -> Bool {
        guard autoReverse else { return false }
        guard let startHour = getReverseStartHour(),
              let endHour = getReverseEndHour() else {
            return false
        }
        
        let calendar = Calendar.current
        let now = Date()
        let currentHour = calendar.component(.hour, from: now)
        var currentWeekday = calendar.component(.weekday, from: now)

        if startHour == endHour {
            // If start == end, then it's valid from start on the start day until the end on
            // the next day.
        } else if startHour > endHour {
            // Overnight schedule, disabled between end and start time.
            if currentHour >= endHour && currentHour < startHour {
                return false
            }
        } else {
            // Non-overnight
            if currentHour < startHour || currentHour >= endHour {
                return false
            }
        }
        
        if startHour >= endHour && currentHour < endHour {
            currentWeekday -= 1
            if currentWeekday < 1 { currentWeekday = 7 }
        }
        
        // Check if current day is in the selected days
        // Use weekdays as default if array is empty (fallback for parameter default issues)
        let activeDays = reverseDays.isEmpty ? DayOfWeek.weekdays : reverseDays
        return activeDays.contains { $0.calendarWeekday == currentWeekday }
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
