package com.clipboard.skeleton;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ClipboardAccessibilityService - Monitors clipboard changes in the background
 * using Android Accessibility Service API.
 * 
 * This service listens for text selection changes and clipboard events,
 * captures copied text, and persists it to SharedPreferences for later retrieval.
 */
public class ClipboardAccessibilityService extends AccessibilityService {
    
    private static final String TAG = "ClipboardAccessibility";
    private static final String PREFS_NAME = "clipboard_monitor_prefs";
    private static final String CLIPS_KEY = "saved_clips";
    private static final int MAX_CLIPS = 100; // Maximum number of clips to store
    
    private ClipboardManager clipboardManager;
    private String lastClipText = "";
    
    // Static reference for plugin communication
    private static ClipboardAccessibilityService instance;
    private static ClipboardEventListener eventListener;
    
    public interface ClipboardEventListener {
        void onClipboardChanged(String text, long timestamp);
    }
    
    public static void setEventListener(ClipboardEventListener listener) {
        eventListener = listener;
    }
    
    public static ClipboardAccessibilityService getInstance() {
        return instance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "ClipboardAccessibilityService created");
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "ClipboardAccessibilityService connected");
        
        // Configure the accessibility service
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED |
                          AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
                          AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                          AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);
        
        // Initialize clipboard manager
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        // Add clipboard listener
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(this::onClipboardChanged);
        }
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We primarily rely on the clipboard listener, but this method
        // can also detect text selection changes
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            checkClipboard();
        }
    }
    
    private void onClipboardChanged() {
        checkClipboard();
    }
    
    private void checkClipboard() {
        if (clipboardManager == null) {
            clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        }
        
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                CharSequence text = item.getText();
                if (text != null) {
                    String clipText = text.toString().trim();
                    if (!clipText.isEmpty() && !clipText.equals(lastClipText)) {
                        lastClipText = clipText;
                        long timestamp = System.currentTimeMillis();
                        
                        Log.d(TAG, "New clipboard text captured: " + clipText.substring(0, Math.min(50, clipText.length())));
                        
                        // Persist to storage immediately
                        saveClipToStorage(clipText, timestamp);
                        
                        // Notify listener (Capacitor plugin) if available
                        if (eventListener != null) {
                            eventListener.onClipboardChanged(clipText, timestamp);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Save clip to SharedPreferences for persistence
     */
    private void saveClipToStorage(String text, long timestamp) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String existingJson = prefs.getString(CLIPS_KEY, "[]");
            
            JSONArray clipsArray = new JSONArray(existingJson);
            
            // Create new clip object
            JSONObject newClip = new JSONObject();
            newClip.put("text", text);
            newClip.put("timestamp", timestamp);
            
            // Add to beginning of array (newest first)
            JSONArray newArray = new JSONArray();
            newArray.put(newClip);
            
            // Add existing clips (up to MAX_CLIPS - 1)
            for (int i = 0; i < Math.min(clipsArray.length(), MAX_CLIPS - 1); i++) {
                newArray.put(clipsArray.get(i));
            }
            
            // Save to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(CLIPS_KEY, newArray.toString());
            editor.apply();
            
            Log.d(TAG, "Clip saved to storage. Total clips: " + newArray.length());
            
        } catch (JSONException e) {
            Log.e(TAG, "Error saving clip to storage", e);
        }
    }
    
    /**
     * Get all saved clips from storage
     */
    public static String getSavedClipsJson(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(CLIPS_KEY, "[]");
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "ClipboardAccessibilityService interrupted");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "ClipboardAccessibilityService destroyed");
    }
}
