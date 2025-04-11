'use client';

import { useEffect, useState } from 'react';
import { Dialog } from '@headlessui/react';
import { client } from '@/lib/api/client';
import type { components } from '@/lib/api/generated/schema';

type OneMinuteRate = components['schemas']['SubscribeInfo']['oneMinuteRate'];
type TradeImpact = components['schemas']['SubscribeInfo']['tradeImpact'];
type SubscribeRequest = components['schemas']['SubscribeRequest'];

interface NotificationPopupProps {
  market: string;
  accessToken: string;
  onClose: () => void;
}

const RATE_OPTIONS: { value: OneMinuteRate; label: string }[] = [
  { value: 'NONE', label: '받지 않음' },
  { value: 'ONE', label: '1% 이상' },
  { value: 'THREE', label: '3% 이상' },
  { value: 'FIVE', label: '5% 이상' },
  { value: 'TEN', label: '10% 이상' },
];

const IMPACT_OPTIONS: { value: TradeImpact; label: string }[] = [
  { value: 'NONE', label: '받지 않음' },
  { value: 'SLIGHT', label: '0.1% 이상' },
  { value: 'MEDIUM', label: '0.5% 이상' },
  { value: 'STRONG', label: '1% 이상' },
];

export default function NotificationPopup({
  market,
  accessToken,
  onClose,
}: NotificationPopupProps) {
  const [currentRate, setCurrentRate] = useState<OneMinuteRate>('NONE');
  const [selectedRate, setSelectedRate] = useState<OneMinuteRate>('NONE');

  const [currentImpact, setCurrentImpact] = useState<TradeImpact>('NONE');
  const [selectedImpact, setSelectedImpact] = useState<TradeImpact>('NONE');

  useEffect(() => {
    fetchSubscribeInfo(accessToken, market)
      .then((data) => {
        setCurrentRate(data.oneMinuteRate);
        setSelectedRate(data.oneMinuteRate);
        setCurrentImpact(data.tradeImpact);
        setSelectedImpact(data.tradeImpact);
      })
      .catch(() => alert('구독 정보를 불러오지 못했습니다.'));
  }, [market, accessToken]);

  const handleSave = async () => {
    onClose(); // 먼저 팝업 닫기

    try {
      await updatePushTopics(
        accessToken,
        market,
        selectedRate,
        currentRate,
        selectedImpact,
        currentImpact,
      );
      console.log('알림 설정이 저장되었습니다.');
    } catch {
      alert('알림 설정에 실패했습니다.');
    }
  };

  // 알림 설정 변경 (POST /api/push/subscribe)
  async function updatePushTopics(
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
  async function fetchSubscribeInfo(
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

  return (
    <Dialog open onClose={onClose} className="fixed inset-0 z-50 flex items-center justify-center">
      {/* ✅ 팝업 외 배경 (Overlay) */}
      <div className="fixed inset-0 bg-black/30" onClick={onClose} aria-hidden="true" />

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 w-full max-w-md shadow-xl z-10">
        <Dialog.Title className="text-lg font-bold mb-4">알림 설정</Dialog.Title>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
            단기변동률(1분)
          </label>
          <select
            value={selectedRate}
            onChange={(e) => setSelectedRate(e.target.value as OneMinuteRate)}
            className="w-full rounded-md border border-gray-300 p-2 dark:bg-gray-700 dark:text-white"
          >
            {RATE_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
            체결가 변동률(임팩트)
          </label>
          <select
            value={selectedImpact}
            onChange={(e) => setSelectedImpact(e.target.value as TradeImpact)}
            className="w-full rounded-md border border-gray-300 p-2 dark:bg-gray-700 dark:text-white"
          >
            {IMPACT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex justify-end space-x-2">
          <button
            onClick={handleSave}
            className="px-4 py-2 rounded-md bg-blue-600 text-white text-sm hover:bg-blue-700"
          >
            저장
          </button>
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-md bg-gray-300 hover:bg-gray-400 text-sm"
          >
            취소
          </button>
        </div>
      </div>
    </Dialog>
  );
}
