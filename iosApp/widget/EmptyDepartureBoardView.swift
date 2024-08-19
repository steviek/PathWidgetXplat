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
    let isError: Bool
    let isPathError: Bool
    
    var body: some View {
        ZStack(alignment: .center) {
            Text(getText())
                .font(Font.system(size: (isError ? 12 : 16)))
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func getText() -> String {
        if isError {
            return IosResourceProvider().getEmptyErrorMessage(isPathApiError: isPathError)
        } else {
            return IosResourceProvider().getEmptyStateString()
        }
    }
}

#Preview {
    EmptyDepartureBoardView(isError: false, isPathError: false)
}
