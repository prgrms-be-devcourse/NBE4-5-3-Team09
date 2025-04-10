'use client';

import Script from 'next/script';

export default function KakaoScriptLoader() {
    return (
        <Script
            src="https://t1.kakaocdn.net/kakao_js_sdk/2.5.0/kakao.min.js"
            strategy="afterInteractive"
            onLoad={() => {
                if (window.Kakao && !window.Kakao.isInitialized()) {
                    window.Kakao.init(process.env.NEXT_PUBLIC_KAKAO_JAVASCRIPT_KEY);
                    console.log('✅ Kakao SDK initialized');
                }
            }}
        />
    );
}
