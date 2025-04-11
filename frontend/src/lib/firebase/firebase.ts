import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
  measurementId: process.env.NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID,
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

onMessage(messaging, (payload) => {
  const timestamp = parseInt(payload.data?.timestamp || '0', 10);
  const now = Date.now();
  const ageInSeconds = (now - timestamp) / 1000;

  // 일정 시간 지난 알림 무시
  if (ageInSeconds > 5) {
    console.log('[Foreground] expired push skipped');
    return;
  }

  const notificationTitle = payload.data?.title;
  const notificationOptions: NotificationOptions = {
    body: payload.data?.body,
    icon: '/logo.svg',
  };

  if (Notification.permission === 'granted') {
    const notification = new Notification(notificationTitle!, notificationOptions);

    notification.onclick = () => {
      const targetUrl = new URL(payload.data?.url!, window.location.origin).href;

      // 현재 창에서 이동
      window.location.href = targetUrl;

      // 알림 수동 닫기
      notification.close();
    };
  }
});

export { messaging, getToken, onMessage };
