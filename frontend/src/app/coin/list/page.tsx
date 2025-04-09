'use client';

import { useEffect, useState } from 'react';
import WebSocketProvider from '@/context/WebSocketContext';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import ClientPage from './ClientPage';
import { MarketDto, PaginationDto } from '@/types';
import { useAuth } from '@/context/AuthContext';

import PaginationComponent from '@/components/Pagination';
import { useRouter, useSearchParams } from 'next/navigation';
import { subscribeUserToPush } from '@/app/api/notification/route';

export default function Page() {
  const [pagination, setPagination] = useState<PaginationDto>({
    page: 1,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });
  const [markets, setMarkets] = useState<MarketDto[]>([]);
  const [quote, setQuote] = useState('KRW');
  const [size, setSize] = useState(9);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { accessToken } = useAuth();
  const searchParams = useSearchParams();
  const router = useRouter();
  const initialPage = Number(searchParams.get('page')) || 1;
  const [page, setPage] = useState(initialPage);

  useEffect(() => {
    if (!accessToken) return;

    if (Notification.permission === 'granted') {
      subscribeUserToPush(accessToken);
    } else if (Notification.permission === 'default') {
      Notification.requestPermission().then((permission) => {
        if (permission === 'granted') {
          subscribeUserToPush(accessToken);
        }
      });
    }
  }, [accessToken]);

  async function fetchMarkets() {
    try {
      setLoading(true);
      setError(null);

      const headers: HeadersInit = {
        'Content-Type': 'application/json',
      };

      if (accessToken) {
        headers.Authorization = `Bearer ${accessToken}`;
      }

      const res = await fetch(
        process.env.NEXT_PUBLIC_API_URL + `/api/market?type=${quote}&page=${page - 1}&size=${size}`,
        {
          method: 'GET',
          headers,
          credentials: 'include',
        },
      );
      const data = await res.json();
      setMarkets(data.content);
      setPagination(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }
  // 기준 통화와 페이지 변경 시 시장 데이터를 fetch
  useEffect(() => {
    fetchMarkets();
  }, [quote, page, size]);

  // 페이지 변경 시 URL 업데이트
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    const params = new URLSearchParams(searchParams);
    params.set('page', newPage.toString());
    router.push(`?${params.toString()}`, { scroll: false });
  };

  // 기준 통화 변경 시 페이지 번호 초기화
  const handleQuoteChange = (newQuote: string) => {
    setQuote(newQuote);
    setPage(1);
  };

  if (loading) return <div className="p-6 flex justify-center items-center">로딩 중...</div>;
  if (error) return <p className="text-red-500">{error}</p>;
  if (!markets) return <p>No data found</p>;

  return (
    <WebSocketProvider subscriptions={[]}>
      <div className="max-w-7xl mx-auto p-6">
        <Tabs value={quote} onValueChange={handleQuoteChange}>
          <TabsList className="grid w-full grid-cols-3 bg-muted">
            <TabsTrigger className="cursor-pointer" value="KRW">
              KRW
            </TabsTrigger>
            <TabsTrigger className="cursor-pointer" value="BTC">
              BTC
            </TabsTrigger>
            <TabsTrigger className="cursor-pointer" value="USDT">
              USDT
            </TabsTrigger>
          </TabsList>
        </Tabs>
        <ClientPage markets={markets.slice(0, size)} />

        <div className="flex justify-center mt-6">
          <PaginationComponent
            currentPage={page}
            totalPages={pagination.totalPages ?? 1}
            maxPageButtons={5}
            onPageChange={handlePageChange}
            size={size}
            onSizeChange={(newSize) => setSize(newSize)}
            totalElements={pagination.totalElements ?? 0}
            pageSizeList={[9, 15, 21]}
          />
        </div>
      </div>
    </WebSocketProvider>
  );
}
