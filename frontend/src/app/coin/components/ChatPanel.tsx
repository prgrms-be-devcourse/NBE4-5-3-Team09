'use client';

import React, { useEffect, useState, useRef, KeyboardEvent } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { Input } from '@/components/ui/input';

interface ChatMessage {
  id?: number;
  sender: string;
  content: string;
  timestamp: string;
}

interface ChatPanelProps {
  marketCode: string;
}

const ChatPanel: React.FC<ChatPanelProps> = ({ marketCode }) => {
  const { chatMessages, updateSubscriptions, publishMessage } = useWebSocket();
  const [input, setInput] = useState('');
  const [isComposing, setIsComposing] = useState(false);
  const [cachedMessages, setCachedMessages] = useState<ChatMessage[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    updateSubscriptions([{ type: 'chat', markets: [marketCode] }]);
  }, [marketCode]);

  useEffect(() => {
    const loadCachedMessages = async () => {
      try {
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/chat/rooms/${marketCode}/messages`
        );
        if (res.ok) {
          const data = await res.json();
          setCachedMessages(data);
        }
      } catch (error) {
        console.error('채팅 기록 불러오기 오류:', error);
      }
    };
    loadCachedMessages();
  }, [marketCode]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages[marketCode], cachedMessages]);

  const sendMessage = () => {
    if (!input.trim()) return;
    const token = sessionStorage.getItem('accessToken');
    const message: ChatMessage = {
      content: input,
      sender: '',
      timestamp: '',
    };
    publishMessage(
      `/app/chat/${marketCode}`,
      JSON.stringify(message),
      token ? { Authorization: `Bearer ${token}` } : {}
    );
    setInput('');
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') sendMessage();
  };

  const handleReport = async (msg: ChatMessage) => {
    if (!msg.id) return alert('메시지 식별자가 없습니다.');
    const token = sessionStorage.getItem('accessToken');
    if (!token) return alert('로그인이 필요합니다.');
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/chat/messages/${msg.id}/report`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      }
    );
    if (res.ok) alert('신고 완료');
    else alert('신고 실패');
  };

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
      <div className="flex-1 overflow-y-auto p-2 border-b border-border">
        {allMessages.map((msg, index) => (
          <div key={index} className="mb-2">
            <strong>{msg.sender}:</strong> {msg.content}
            <div className="text-xs text-muted-foreground">
              {msg.timestamp
                ? new Date(parseInt(msg.timestamp)).toLocaleTimeString()
                : ''}
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
      <div className="flex items-center p-2 border-t border-border">
        <Input
          type="text"
          placeholder="메시지 입력"
          value={input}
          onChange={(e) => {
            setInput(e.target.value); // 항상 업데이트
          }}
          onCompositionStart={() => setIsComposing(true)}
          onCompositionEnd={() => setIsComposing(false)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !isComposing) {
              sendMessage();
            }
          }}
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
