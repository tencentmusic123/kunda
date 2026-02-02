# Clipboard Skeleton

A Capacitor-based Android application for testing background clipboard monitoring using an Accessibility Service.

## Tech Stack

- **Frontend**: React + Vite + TypeScript + Tailwind CSS
- **Mobile Bridge**: Capacitor JS 6+
- **Native Android**: Java (Accessibility Service)

## Features

- Background clipboard monitoring using Android Accessibility Service
- Persistent storage of copied text in SharedPreferences
- Real-time updates when new clipboard content is captured
- Clean UI with Tailwind CSS

## Project Structure

```
clipboard-skeleton/
├── src/                          # React frontend source
│   ├── App.tsx                   # Main React component
│   ├── main.tsx                  # React entry point
│   ├── index.css                 # Tailwind CSS styles
│   └── plugins/
│       └── ClipboardMonitor.ts   # Capacitor plugin interface
├── android/                      # Android native code
│   └── app/src/main/java/com/clipboard/skeleton/
│       ├── MainActivity.java
│       ├── ClipboardAccessibilityService.java
│       └── ClipboardMonitorPlugin.java
├── package.json
├── capacitor.config.ts
├── tailwind.config.js
├── postcss.config.js
├── tsconfig.json
└── vite.config.ts
```

## Setup Instructions

### Prerequisites

- Node.js 18+
- Android Studio (with Android SDK)
- Java 17+

### Installation

1. Install dependencies:
   ```bash
   cd clipboard-skeleton
   npm install
   ```

2. Build the web app:
   ```bash
   npm run build
   ```

3. Sync with Capacitor:
   ```bash
   npx cap sync android
   ```

4. Open in Android Studio:
   ```bash
   npx cap open android
   ```

5. Build and run on device/emulator from Android Studio.

## Usage

1. Launch the app on your Android device.
2. Tap "Enable Accessibility Service" button.
3. Find "Clipboard Skeleton" in the accessibility services list and enable it.
4. Copy any text from any app - it will be captured and displayed in the app.

## Native Plugin Methods

### `getSavedClips()`
Returns all saved clipboard entries from native storage.

```typescript
const result = await ClipboardMonitor.getSavedClips();
console.log(result.clips); // Array of { text: string, timestamp: number }
```

### `openAccessibilitySettings()`
Opens Android Accessibility Settings for enabling the service.

```typescript
await ClipboardMonitor.openAccessibilitySettings();
```

### Event Listener
Listen for real-time clipboard changes:

```typescript
ClipboardMonitor.addListener('clipboardChanged', (data) => {
  console.log('New clip:', data.text, data.timestamp);
});
```

## Security Note

This app requires Accessibility Service permission to monitor clipboard activity. This permission should only be granted by the user after understanding its implications.

## License

MIT
