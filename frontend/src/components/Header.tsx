'use client';

import { useAuth } from '@/context/AuthContext';
import NavLink from '@/components/NavLink';
import { usePathname, useRouter } from 'next/navigation';
import { useState } from 'react';
import Link from 'next/link';
import { Coins, Bookmark, CircleUser } from 'lucide-react';
import { Button } from './ui/button';
import { useUserStore } from '@/store/user.store';
import { ModeToggle } from './ThemeToggle';

const navItems = [
  { name: '코인 대시보드', href: '/coin/list', icon: Coins },
  { name: '북마크 대시보드', href: '/bookmark', icon: Bookmark },
];

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { accessToken, isAuthLoading, setAccessToken } = useAuth();
  const { user, deleteUser } = useUserStore();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const isLoggedIn = !!accessToken;

  const handleLogout = async () => {
    if (!accessToken) return;
    setIsLoggingOut(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/logout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: 'include',
      });
      if (res.ok) {
        setAccessToken(null);
        deleteUser();
        router.push('/user/login');
      } else {
        const errorData = await res.json();
        alert(`로그아웃 실패: ${errorData.message || ''}`);
      }
    } catch (err) {
      console.error('로그아웃 오류:', err);
      alert('로그아웃 중 오류가 발생했습니다.');
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <header className="bg-card border-b border-muted">
      <div className="container mx-auto px-4 py-4 flex justify-between items-center">
        <div className="flex flex-col md:flex-row md:items-center gap-y-2 md:gap-x-4">
          <Link href="/">
            <div className="flex items-center">
              <img src="/logo.svg" alt="Coing Logo" className="h-8 mr-2" />
              <span className="text-xl font-bold text-secondary">Coing</span>
            </div>
          </Link>
          {/* 기본 네비게이션 */}
          <nav className="flex flex-col md:flex-row md:space-x-4 gap-y-2">
            {navItems.map((item) => (
              <NavLink key={item.href} href={item.href}>
                <item.icon className="w-4" />
                {item.name}
              </NavLink>
            ))}
          </nav>
        </div>

        <div className="flex items-center space-x-4">
          {/* 관리자 권한이 있을 때 관리 페이지 버튼 추가 */}
          {user?.authority === 'ROLE_ADMIN' && (
            <Button
              variant="outline"
              onClick={() => router.push('/admin/dashboard')}
              className="mr-2 cursor-pointer"
            >
              관리 페이지
            </Button>
          )}

          <Link
            href="/user/info"
            className="flex items-center gap-1 text-sm font-medium text-secondary hover:text-card-foreground cursor-pointer"
          >
            <CircleUser />
            {user?.name || '익명'}
          </Link>
          <div style={{ minHeight: '36px' }}>
            {pathname !== '/user/login' && (
              <Button
                className="cursor-pointer text-card"
                onClick={isLoggedIn ? handleLogout : () => router.push('/user/login')}
                disabled={isAuthLoading || isLoggingOut}
              >
                {isAuthLoading
                  ? '로딩중...'
                  : isLoggedIn
                    ? isLoggingOut
                      ? '로그아웃 중...'
                      : '로그아웃'
                    : '로그인'}
              </Button>
            )}
          </div>
          <ModeToggle />
        </div>
      </div>
    </header>
  );
}
