import { useState, useEffect, useCallback } from 'react';
import ClipboardMonitor, { ClipItem } from './plugins/ClipboardMonitor';

function App() {
  const [clips, setClips] = useState<ClipItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadClips = useCallback(async () => {
    try {
      setLoading(true);
      const result = await ClipboardMonitor.getSavedClips();
      setClips(result.clips || []);
      setError(null);
    } catch (err) {
      console.error('Failed to load clips:', err);
      setError('Failed to load clipboard history. Make sure the app is running on Android.');
    } finally {
      setLoading(false);
    }
  }, []);

  const openSettings = async () => {
    try {
      await ClipboardMonitor.openAccessibilitySettings();
    } catch (err) {
      console.error('Failed to open settings:', err);
      setError('Failed to open accessibility settings.');
    }
  };

  useEffect(() => {
    loadClips();

    // Listen for real-time clipboard changes
    let removeListener: (() => void) | undefined;

    ClipboardMonitor.addListener('clipboardChanged', (data: ClipItem) => {
      setClips(prevClips => [data, ...prevClips]);
    }).then(({ remove }) => {
      removeListener = remove;
    }).catch(err => {
      console.error('Failed to add listener:', err);
    });

    return () => {
      if (removeListener) {
        removeListener();
      }
    };
  }, [loadClips]);

  const formatTimestamp = (timestamp: number) => {
    return new Date(timestamp).toLocaleString();
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-blue-600 text-white py-4 px-6 shadow-lg">
        <h1 className="text-2xl font-bold text-center">Clipboard Skeleton</h1>
      </header>

      <main className="container mx-auto px-4 py-6 max-w-2xl">
        {/* Permission Button */}
        <div className="mb-6">
          <button
            onClick={openSettings}
            className="w-full bg-green-500 hover:bg-green-600 text-white font-semibold py-3 px-6 rounded-lg shadow-md transition-colors duration-200 flex items-center justify-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            Enable Accessibility Service
          </button>
          <p className="text-sm text-gray-600 mt-2 text-center">
            Grant accessibility permission to monitor clipboard in the background
          </p>
        </div>

        {/* Refresh Button */}
        <div className="mb-4">
          <button
            onClick={loadClips}
            disabled={loading}
            className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200"
          >
            {loading ? 'Loading...' : 'Refresh'}
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {/* Clipboard History List */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-4 py-3 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800">
              Clipboard History ({clips.length})
            </h2>
          </div>

          {loading && clips.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto mb-2"></div>
              Loading clipboard history...
            </div>
          ) : clips.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              <svg className="w-12 h-12 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              No clipboard entries yet.
              <br />
              <span className="text-sm">Copy some text to see it here!</span>
            </div>
          ) : (
            <ul className="divide-y divide-gray-200">
              {clips.map((clip, index) => (
                <li key={`${clip.timestamp}-${index}`} className="p-4 hover:bg-gray-50 transition-colors">
                  <p className="text-gray-800 break-words whitespace-pre-wrap">
                    {clip.text}
                  </p>
                  <p className="text-xs text-gray-500 mt-2">
                    {formatTimestamp(clip.timestamp)}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </div>
  );
}

export default App;
