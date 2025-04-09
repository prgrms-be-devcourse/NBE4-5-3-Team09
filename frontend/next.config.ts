import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  webpack: (config, { isServer }) => {
    if (!isServer) {
      // 클라이언트 빌드에서만 실행
      config.module.rules.push({
        test: /firebase-messaging-sw\.js$/,
        use: [
          {
            loader: 'string-replace-loader',
            options: {
              search: 'NEXT_PUBLIC_FIREBASE_API_KEY', // 환경변수 주입할 부분
              replace: process.env.NEXT_PUBLIC_FIREBASE_API_KEY, // 환경 변수로 교체
              flags: 'g',
            },
          },
          {
            loader: 'string-replace-loader',
            options: {
              search: 'NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN',
              replace: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
              flags: 'g',
            },
          },
          {
            loader: 'string-replace-loader',
            options: {
              search: 'NEXT_PUBLIC_FIREBASE_PROJECT_ID',
              replace: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
              flags: 'g',
            },
          },
          {
            loader: 'string-replace-loader',
            options: {
              search: 'NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID',
              replace: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
              flags: 'g',
            },
          },
          {
            loader: 'string-replace-loader',
            options: {
              search: 'NEXT_PUBLIC_FIREBASE_APP_ID',
              replace: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
              flags: 'g',
            },
          },
          {
            loader: 'string-replace-loader',
            options: {
              search: 'NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID',
              replace: process.env.NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID,
              flags: 'g',
            },
          },
        ],
      });
    }
    return config;
  },
};

export default nextConfig;
