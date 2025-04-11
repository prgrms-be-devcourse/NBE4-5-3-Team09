'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { Menu, X } from 'lucide-react';
import { useWebSocket } from '@/context/WebSocketContext';
import OrderbookList from '../components/orderbook/OrderbookList';
import CandleChart from '../components/CandleChart';
import NewsList from '../components/NewsList';
import type { CandleItem, NewsItem } from '@/types';
import TradeList from '@/app/coin/components/TradeList';
import Ticker from '@/app/coin/components/Ticker';
import { fetchApi } from '@/lib/api';
import ChatPopup from '@/app/coin/components/ChatPopup';
import ShareModal from '@/app/coin/components/ShareModal';
import NotificationPopup from '../components/NotificationPopup';

export default function ClientPage() {
  const { market } = useParams() as { market: string };
  const { tickers, trades, orderbooks } = useWebSocket();
  const ticker = tickers?.[market] ?? null;
  const trade = trades?.[market] ?? null;
  const orderbook = orderbooks?.[market] ?? null;

  const [candles, setCandles] = useState<CandleItem[]>([]);
  const [candleType, setCandleType] = useState<
    'seconds' | 'minutes' | 'days' | 'weeks' | 'months' | 'years'
  >('seconds');
  const [minuteUnit, setMinuteUnit] = useState(1);
  const [news, setNews] = useState<NewsItem[]>([]);
  const [isChatPopupOpen, setChatPopupOpen] = useState(false);
  const [isShareOpen, setShareOpen] = useState(false);
  const [isNotificationPopupOpen, setNotificationPopupOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    setIsLoggedIn(!!sessionStorage.getItem('accessToken'));
  }, []);

  const getPollingInterval = (type: string): number => {
    switch (type) {
      case 'seconds':
        return 1000;
      case 'minutes':
        return 30000;
      case 'days':
        return 3600000;
      case 'weeks':
      case 'months':
      case 'years':
        return 86400000;
      default:
        return 1000;
    }
  };

  useEffect(() => {
    const fetchCandles = async () => {
      try {
        const unitQuery = candleType === 'minutes' && minuteUnit ? `&unit=${minuteUnit}` : '';
        const data = await fetchApi<CandleItem[]>(
          `/api/candle?market=${market}&candleType=${candleType}${unitQuery}`,
          { method: 'GET' },
        );
        setCandles(data);
      } catch (err) {
        console.error('캔들 데이터 호출 오류:', err);
      }
    };

    const interval = setInterval(fetchCandles, getPollingInterval(candleType));
    fetchCandles();
    return () => clearInterval(interval);
  }, [market, candleType, minuteUnit]);

  useEffect(() => {
    const fetchNews = async () => {
      try {
        const url = `/api/news?market=${encodeURIComponent(market)}`;
        const data = await fetchApi<NewsItem[]>(url, { method: 'GET' });
        setNews(data);
      } catch (err) {
        console.error('뉴스 데이터 호출 오류:', err);
      }
    };

    if (market) fetchNews();
  }, [market]);

  return (
    <div style={{ position: 'relative' }}>
      {/* 버튼 영역 (반응형) */}
      <div className="absolute top-4 right-4 z-10 mt-4">
        <div className="hidden md:flex gap-2">
          <button
            onClick={() =>
              isLoggedIn ? setNotificationPopupOpen(true) : alert('로그인이 필요합니다.')
            }
            className="px-3 py-2 rounded-md transition-colors bg-primary text-primary-foreground dark:text-black dark:bg-primary-dark hover:bg-primary/90"
          >
            알림 설정
          </button>
          <button
            onClick={() => setShareOpen(!isShareOpen)}
            className="px-3 py-2 rounded-md transition-colors bg-primary text-primary-foreground dark:text-black dark:bg-primary-dark hover:bg-primary/90"
          >
            공유하기
          </button>
          <button
            onClick={() => (isLoggedIn ? setChatPopupOpen(true) : alert('로그인이 필요합니다.'))}
            className="px-3 py-2 rounded-md transition-colors bg-primary text-primary-foreground dark:text-black dark:bg-primary-dark hover:bg-primary/90"
          >
            채팅방(회원 전용)
          </button>
        </div>

        <MobileHeaderMenu
          isLoggedIn={isLoggedIn}
          onChatClick={() => setChatPopupOpen(true)}
          onShareClick={() => setShareOpen(true)}
          onNotifyClick={() => setNotificationPopupOpen(true)}
        />
      </div>

      <Ticker market={market} ticker={ticker} />
      <div className="space-y-4 mt-4">
        <CandleChart
          candles={candles}
          candleType={candleType}
          setCandleType={setCandleType}
          minuteUnit={minuteUnit}
          setMinuteUnit={setMinuteUnit}
        />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <TradeList market={market} trade={trade} />
          <OrderbookList market={market} orderbook={orderbook} />
        </div>
        <div className="w-full">
          <NewsList news={news} />
        </div>
      </div>

      {isChatPopupOpen && <ChatPopup marketCode={market} onClose={() => setChatPopupOpen(false)} />}
      {isShareOpen && (
        <ShareModal onClose={() => setShareOpen(false)} market={market} ticker={ticker} />
      )}
      {isNotificationPopupOpen && (
        <NotificationPopup
          market={market}
          accessToken={sessionStorage.getItem('accessToken') ?? ''}
          onClose={() => setNotificationPopupOpen(false)}
        />
      )}
    </div>
  );
}

function MobileHeaderMenu({
  isLoggedIn,
  onChatClick,
  onShareClick,
  onNotifyClick,
}: {
  isLoggedIn: boolean;
  onChatClick: () => void;
  onShareClick: () => void;
  onNotifyClick: () => void;
}) {
  const [isOpen, setIsOpen] = useState(false);

  const handleClick = (action: () => void) => {
    if (!isLoggedIn) {
      alert('로그인이 필요합니다.');
      return;
    }
    setIsOpen(false);
    action();
  };

  return (
    <div className="flex md:hidden flex-col items-end relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="p-2 bg-primary text-primary-foreground rounded-md dark:text-black dark:bg-primary-dark"
      >
        {isOpen ? <X size={20} /> : <Menu size={20} />}
      </button>

      {isOpen && (
        <div
          tabIndex={0}
          onBlur={() => setIsOpen(false)}
          className="absolute right-0 mt-12 w-40 bg-primary text-primary-foreground dark:text-black dark:bg-primary-dark rounded-md border shadow-md focus:outline-none"
        >
          <button
            onClick={() => handleClick(onNotifyClick)}
            className="w-full px-4 py-3 text-left text-sm hover:bg-primary/90 border-b"
          >
            알림 설정
          </button>
          <button
            onClick={() => {
              setIsOpen(false);
              onShareClick();
            }}
            className="w-full px-4 py-3 text-left text-sm hover:bg-primary/90 border-b"
          >
            공유하기
          </button>
          <button
            onClick={() => handleClick(onChatClick)}
            className="w-full px-4 py-3 text-left text-sm hover:bg-primary/90"
          >
            채팅방(회원 전용)
          </button>
        </div>
      )}
    </div>
  );
}
