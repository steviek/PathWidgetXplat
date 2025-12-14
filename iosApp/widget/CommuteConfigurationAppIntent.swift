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

// MARK: - Hour Entity Types

// Default hour IDs
private let defaultStartHourId = 12
private let defaultEndHourId = 3

// Helper to format hour according to user's locale
private func formatHour(_ hour: Int) -> String {
    let date = Calendar.current.date(bySetting: .hour, value: hour, of: Date()) ?? Date()
    return date.formatted(date: .omitted, time: .shortened)
}

// Protocol for shared hour entity behavior
protocol HourEntityProtocol: AppEntity where ID == Int {
    var id: Int { get }
    init(id: Int)
}

extension HourEntityProtocol {
    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(stringLiteral: formatHour(id))
    }
}

// Start hour entity (default: 12 PM)
struct StartHourEntity: HourEntityProtocol {
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Hour"
    static var defaultQuery = StartHourQuery()
    var id: Int
}

// End hour entity (default: 3 AM)
struct EndHourEntity: HourEntityProtocol {
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Hour"
    static var defaultQuery = EndHourQuery()
    var id: Int
}

// MARK: - Hour Entity Queries

struct StartHourQuery: EntityQuery {
    func entities(for identifiers: [Int]) async throws -> [StartHourEntity] {
        identifiers.map { StartHourEntity(id: $0) }
    }
    
    func suggestedEntities() async throws -> [StartHourEntity] {
        (0..<24).map { StartHourEntity(id: $0) }
    }
    
    func defaultResult() async -> StartHourEntity? {
        StartHourEntity(id: defaultStartHourId)
    }
}

struct EndHourQuery: EntityQuery {
    func entities(for identifiers: [Int]) async throws -> [EndHourEntity] {
        identifiers.map { EndHourEntity(id: $0) }
    }
    
    func suggestedEntities() async throws -> [EndHourEntity] {
        (0..<24).map { EndHourEntity(id: $0) }
    }
    
    func defaultResult() async -> EndHourEntity? {
        EndHourEntity(id: defaultEndHourId)
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
                \.$startHour
                \.$endHour
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
    
    @Parameter(title: "Reverse from", default: nil)
    var startHour: StartHourEntity?
    
    @Parameter(title: "Reverse until", default: nil)
    var endHour: EndHourEntity?

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
        self.useSeasonalBackgrounds = true
        self.startHour = StartHourEntity(id: defaultStartHourId)
        self.endHour = EndHourEntity(id: defaultEndHourId)
    }
    
    init(
        originStation: StationChoice = .exp,
        destinationStation: StationChoice = .wtc,
        timeDisplay: TimeDisplay = .relative,
        autoReverse: Bool = false,
        showLastRefreshedTime: Bool = false,
        useSeasonalBackgrounds: Bool = true,
        reverseDays: [DayOfWeek] = DayOfWeek.weekdays,
        startHour: StartHourEntity? = nil,
        endHour: EndHourEntity? = nil
    ) {
        self.originStation = originStation
        self.destinationStation = destinationStation
        self.timeDisplay = timeDisplay
        self.autoReverse = autoReverse
        self.showLastRefreshedTime = showLastRefreshedTime
        self.useSeasonalBackgrounds = useSeasonalBackgrounds
        self.reverseDays = reverseDays
        self.startHour = startHour ?? StartHourEntity(id: defaultStartHourId)
        self.endHour = endHour ?? EndHourEntity(id: defaultEndHourId)
    }
    
    // Get hour IDs with defaults applied
    var startHourId: Int { startHour?.id ?? defaultStartHourId }
    var endHourId: Int { endHour?.id ?? defaultEndHourId }
    
    // Helper function to determine if stations should be reversed based on current time and day
    func shouldReverseStations() -> Bool {
        guard autoReverse else { return false }
        
        let startId = startHourId
        let endId = endHourId
        
        let calendar = Calendar.current
        let now = Date()
        let currentHour = calendar.component(.hour, from: now)
        var currentWeekday = calendar.component(.weekday, from: now)

        if startId == endId {
            // If start == end, then it's valid from start on the start day until the end on
            // the next day.
        } else if startId > endId {
            // Overnight schedule, disabled between end and start time.
            if currentHour >= endId && currentHour < startId {
                return false
            }
        } else {
            // Non-overnight
            if currentHour < startId || currentHour >= endId {
                return false
            }
        }
        
        if startId >= endId && currentHour < endId {
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
