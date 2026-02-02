import { registerPlugin } from '@capacitor/core';

export interface ClipItem {
  text: string;
  timestamp: number;
}

export interface ClipboardMonitorPlugin {
  /**
   * Get all saved clipboard clips from native storage
   */
  getSavedClips(): Promise<{ clips: ClipItem[] }>;
  
  /**
   * Open Android Accessibility Settings
   */
  openAccessibilitySettings(): Promise<void>;
  
  /**
   * Add listener for new clipboard events
   */
  addListener(
    eventName: 'clipboardChanged',
    listenerFunc: (data: ClipItem) => void
  ): Promise<{ remove: () => void }>;
}

const ClipboardMonitor = registerPlugin<ClipboardMonitorPlugin>('ClipboardMonitor');

export default ClipboardMonitor;
