// lib/kakao/share.ts

/**
 * Kakao 커스텀 템플릿 공유 함수
 * @param templateId Kakao Developers에서 생성한 메시지 템플릿 ID
 * @param templateArgs 템플릿 안에 #{key} 형태로 들어갈 값들을 객체 형태로 전달
 */
export const shareViaKakao = (templateId: number, templateArgs?: Record<string, string>) => {
    if (typeof window === 'undefined') {
        console.warn('Kakao 공유는 브라우저 환경에서만 사용할 수 있습니다.');
        return;
    }

    if (!window.Kakao) {
        console.error('Kakao SDK가 로드되지 않았습니다.');
        return;
    }

    if (!window.Kakao.isInitialized()) {
        console.error('Kakao SDK가 초기화되지 않았습니다.');
        return;
    }

    try {
        window.Kakao.Link.sendCustom({
            templateId,
            templateArgs,
        });
    } catch (error) {
        console.error('카카오 공유 중 오류 발생:', error);
    }
};
