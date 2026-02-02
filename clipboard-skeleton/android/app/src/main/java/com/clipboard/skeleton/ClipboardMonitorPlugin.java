package com.clipboard.skeleton;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ClipboardMonitorPlugin - Capacitor plugin that bridges React and Java
 * for clipboard monitoring functionality.
 */
@CapacitorPlugin(name = "ClipboardMonitor")
public class ClipboardMonitorPlugin extends Plugin {
    
    private static final String TAG = "ClipboardMonitorPlugin";
    private static final String EVENT_CLIPBOARD_CHANGED = "clipboardChanged";
    
    @Override
    public void load() {
        super.load();
        
        // Set up listener for clipboard events from the accessibility service
        ClipboardAccessibilityService.setEventListener((text, timestamp) -> {
            JSObject data = new JSObject();
            data.put("text", text);
            data.put("timestamp", timestamp);
            notifyListeners(EVENT_CLIPBOARD_CHANGED, data);
            Log.d(TAG, "Clipboard event sent to JS: " + text.substring(0, Math.min(50, text.length())));
        });
    }
    
    /**
     * Get all saved clips from native storage
     */
    @PluginMethod
    public void getSavedClips(PluginCall call) {
        try {
            Context context = getContext();
            String clipsJson = ClipboardAccessibilityService.getSavedClipsJson(context);
            
            JSONArray jsonArray = new JSONArray(clipsJson);
            JSArray clips = new JSArray();
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonClip = jsonArray.getJSONObject(i);
                JSObject clip = new JSObject();
                clip.put("text", jsonClip.getString("text"));
                clip.put("timestamp", jsonClip.getLong("timestamp"));
                clips.put(clip);
            }
            
            JSObject result = new JSObject();
            result.put("clips", clips);
            call.resolve(result);
            
            Log.d(TAG, "Returned " + clips.length() + " clips");
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing clips JSON", e);
            call.reject("Failed to get saved clips", e);
        }
    }
    
    /**
     * Open Android Accessibility Settings for the user to enable the service
     */
    @PluginMethod
    public void openAccessibilitySettings(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
            Log.d(TAG, "Opened accessibility settings");
        } catch (Exception e) {
            Log.e(TAG, "Error opening accessibility settings", e);
            call.reject("Failed to open accessibility settings", e);
        }
    }
}
