export async function ensureServiceWorkerRegistered(): Promise<ServiceWorkerRegistration | null> {
  if (!('serviceWorker' in navigator)) {
    console.warn('서비스 워커를 지원하지 않는 브라우저입니다.');
    return null;
  }

  try {
    let registration = await navigator.serviceWorker.getRegistration();

    if (!registration) {
      registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
      console.log('서비스 워커 새로 등록됨:', registration);
    } else {
      console.log('기존 서비스 워커 사용:', registration);
    }

    // active 상태 될 때까지 기다리기
    if (!registration.active) {
      await waitForServiceWorkerActivation(registration);
    }

    return registration;
  } catch (error) {
    console.error('서비스 워커 등록 실패:', error);
    return null;
  }
}

async function waitForServiceWorkerActivation(reg: ServiceWorkerRegistration): Promise<void> {
  if (reg.active) return;

  return new Promise((resolve) => {
    const checkActive = () => {
      if (reg.active) {
        resolve();
      } else {
        setTimeout(checkActive, 100); // polling every 100ms
      }
    };
    checkActive();
  });
}
