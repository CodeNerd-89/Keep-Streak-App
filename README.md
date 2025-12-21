# 📱 Keep Streak

**KeepStreak** is an Android application designed to help users build and maintain healthy digital habits by tracking app usage, maintaining streaks, and visualizing usage patterns. The app works entirely offline, ensuring user privacy by storing all data locally on the device.

---

## 🚀 Features

- **App Usage Tracking**  
  Track daily usage of selected applications using Android’s Usage Access APIs.

- **Streak Management**  
  Build and maintain streaks for consistent app usage habits.

- **Usage Statistics & Visualization**  
  Gain insights into your behavior with clear and informative charts (e.g., pie charts).

- **Offline & Privacy-Friendly**  
  All data, including usage history and streaks, is stored securely on the user’s device—no cloud, no tracking.

---

## 🛠 Tech Stack

- **Platform:** Android  
- **Language:** Java  
- **Build System:** Gradle  

### Libraries & Components
- **Room Persistence Library**  
  - `AppDatabase`  
  - `TrackedAppDao`  
  - `UsageHistoryDao`  

- **WorkManager**  
  - `UsageStatsWorker` – periodic usage data collection  
  - `StreakReminderWorker` – reminder notifications  

---

## 📦 Installation

To run the project locally:

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/KeepStreak.git
   cd KeepStreak
