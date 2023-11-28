//
//  HomeView.swift
//  iosApp
//
//  Created by Steven Kideckel on 2023-10-23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct HomeView: View {
    var body: some View {
        ZStack {
            VStack {
                Spacer()
                Text(getString(strings().welcome_text))
                    .multilineTextAlignment(.center)
                Spacer()
            }
            
            
            VStack {
                Spacer()
                Button(action: emailSupport) {
                    Text(getString(strings().report_a_problem))
                }
                .padding()
                Spacer().frame(height: 8)
            }
        }
        .padding()
    }
    
    private func emailSupport() {
        if let url = URL(string: "mailto:sixbynineapps@gmail.com") {
            UIApplication.shared.open(url)
        }
    }
}

#Preview {
    HomeView()
}
