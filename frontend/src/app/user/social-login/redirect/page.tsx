'use client';

import { useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { toast } from 'sonner';

export default function SocialLoginRedirectPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const hasFetched = useRef(false);

  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;

    const tempToken = searchParams.get('tempToken');
    const quitToken = searchParams.get('quitToken');

    if (tempToken) {
      // 🔐 로그인 로직
      const fetchAccessToken = async () => {
        try {
          const response = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/auth/social-login/redirect?tempToken=${tempToken}`,
            {
              method: 'POST',
              credentials: 'include',
            },
          );

          if (response.ok) {
            const data = await response.json();
            console.log('소셜 로그인 성공:', data);

            const authHeader = response.headers.get('Authorization');
            if (authHeader?.startsWith('Bearer ')) {
              const token = authHeader.slice(7);
              console.log('Access Token 저장됨:', token);
            }
            router.push('/');
          } else {
            const errorData = await response.json();
            alert(`로그인 실패: ${errorData.message}`);
            router.push('/');
          }
        } catch (error) {
          console.error('Login error:', error);
          alert('로그인 중 오류가 발생했습니다.');
          router.push('/');
        }
      };

      fetchAccessToken();
    } else if (quitToken) {
      // ❌ 탈퇴 로직
      const withdraw = async () => {
        try {
          const response = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/api/auth/social-login/redirect/quit?quitToken=${quitToken}`,
            {
              method: 'POST',
              credentials: 'include',
            },
          );

          if (response.ok) {
            toast.success('회원 탈퇴 성공');
            window.location.href = '/user/login';
          } else {
            const errorData = await response.json();
            alert(`회원 탈퇴 실패: ${errorData.message}`);
          }
        } catch (error) {
          console.error('Withdraw error:', error);
          alert('탈퇴 중 오류가 발생했습니다.');
        }
      };

      withdraw();
    } else {
      alert('유효하지 않은 요청입니다.');
      router.push('/');
    }
  }, [searchParams, router]);

  return <div>처리 중...</div>;
}
