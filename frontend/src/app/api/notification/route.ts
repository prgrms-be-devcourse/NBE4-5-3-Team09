import { getToken, messaging } from '@/lib/firebase/firebase';
import { ensureServiceWorkerRegistered } from '@/lib/firebase/register-sw';
import { client } from '@/lib/api/client';
import type { components } from '@/lib/api/generated/schema';

type OneMinuteRate = components['schemas']['SubscribeInfo']['oneMinuteRate'];
type TradeImpact = components['schemas']['SubscribeInfo']['tradeImpact'];
type SubscribeRequest = components['schemas']['SubscribeRequest'];
type PushTokenSaveRequest = components['schemas']['PushTokenSaveRequest'];

let isSubscribing = false;

// 푸시 토큰 초기 등록 (POST /api/push/register)
export async function subscribeUserToPush(accessToken: string | null) {
  if (isSubscribing || localStorage.getItem('push_subscribed') === 'true') return;

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
      const body: PushTokenSaveRequest = { token };

      const { error } = await client.POST('/api/push/register', {
        body,
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (error) {
        throw new Error('푸시 토큰 등록 실패');
      }

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

// 알림 설정 변경 (POST /api/push/subscribe)
export async function updatePushTopics(
  accessToken: string,
  market: string,
  subscribeRate: OneMinuteRate,
  unsubscribeRate: OneMinuteRate,
  subscribeImpact: TradeImpact,
  unsubscribeImpact: TradeImpact,
) {
  const body: SubscribeRequest = {
    market,
    subscribeInfo: {
      oneMinuteRate: subscribeRate,
      tradeImpact: subscribeImpact,
    },
    unsubscribeInfo: {
      oneMinuteRate: unsubscribeRate,
      tradeImpact: unsubscribeImpact,
    },
  };

  const { error } = await client.POST('/api/push/subscribe', {
    body,
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (error) {
    console.error('알림 구독 변경 실패:', error);
    throw error;
  }

  console.log('알림 구독 변경 완료');
}

// 현재 구독 정보 조회 API
export async function fetchSubscribeInfo(
  accessToken: string,
  market: string,
): Promise<components['schemas']['SubscribeInfo']> {
  const { data, error } = await client.GET('/api/push/subscribe-info', {
    params: {
      query: { market },
    },
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (error || !data) {
    throw new Error('구독 정보를 가져오는 데 실패했습니다.');
  }

  return data;
}
