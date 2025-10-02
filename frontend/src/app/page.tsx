"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { Card, CardBody, CardHeader, Button } from "@repo/ui";
import { apiFetch, clearAuthTokens, type ApiResponse } from "@/lib/api-client";
import { tokenStorage } from "@/lib/token-storage";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

type HomePageProps = {
  searchParams?: Record<string, string | string[] | undefined>;
};

type UserProfile = {
  id: number;
  email: string;
  name: string;
  nickname: string;
  gender: string;
  birthday: string;
  introduce: string;
  profileCompleted: boolean;
};

const baseActions = [
  {
    href: "/login",
    label: "로그인",
    description: "기존 계정으로 바로 접속하세요.",
    style: "bg-brand text-brand-contrast hover:bg-brand-light"
  },
  {
    href: "/register",
    label: "회원가입",
    description: "필수 정보를 입력하고 계정을 생성합니다.",
    style: "bg-white text-brand border border-slate-200 hover:border-brand/40"
  }
];

export default function HomePage({ searchParams }: HomePageProps) {
  const profileParam = searchParams?.profile;
  const [user, setUser] = useState<UserProfile | null>(null);
  const [profileFetchError, setProfileFetchError] = useState<string | null>(null);
  const [loadingProfile, setLoadingProfile] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    const fetchProfile = async () => {
      const accessToken = tokenStorage.getAccessToken();
      if (!API_BASE_URL || !accessToken) {
        setLoadingProfile(false);
        setUser(null);
        return;
      }

      try {
        const response = await apiFetch("/api/auth/me", { signal: controller.signal });
        const payload = (await response.json()) as ApiResponse<UserProfile | null>;

        if (response.ok && payload.success && payload.data) {
          setUser(payload.data);
          setProfileFetchError(null);
        } else if (response.status === 401) {
          setUser(null);
          setProfileFetchError(null);
        } else {
          setUser(null);
          setProfileFetchError(payload.message ?? "프로필 정보를 불러오지 못했습니다.");
        }
      } catch (error) {
        if (!(error instanceof DOMException && error.name === "AbortError")) {
          setProfileFetchError("프로필 정보를 불러오지 못했습니다.");
        }
        setUser(null);
      } finally {
        setLoadingProfile(false);
      }
    };

    void fetchProfile();

    return () => controller.abort();
  }, []);

  const needsProfileFromQuery = useMemo(() => {
    if (Array.isArray(profileParam)) {
      return profileParam.includes("needs");
    }
    return profileParam === "needs";
  }, [profileParam]);

  const needsProfileBanner = needsProfileFromQuery || (!!user && !user.profileCompleted);
  const actions = user ? [] : baseActions;

  const handleLogout = async () => {
    const refreshToken = tokenStorage.getRefreshToken();
    if (!refreshToken) {
      tokenStorage.clearTokens();
      setUser(null);
      return;
    }

    try {
      await apiFetch("/api/auth/logout", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken })
      });
    } finally {
      clearAuthTokens();
      setUser(null);
    }
  };

  const handleRefreshProfile = async () => {
    if (!tokenStorage.getAccessToken()) {
      setProfileFetchError("로그인이 필요합니다.");
      return;
    }
    setRefreshing(true);
    try {
      const response = await apiFetch("/api/auth/me");
      const payload = (await response.json()) as ApiResponse<UserProfile | null>;
      if (response.ok && payload.success && payload.data) {
        setUser(payload.data);
        setProfileFetchError(null);
      } else if (response.status === 401) {
        setUser(null);
        setProfileFetchError(null);
      } else {
        setProfileFetchError(payload.message ?? "프로필 정보를 불러오지 못했습니다.");
      }
    } finally {
      setRefreshing(false);
    }
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-100 via-white to-slate-200 px-6 py-16">
      <div className="mx-auto flex w-full max-w-5xl flex-col gap-12">
        {needsProfileBanner ? (
          <div className="rounded-2xl border border-amber-200 bg-amber-50 px-6 py-4 text-sm text-amber-800 shadow-soft">
            <p className="font-semibold">프로필을 완성해 주세요!</p>
            <p className="mt-1">
              기본 계정은 생성되었습니다. 나중에 프로필 페이지에서 자기소개 등 추가 정보를 채우면 더 좋은 경험을 제공해 드릴 수 있어요.
            </p>
          </div>
        ) : null}

        <section className="text-center">
          <p className="inline-flex rounded-full bg-slate-200/80 px-3 py-1 text-xs font-medium text-slate-600">
            OAuth2 인증 플랫폼
          </p>
          <h1 className="mt-4 text-4xl font-semibold tracking-tight text-brand">
            소셜과 일반 로그인을 모두 품은 통합 인증 모듈
          </h1>
          <p className="mt-3 text-base text-slate-500">
            Next.js 프론트엔드와 Spring Boot 백엔드로 구성된 모듈형 구조로,
            다양한 인증 시나리오를 빠르게 확장할 수 있습니다.
          </p>
        </section>

        {actions.length > 0 ? (
          <section className="grid gap-6 md:grid-cols-2">
            {actions.map((action) => (
              <Link key={action.href} href={action.href} className="transition-transform duration-200 hover:-translate-y-1">
                <Card className="h-full">
                  <CardBody className="flex h-full flex-col gap-3">
                    <span className={`inline-flex w-fit rounded-full px-3 py-1 text-xs font-medium ${action.style}`}>
                      {action.label}
                    </span>
                    <h2 className="text-xl font-semibold text-brand">{action.label}</h2>
                    <p className="text-sm text-slate-500">{action.description}</p>
                  </CardBody>
                </Card>
              </Link>
            ))}
          </section>
        ) : null}

        {user ? (
          <Card>
            <CardHeader
              title="내 프로필"
              subtitle={user.profileCompleted ? "환영합니다!" : "프로필을 완성하면 더 많은 기능을 사용할 수 있어요."}
            />
            <CardBody className="flex flex-col gap-6">
              <div className="grid gap-4 md:grid-cols-2">
                <ProfileField label="이메일" value={user.email} important />
                <ProfileField label="닉네임" value={user.nickname} />
                <ProfileField label="이름" value={user.name} />
                <ProfileField label="성별" value={user.gender} />
                <ProfileField label="생년월일" value={new Date(user.birthday).toLocaleDateString()} />
                <ProfileField label="자기소개" value={user.introduce || "아직 입력하지 않았어요."} fullWidth />
              </div>
              <div className="flex flex-wrap gap-3">
                <Button variant="secondary" onClick={handleRefreshProfile} disabled={refreshing}>
                  {refreshing ? "새로고침 중..." : "프로필 새로고침"}
                </Button>
                <Button variant="outline" onClick={handleLogout}>로그아웃</Button>
              </div>
            </CardBody>
          </Card>
        ) : null}

        {!loadingProfile && !user && profileFetchError ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 px-6 py-4 text-sm text-rose-700 shadow-soft">
            {profileFetchError}
          </div>
        ) : null}

        <Card>
          <CardHeader
            title="모듈형 아키텍처"
            subtitle="공통 UI 패키지와 확장 가능한 백엔드로 새로운 애플리케이션도 손쉽게 추가할 수 있습니다."
          />
          <CardBody className="grid gap-4 md:grid-cols-3">
            <div className="rounded-lg border border-slate-200 bg-slate-50/80 p-4">
              <h3 className="text-base font-semibold text-brand">Turborepo 기반</h3>
              <p className="mt-2 text-sm text-slate-500">
                효율적인 캐시와 파이프라인 설정으로 다중 앱과 패키지를 빠르게 관리합니다.
              </p>
            </div>
            <div className="rounded-lg border border-slate-200 bg-slate-50/80 p-4">
              <h3 className="text-base font-semibold text-brand">재사용 가능한 UI</h3>
              <p className="mt-2 text-sm text-slate-500">
                Tailwind 기반의 UI 컴포넌트를 별도 패키지로 분리해 어느 앱에서나 사용 가능합니다.
              </p>
            </div>
            <div className="rounded-lg border border-slate-200 bg-slate-50/80 p-4">
              <h3 className="text-base font-semibold text-brand">Spring Security + OAuth2</h3>
              <p className="mt-2 text-sm text-slate-500">
                소셜 계정으로 바로 가입하고, 이후 프로필을 완성해 더 나은 경험을 제공합니다.
              </p>
            </div>
          </CardBody>
        </Card>
      </div>
    </main>
  );
}

type ProfileFieldProps = {
  label: string;
  value: string;
  important?: boolean;
  fullWidth?: boolean;
};

function ProfileField({ label, value, important = false, fullWidth = false }: ProfileFieldProps) {
  return (
    <div className={`flex flex-col gap-1 ${fullWidth ? "md:col-span-2" : ""}`}>
      <span className={`text-xs font-medium ${important ? "text-brand" : "text-slate-500"}`}>{label}</span>
      <span className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 shadow-sm">
        {value}
      </span>
    </div>
  );
}
