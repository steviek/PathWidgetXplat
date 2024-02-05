import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .onAppear {
                AppLifecycleObserver().setAppIsActive(isActive: true)
            }
            .onDisappear {
                AppLifecycleObserver().setAppIsActive(isActive: false)
            }
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                AppLifecycleObserver().setAppIsActive(isActive: true)
            }
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification)) { _ in
                AppLifecycleObserver().setAppIsActive(isActive: false)
            }
            .ignoresSafeArea(.all) // Compose has own keyboard handler
    }
}



