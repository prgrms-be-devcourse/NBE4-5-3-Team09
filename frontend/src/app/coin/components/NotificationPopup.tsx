'use client';

import { useEffect, useState } from 'react';
import { Dialog } from '@headlessui/react';
import { updatePushTopics, fetchSubscribeInfo } from '@/app/api/notification/route';

export type OneMinuteRate = 'NONE' | 'ONE' | 'THREE' | 'FIVE' | 'TEN';

interface NotificationPopupProps {
  market: string;
  accessToken: string;
  onClose: () => void;
}

const DROPDOWN_OPTIONS: { value: OneMinuteRate; label: string }[] = [
  { value: 'NONE', label: '받지 않음' },
  { value: 'ONE', label: '1%' },
  { value: 'THREE', label: '3%' },
  { value: 'FIVE', label: '5%' },
  { value: 'TEN', label: '10%' },
];

export default function NotificationPopup({
  market,
  accessToken,
  onClose,
}: NotificationPopupProps) {
  const [currentRate, setCurrentRate] = useState<OneMinuteRate>('NONE');
  const [selected, setSelected] = useState<OneMinuteRate>('NONE');

  useEffect(() => {
    fetchSubscribeInfo(accessToken, market)
      .then((data) => {
        setCurrentRate(data.oneMinuteRate);
        setSelected(data.oneMinuteRate);
      })
      .catch(() => alert('구독 정보를 불러오지 못했습니다.'));
  }, [market, accessToken]);

  const handleSave = async () => {
    onClose(); // 먼저 팝업 닫기

    try {
      await updatePushTopics(accessToken, market, selected, currentRate);
      console.log('알림 설정이 저장되었습니다.');
    } catch (err) {
      alert('알림 설정에 실패했습니다.');
    }
  };

  return (
    <Dialog open onClose={onClose} className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 w-full max-w-md shadow-xl">
        <Dialog.Title className="text-lg font-bold mb-4">알림 설정</Dialog.Title>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
            단기변동률(1분)
          </label>
          <select
            value={selected}
            onChange={(e) => setSelected(e.target.value as OneMinuteRate)}
            className="w-full rounded-md border border-gray-300 p-2 dark:bg-gray-700 dark:text-white"
          >
            {DROPDOWN_OPTIONS.map((opt) => (
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
