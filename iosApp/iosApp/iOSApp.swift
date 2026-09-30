import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        IosAppInitializerKt.doInitAppIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}