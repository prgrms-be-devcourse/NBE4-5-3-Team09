'use client';

import React, { useEffect, useState, useRef, KeyboardEvent } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { Input } from '@/components/ui/input';

interface ChatMessage {
  // 메시지의 DB 식별자 (백엔드에서 내려줘야 함)
  id?: number;
  sender: string;
  content: string;
  timestamp: string; // millisecond 단위의 timestamp
}

interface ChatPanelProps {
  marketCode: string;
}

const ChatPanel: React.FC<ChatPanelProps> = ({ marketCode }) => {
  const { chatMessages, updateSubscriptions, publishMessage } = useWebSocket();
  const [input, setInput] = useState<string>('');
  const [cachedMessages, setCachedMessages] = useState<ChatMessage[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // marketCode가 바뀔 때마다 채팅 구독 업데이트
  useEffect(() => {
    updateSubscriptions([{ type: 'chat', markets: [marketCode] }]);
  }, [marketCode]);

  // (1) 초기 마운트 시 + marketCode 변경 시, 백엔드에서 캐시된 메시지 로드
  useEffect(() => {
    const loadCachedMessages = async () => {
      try {
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/chat/rooms/${marketCode}/messages`
        );
        if (res.ok) {
          const data = await res.json();
          setCachedMessages(data);
        } else {
          console.error('캐시된 채팅 기록 불러오기 실패');
        }
      } catch (error) {
        console.error('채팅 기록 불러오기 오류:', error);
      }
    };
    loadCachedMessages();
  }, [marketCode]);

  // (2) chatMessages나 cachedMessages 변경될 때, 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages[marketCode], cachedMessages]);

  // (3) 메시지 전송 로직
  const sendMessage = () => {
    if (!input.trim()) return;

    const token = sessionStorage.getItem('accessToken');
    const message: ChatMessage = {
      // 백엔드가 sender, timestamp 세팅하므로 여기서는 내용만.
      content: input,
      sender: '',
      timestamp: '',
    };

    // 토큰이 있다면 Authorization 헤더에 넣어 전송
    publishMessage(
      `/app/chat/${marketCode}`,
      JSON.stringify(message),
      token ? { Authorization: `Bearer ${token}` } : {}
    );
    setInput('');
  };

  // (4) Enter 키로 전송
  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  // (5) 신고 버튼 클릭 시 API 호출
  const handleReport = async (msg: ChatMessage) => {
    // messageId가 없으면 신고 불가
    if (!msg.id) {
      alert('메시지 식별자가 없습니다. 신고할 수 없습니다.');
      return;
    }

    try {
      // 로그인 토큰 (예: sessionStorage에 저장된 accessToken)
      const token = sessionStorage.getItem('accessToken');
      if (!token) {
        alert('로그인이 필요합니다.');
        return;
      }

      const reportUrl = `${process.env.NEXT_PUBLIC_API_URL}/api/chat/messages/${msg.id}/report`;
      const res = await fetch(reportUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      });

      if (res.ok) {
        alert('신고가 접수되었습니다.');
      } else {
        const data = await res.json().catch(() => null);
        alert(`신고 실패: ${data?.message || '원인을 알 수 없습니다.'}`);
      }
    } catch (error) {
      console.error('신고 API 호출 오류:', error);
      alert('신고에 실패했습니다.');
    }
  };

  // (6) 실시간 메시지와 캐시된 메시지를 합치되, 중복 제거
  const realtimeMessages = chatMessages[marketCode] || [];
  const allMessages = Array.from(
    new Map(
      [...cachedMessages, ...realtimeMessages].map((m) => [
        `${m.timestamp}-${m.content}`,
        m,
      ])
    ).values()
  );

  return (
    <div className="flex flex-col h-full">
      {/* 메시지 표시 영역 */}
      <div className="flex-1 overflow-y-auto p-2 border-b border-border">
        {allMessages.map((msg, index) => (
          <div key={index} className="mb-2">
            <strong>{msg.sender}:</strong> {msg.content}
            <div className="text-xs text-muted-foreground">
              {/* 메시지 발송 시각 */}
              {msg.timestamp
                ? new Date(parseInt(msg.timestamp)).toLocaleTimeString()
                : ''}

              {/* 신고 버튼: timestamp 옆 */}
              <button
                onClick={() => handleReport(msg)}
                className="ml-2 text-red-500 underline"
              >
                신고
              </button>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* 입력창 + 전송 버튼 */}
      <div className="flex items-center p-2 border-t border-border">
        <Input
          type="text"
          placeholder="메시지 입력"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          className="flex-1 mr-2"
        />
        <button
          onClick={sendMessage}
          className="bg-primary text-primary-foreground rounded-md px-4 py-2 transition-colors hover:bg-primary/90 dark:text-black dark:bg-primary-dark"
        >
          전송
        </button>
      </div>
    </div>
  );
};

export default ChatPanel;
