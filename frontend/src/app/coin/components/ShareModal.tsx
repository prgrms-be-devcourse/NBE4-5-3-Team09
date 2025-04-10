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
import { shareViaKakao } from '@/lib/kakao/share';
import { X, Share2, Link2 } from 'lucide-react'; // 아이콘을 위한 import
import { toast } from 'sonner';

export default function ShareDialog({ onClose, market }: { onClose: () => void; market: string }) {
    const currentUrl = typeof window !== 'undefined' ? window.location.href : '';
    const [open, setOpen] = useState(true); // 모달 상태 관리

    useEffect(() => {
        if (window.Kakao && !window.Kakao.isInitialized()) {
            window.Kakao.init(process.env.NEXT_PUBLIC_KAKAO_JS_KEY);
        }
    }, []);

    const handleKakaoShare = () => {
        const envTemplateId = process.env.NEXT_PUBLIC_KAKAO_MESSAGE_TEMPLATE_ID;
        const templateId = Number(envTemplateId ?? 0)
        const templateArgs = {
            title: '코잉',
            description: market,
        };
        shareViaKakao(templateId, templateArgs);
        setOpen(false);
        onClose();
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
            <Script src="https://developers.kakao.com/sdk/js/kakao.js" strategy="beforeInteractive" />
            <AlertDialog open={open} onOpenChange={(o) => {
                if (!o) handleClose();
            }}>
                <AlertDialogContent className="w-[15vw] max-w-xs px-6 py-4">
                    <AlertDialogHeader>
                        <div className="flex justify-between items-center">
                            <AlertDialogTitle>공유하기</AlertDialogTitle>
                            <button onClick={handleClose}>
                                <X className="w-4 h-4" />
                            </button>
                        </div>
                    </AlertDialogHeader>
                    <AlertDialogFooter className="flex flex-col items-center gap-3 mt-4">
                        <AlertDialogAction
                            onClick={handleKakaoShare}
                            className="p-0 max-w-sm bg-transparent border-none shadow-none hover:bg-transparent focus:outline-none flex items-center justify-center cursor-pointer"
                        >
                            <img
                                src="/kakao_logo.png"
                                alt="카카오로 공유"
                            />
                        </AlertDialogAction>
                        <AlertDialogAction
                            onClick={handleCopy}
                            className="flex items-center justify-center gap-2 max-w-sm bg-gray-100 text-gray-800 hover:bg-gray-200 rounded-md cursor-pointer"
                        >
                            <Link2 className="w-fit max-w-xs" />
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}
