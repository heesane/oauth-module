"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { tokenStorage } from "@/lib/token-storage";

function parseHash(hash: string) {
  const params = new URLSearchParams(hash.startsWith("#") ? hash.substring(1) : hash);
  return {
    accessToken: params.get("access_token"),
    refreshToken: params.get("refresh_token")
  };
}

export default function OAuthCallbackPage() {
  const router = useRouter();

  useEffect(() => {
    const { accessToken, refreshToken } = parseHash(window.location.hash);

    if (accessToken && refreshToken) {
      tokenStorage.setTokens(accessToken, refreshToken);
      router.replace("/?profile=needs");
    } else {
      router.replace("/login");
    }
  }, [router]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100">
      <div className="rounded-lg bg-white px-6 py-4 shadow-soft">
        <p className="text-sm text-slate-600">소셜 로그인 처리 중입니다...</p>
      </div>
    </main>
  );
}
