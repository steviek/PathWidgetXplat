//
//  EmptyDepartureBoardView.swift
//  widget2Extension
//
//  Created by Steven Kideckel on 2023-10-21.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct EmptyDepartureBoardView: View {
    var body: some View {
        ZStack(alignment: .center) {
            Text(getString(strings().edit_to_add_stations))
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview {
    EmptyDepartureBoardView()
}
