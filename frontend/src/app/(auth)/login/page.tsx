"use client";

import { useCallback, useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Card, CardBody, CardHeader, Form, TextField } from "@repo/ui";
import { tokenStorage } from "@/lib/token-storage";
import type { ApiResponse, TokenResponse } from "@/lib/api-client";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";
const hasApiBaseUrl = API_BASE_URL.trim().length > 0;
const socialRouteBase = hasApiBaseUrl ? API_BASE_URL : "";

type LoginFormState = {
  identifier: string;
  password: string;
};

type AuthResponse = ApiResponse<TokenResponse>;

const socialProviders = [
  {
    name: "Google",
    description: "google.com",
    route: "/oauth2/authorization/google"
  },
  {
    name: "Kakao",
    description: "kakao.com",
    route: "/oauth2/authorization/kakao"
  }
];

export default function LoginPage() {
  const router = useRouter();
  const [formState, setFormState] = useState<LoginFormState>({ identifier: "", password: "" });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [socialError, setSocialError] = useState<string | null>(null);

  const handleSubmit = useCallback(
    async (event: React.FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      setSubmitting(true);
      setError(null);

      try {
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(formState)
        });

        const payload = (await response.json()) as AuthResponse;
        if (!response.ok || !payload.success || !payload.data) {
          throw new Error(payload.message ?? "로그인에 실패했습니다. 다시 시도해 주세요.");
        }

        tokenStorage.setTokens(payload.data.accessToken, payload.data.refreshToken);
        router.push("/");
      } catch (submitError) {
        setError(submitError instanceof Error ? submitError.message : "알 수 없는 오류입니다.");
      } finally {
        setSubmitting(false);
      }
    },
    [formState, router]
  );

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-100 via-white to-slate-200 px-6 py-12">
      <div className="mx-auto flex w-full max-w-4xl flex-col gap-8">
        <div className="space-y-2 text-center">
          <p className="inline-flex rounded-full bg-slate-200/60 px-3 py-1 text-xs font-medium text-slate-600">
            OAuth2 통합 인증 모듈
          </p>
          <h1 className="text-3xl font-semibold text-brand">다양한 방식으로 로그인하세요</h1>
          <p className="text-sm text-slate-500">
            일반 로그인부터 소셜 로그인까지 한 번에 제공하는 인증 경험을 준비했어요.
            {!hasApiBaseUrl ? (
              <span className="mt-2 block text-xs text-rose-500">NEXT_PUBLIC_API_BASE_URL 환경 변수가 비어 있어 소셜 로그인이 정상 작동하지 않을 수 있습니다.</span>
            ) : null}
          </p>
        </div>

        <Card className="grid gap-10 p-0 lg:grid-cols-5">
          <div className="surface-muted rounded-l-2xl border-r border-slate-100 p-6 lg:col-span-2">
            <div className="flex flex-col gap-4">
              <h2 className="text-xl font-semibold text-brand">소셜 계정으로 빠르게 시작</h2>
              <p className="text-sm text-slate-500">
                최초 로그인 시 필수 정보만 보충하면 곧바로 서비스를 이용할 수 있습니다.
              </p>
              <div className="flex flex-col gap-3">
                {socialError ? (
                  <p className="rounded-md bg-rose-50 px-3 py-2 text-xs text-rose-600">{socialError}</p>
                ) : null}
                {socialProviders.map((provider) => (
                  <Button
                    key={provider.name}
                    type="button"
                    variant="secondary"
                    onClick={() => {
                      if (!hasApiBaseUrl) {
                        setSocialError("NEXT_PUBLIC_API_BASE_URL을 설정해 백엔드 주소를 알려주세요.");
                        return;
                      }
                      setSocialError(null);
                      router.push(`${socialRouteBase}${provider.route}`);
                    }}
                    className="justify-start px-5"
                    disabled={!hasApiBaseUrl}
                  >
                    <span className="font-medium">{provider.name}</span>
                    <span className="ml-auto text-xs text-slate-500">{provider.description}</span>
                  </Button>
                ))}
              </div>
            </div>
          </div>

          <CardBody className="lg:col-span-3">
            <Form
              title="이메일 또는 닉네임으로 로그인"
              description="등록한 자격 증명을 입력해 주세요."
              onSubmit={handleSubmit}
              footer={
                <div className="flex flex-col gap-3">
                  <Button type="submit" loading={submitting}>
                    로그인
                  </Button>
                  <Button type="button" variant="outline" onClick={() => router.push("/register")}>
                    아직 계정이 없으신가요?
                  </Button>
                </div>
              }
            >
              <TextField
                label="이메일 또는 닉네임"
                name="identifier"
                value={formState.identifier}
                onChange={(event) =>
                  setFormState((prev) => ({ ...prev, identifier: event.target.value.trimStart() }))
                }
                required
              />
              <TextField
                label="비밀번호"
                name="password"
                type="password"
                autoComplete="current-password"
                value={formState.password}
                onChange={(event) =>
                  setFormState((prev) => ({ ...prev, password: event.target.value }))
                }
                required
              />
              {error ? <p className="rounded-md bg-rose-50 px-3 py-2 text-sm text-rose-600">{error}</p> : null}
            </Form>
          </CardBody>
        </Card>
      </div>
    </main>
  );
}
