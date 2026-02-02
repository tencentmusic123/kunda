import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.clipboard.skeleton',
  appName: 'Clipboard Skeleton',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  }
};

export default config;
