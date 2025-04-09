import { messaging, getToken } from '@/lib/firebase/firebase';
import { ensureServiceWorkerRegistered } from '@/lib/firebase/register-sw';

let isSubscribing = false;

export async function subscribeUserToPush(accessToken: string | null) {
  if (isSubscribing) {
    return;
  }

  if (localStorage.getItem('push_subscribed') === 'true') {
    return;
  }

  isSubscribing = true;

  const registration = await ensureServiceWorkerRegistered();
  if (!registration?.active) {
    localStorage.removeItem('push_subscribed');
    isSubscribing = false;
    return;
  }

  try {
    const token = await getToken(messaging, {
      vapidKey: process.env.NEXT_PUBLIC_VAPID_KEY,
      serviceWorkerRegistration: registration,
    });

    if (token) {
      await fetch(process.env.NEXT_PUBLIC_API_URL + '/api/push/subscribe', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({ token }),
      });

      localStorage.setItem('push_subscribed', 'true');
      console.log('푸시 구독 완료');
    } else {
      console.error('푸시 토큰 저장 실패');
    }
  } catch (err) {
    console.error('푸시 구독 중 에러 발생:', err);
  } finally {
    isSubscribing = false;
  }
}
