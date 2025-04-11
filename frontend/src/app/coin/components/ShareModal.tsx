'use client';

import { useEffect, useState } from 'react';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogContent,
    AlertDialogFooter,
    AlertDialogTitle,
    AlertDialogHeader
} from '@/components/ui/alert-dialog';
import Script from 'next/script';
import { X, Link2 } from 'lucide-react';
import { toast } from 'sonner';
import type { TickerDto } from '@/types';

declare global {
    interface Kakao {
        init: (key: string) => void;
        isInitialized: () => boolean;
        Share: {
            sendCustom: (config: {
                templateId: number;
                templateArgs: {
                    title: string;
                    description: string;
                    path: string;
                }
            }) => void;
        };
    }

    interface Window {
        Kakao: Kakao;
    }
}

export default function ShareModal({ onClose, market, ticker }: { onClose: () => void; market: string; ticker: TickerDto | null }) {
    const [open, setOpen] = useState(true);
    const currentUrl = typeof window !== 'undefined' ? window.location.href : '';
    const templateIdStr = process.env.NEXT_PUBLIC_KAKAO_MESSAGE_TEMPLATE_ID;
    const templateId = templateIdStr !== undefined ? Number(templateIdStr) : 0;

    useEffect(() => {
        if (window.Kakao && !window.Kakao.isInitialized()) {
            window.Kakao.init(process.env.NEXT_PUBLIC_KAKAO_JS_KEY!);
        }
    }, []);

    const handleKakaoShare = () => {
        if (!window.Kakao?.Share) return;

        const marketName = ticker
            ? `${ticker.koreanName} ${market}`
            : market;

        const price = ticker
            ? `${ticker.tradePrice.toLocaleString()} ${ticker.code.split('-')[0]}`
            : '가격 정보 없음';

        const rate = ticker
            ? (ticker.signedChangeRate * 100).toFixed(2) + '%'
            : '정보 없음';

        window.Kakao.Share.sendCustom({
            templateId: templateId,
            templateArgs: {
                title: marketName,
                description: `현재가 ${price}\n변동률 ${rate}`,
                path: '/coin/' + market,
            },
        });
    };

    const handleCopy = async () => {
        await navigator.clipboard.writeText(currentUrl);
        toast.success('링크가 복사되었습니다');
        setOpen(false);
        onClose();
    };

    const handleClose = () => {
        setOpen(false);
        onClose();
    };

    return (
        <>
            <Script
                src="https://t1.kakaocdn.net/kakao_js_sdk/2.7.5/kakao.min.js"
                integrity="sha384-dok87au0gKqJdxs7msEdBPNnKSRT+/mhTVzq+qOhcL464zXwvcrpjeWvyj1kCdq6"
                crossOrigin="anonymous"
            />
            <AlertDialog open={open} onOpenChange={(o) => !o && handleClose()}>
                <AlertDialogContent className="w-[20vw] max-w-xs px-6 py-4">
                    <AlertDialogHeader>
                        <div className="flex justify-between items-center">
                            <AlertDialogTitle>공유하기</AlertDialogTitle>
                            <button onClick={handleClose}>
                                <X className="w-4 h-4" />
                            </button>
                        </div>
                    </AlertDialogHeader>
                    <AlertDialogFooter className="flex flex-col items-center gap-3 mt-4">
                        <div className="flex justify-center">
                            <button onClick={handleKakaoShare}>
                                <img
                                    src="https://developers.kakao.com/assets/img/about/logos/kakaotalksharing/kakaotalk_sharing_btn_medium.png"
                                    alt="카카오톡 공유 보내기 버튼"
                                    className="w-9.5 h-9.5 cursor-pointer"
                                />
                            </button>
                        </div>
                        <AlertDialogAction
                            onClick={handleCopy}
                            className="flex items-center justify-center gap-2 max-w-sm bg-gray-100 text-gray-800 hover:bg-gray-200 rounded-md cursor-pointer"
                        >
                            <Link2 className="w-4 h-4" />
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}
