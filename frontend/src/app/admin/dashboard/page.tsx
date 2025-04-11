'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import RequireAuthenticated from '@/components/RequireAutenticated';
import { useAuth } from '@/context/AuthContext';

interface ChatMessageDto {
  id: string | null;
  sender: string; // 혹은 관리자 API에서 적절한 타입(예: 사용자 이름)
  content: string;
  timestamp: string;
}

interface ChatMessageReportDto {
  chatMessage: ChatMessageDto;
  reportCount: number;
}

export default function AdminDashboardPage() {
  const { accessToken, customFetch } = useAuth();
  const router = useRouter();
  const [reports, setReports] = useState<ChatMessageReportDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchReports() {
      try {
        const res = await customFetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/admin/reported-messages`,
          {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
          },
        );
        if (!res.ok) {
          throw new Error('신고된 메시지 조회에 실패했습니다.');
        }
        const data: ChatMessageReportDto[] = await res.json();
        setReports(data);
      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error(err);
          setError(err.message || '오류가 발생했습니다.');
        }
      } finally {
        setLoading(false);
      }
    }
    fetchReports();
  }, [accessToken, customFetch]);

  if (loading) {
    return (
      <RequireAuthenticated>
        <div className="w-full flex justify-center bg-background p-4">로딩 중...</div>
      </RequireAuthenticated>
    );
  }

  if (error) {
    return (
      <RequireAuthenticated>
        <div className="w-full flex justify-center bg-background p-4 text-red-500">{error}</div>
      </RequireAuthenticated>
    );
  }

  return (
    <RequireAuthenticated>
      <div className="mt-10 max-w-4xl mx-auto bg-card shadow-lg rounded-lg p-8">
        <h1 className="text-3xl font-bold text-center mb-6">신고된 메시지 목록</h1>
        {reports.length === 0 ? (
          <p className="text-center">신고된 메시지가 없습니다.</p>
        ) : (
          <table className="min-w-full border border-gray-300">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-4 py-2 border dark:text-black">작성자</th>
                <th className="px-4 py-2 border dark:text-black">내용</th>
                <th className="px-4 py-2 border dark:text-black">전송 시간</th>
              </tr>
            </thead>
            <tbody>
              {reports.map((report, index) => (
                <tr key={index}>
                  <td className="border px-4 py-2">{report.chatMessage.sender}</td>
                  <td className="border px-4 py-2">{report.chatMessage.content}</td>
                  <td className="border px-4 py-2">
                    {new Date(Number(report.chatMessage.timestamp)).toLocaleString('ko-KR', {
                      timeZone: 'Asia/Seoul',
                    })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <div className="mt-4 text-center ">
          <Button className="dark:text-black cursor-pointer" onClick={() => router.push('/')}>
            홈으로 이동
          </Button>
        </div>
      </div>
    </RequireAuthenticated>
  );
}
