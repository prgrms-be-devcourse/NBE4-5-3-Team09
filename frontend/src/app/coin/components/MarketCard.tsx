'use client';

import Link from 'next/link';
import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from '@/components/ui/alert-dialog';
import { MarketDto, TickerDto } from '@/types';
import { useAuth } from '@/context/AuthContext';

interface MarketCardProps {
  market: MarketDto;
  ticker?: TickerDto | null;
  onBookmarkToggle: (market: MarketDto) => void;
}

export default function MarketCard({ market, ticker, onBookmarkToggle }: MarketCardProps) {
  const { accessToken } = useAuth();
  const [showLoginAlert, setShowLoginAlert] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(market.isBookmarked); // 북마크 상태 추가

  const formatTradePrice = (tradePrice: number): string => {
    const decimalPlaces = tradePrice <= 1 ? 8 : tradePrice < 1000 ? 1 : 0;
    return new Intl.NumberFormat(undefined, {
      minimumFractionDigits: decimalPlaces,
      maximumFractionDigits: decimalPlaces,
    }).format(tradePrice);
  };

  const formatSignedChangeRate = (rate: number): string => {
    return `${rate >= 0 ? '+' : ''}${(rate * 100).toFixed(2)}%`;
  };

  const formatTradeVolume = (volume: number): string => {
    return new Intl.NumberFormat(undefined, {
      minimumFractionDigits: 3,
      maximumFractionDigits: 3,
    }).format(volume);
  };

  const handleBookmarkClick = async (e: React.MouseEvent) => {
    e.stopPropagation();

    if (!accessToken) {
      setShowLoginAlert(true);
      return;
    }

    // 먼저 색을 변경 (UI 업데이트)
    setIsBookmarked((prev) => !prev);

    // 서버로 북마크 요청 보내기
    try {
      await onBookmarkToggle(market); // onBookmarkToggle 함수는 실제 서버 요청을 처리함
    } catch {
      // 오류가 발생한 경우 색을 원래대로 되돌리기
      setIsBookmarked((prev) => !prev);
    }
  };

  return (
    <>
      <Card className="flex flex-col gap-4 bg-card rounded-sm border-0 cursor-pointer shadow-md hover:shadow-lg">
        <div className="relative flex flex-col h-full">
          <Link href={`/coin/${market.code}`} className="flex flex-col h-full">
            <CardHeader className="mb-4">
              <div className="flex flex-row flex-wrap">
                <CardTitle className="text-lg font-bold mr-2">{market.koreanName}</CardTitle>
                <div className="text-muted-foreground text-sm font-light self-end my-1">
                  {market.englishName}
                </div>
              </div>
              <div className="text-xs text-gray-400">({market.code})</div>
            </CardHeader>

            <CardContent className="mt-auto">
              <div className="flex flex-wrap justify-between items-end mt-1">
                <p
                  className={`text-xl font-semibold ${ticker ? (ticker.signedChangeRate >= 0 ? 'text-red-500' : 'text-blue-500') : ''}`}
                >
                  {ticker ? formatTradePrice(ticker.tradePrice) : '0'}
                  <span className="ml-1 text-xs">{market.code.split('-')[0]}</span>
                  {ticker ? ` (${(ticker.oneMinuteRate * 100).toFixed(2) + '%'})` : '-'}
                </p>

                <p
                  className={`text-sm ${ticker ? (ticker.signedChangeRate >= 0 ? 'text-red-500' : 'text-blue-500') : ''}`}
                >
                  {ticker?.signedChangeRate
                    ? formatSignedChangeRate(ticker.signedChangeRate)
                    : '0%'}
                </p>
              </div>

              <p className="text-xs text-muted-foreground mt-2">
                거래량 {ticker ? formatTradeVolume(ticker.accTradeVolume) : '0'}
                <span className="text-xs">{market.code.split('-')[1]}</span>
              </p>
            </CardContent>
          </Link>

          {/* Bookmark 버튼 */}
          <button
            className="cursor-pointer absolute top-1 right-5.5 text-gray-300 hover:text-gray-600 transition-colors"
            onClick={handleBookmarkClick}
          >
            <svg
              className="w-5 h-5"
              fill={isBookmarked ? '#FFCC33' : 'currentColor'} // 상태에 따라 색 변경
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"
              />
            </svg>
          </button>
        </div>
      </Card>

      {/* 로그인 안내 모달 */}
      <AlertDialog open={showLoginAlert} onOpenChange={setShowLoginAlert}>
        <AlertDialogContent className="p-8 bg-card shadow-sm max-w-md w-full">
          <AlertDialogHeader className="text-center">
            <AlertDialogTitle className="text-lg font-bold">로그인 안내</AlertDialogTitle>
            <AlertDialogDescription className="text-muted-foreground">
              해당 기능은 로그인 후 이용할 수 있습니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter className="flex justify-center space-x-2">
            <AlertDialogCancel className="cursor-pointer px-4 py-2 border rounded-md">
              취소
            </AlertDialogCancel>
            <AlertDialogAction asChild>
              <Link
                href="/user/login"
                className="px-4 py-2 bg-destructive hover:bg-destructive rounded text-white"
              >
                로그인 하러 가기
              </Link>
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
