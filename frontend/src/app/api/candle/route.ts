import { NextRequest, NextResponse } from 'next/server';
import { client } from '@/lib/api/client';
import { CandleChartDto, CandleItem } from '@/types';

export async function GET(request: NextRequest): Promise<NextResponse> {
  const { searchParams } = new URL(request.url);
  const minuteUnit = searchParams.get('unit') || '';
  const market = searchParams.get('market') || 'KRW-BTC';
  const type = searchParams.get('candleType') || 'seconds';

  const query: Record<string, string> = {};
  if (type === 'minutes' && minuteUnit) {
    query.unit = minuteUnit;
  }

  const enumType = type as 'seconds' | 'minutes' | 'days' | 'weeks' | 'months' | 'years';
  try {
    const response = await client.GET('/api/candles/{market}/{type}', {
      params: {
        path: {
          market,
          type: enumType,
        },
        query,
      },
    });

    if (response.error) {
      return NextResponse.json({ error: response['error'] }, { status: 400 });
    }

    const data = response.data as CandleChartDto[];
    const mapped: CandleItem[] = data.map((dto) => ({
      time: new Date(dto.candleDateTimeUtc).getTime(),
      open: dto.openingPrice,
      high: dto.highPrice,
      low: dto.lowPrice,
      close: dto.tradePrice,
      volume: dto.candleAccTradeVolume,
    }));

    return NextResponse.json(mapped);
  } catch (error) {
    console.error('캔들 데이터 호출 오류:', error);
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    return NextResponse.json({ error: errorMessage || '캔들 데이터 호출 오류' }, { status: 500 });
  }
}
