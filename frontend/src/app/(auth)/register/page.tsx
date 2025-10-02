"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Card, CardBody, CardHeader, Form, TextField } from "@repo/ui";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

type ApiResponse<T> = {
  success: boolean;
  message?: string;
  data: T;
};

type RegisterFormState = {
  email: string;
  name: string;
  nickname: string;
  password: string;
  confirmPassword: string;
  gender: string;
  birthday: string;
  introduce: string;
};

export default function RegisterPage() {
  const router = useRouter();
  const [formState, setFormState] = useState<RegisterFormState>({
    email: "",
    name: "",
    nickname: "",
    password: "",
    confirmPassword: "",
    gender: "",
    birthday: "",
    introduce: ""
  });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (formState.password !== formState.confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    setError(null);
    setSubmitting(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          email: formState.email,
          name: formState.name,
          nickname: formState.nickname,
          password: formState.password,
          gender: formState.gender,
          birthday: formState.birthday,
          introduce: formState.introduce
        })
      });

      const payload = (await response.json()) as ApiResponse<number | null>;
      if (!response.ok || !payload.success) {
        throw new Error(payload.message ?? "회원가입에 실패했습니다.");
      }

      router.push("/login");
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "알 수 없는 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-100 via-white to-slate-200 px-6 py-12">
      <div className="mx-auto flex w-full max-w-5xl flex-col gap-10">
        <div className="text-center">
          <p className="inline-flex rounded-full bg-slate-200/60 px-3 py-1 text-xs font-medium text-slate-600">
            신규 회원가입
          </p>
          <h1 className="mt-3 text-3xl font-semibold text-brand">필수 정보를 채워 계정을 만들어보세요</h1>
          <p className="mt-2 text-sm text-slate-500">
            모든 정보는 서비스 맞춤 경험을 위해 안전하게 사용됩니다.
          </p>
        </div>

        <Card className="w-full">
          <CardHeader
            title="기본 정보 입력"
            subtitle="정확한 정보를 입력할수록 더 개인화된 경험을 제공해 드릴 수 있습니다."
          />
          <CardBody>
            <Form onSubmit={handleSubmit}>
              <div className="grid gap-5 lg:grid-cols-2">
                <TextField
                  label="이메일"
                  name="email"
                  type="email"
                  value={formState.email}
                  onChange={(event) =>
                    setFormState((prev) => ({ ...prev, email: event.target.value.trim() }))
                  }
                  required
                />
                <TextField
                  label="닉네임"
                  name="nickname"
                  value={formState.nickname}
                  onChange={(event) =>
                    setFormState((prev) => ({ ...prev, nickname: event.target.value }))
                  }
                  required
                />
              </div>
              <div className="grid gap-5 lg:grid-cols-2">
                <TextField
                  label="이름"
                  name="name"
                  value={formState.name}
                  onChange={(event) =>
                    setFormState((prev) => ({ ...prev, name: event.target.value }))
                  }
                  required
                />
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-slate-700" htmlFor="gender">
                    성별
                  </label>
                  <select
                    id="gender"
                    name="gender"
                    className="w-full rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm shadow-sm transition focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/30"
                    value={formState.gender}
                    onChange={(event) =>
                      setFormState((prev) => ({ ...prev, gender: event.target.value }))
                    }
                    required
                  >
                    <option value="" disabled>
                      선택해 주세요
                    </option>
                    <option value="MALE">남성</option>
                    <option value="FEMALE">여성</option>
                    <option value="OTHER">기타</option>
                  </select>
                </div>
              </div>
              <div className="grid gap-5 lg:grid-cols-2">
                <TextField
                  label="비밀번호"
                  name="password"
                  type="password"
                  value={formState.password}
                  onChange={(event) =>
                    setFormState((prev) => ({ ...prev, password: event.target.value }))
                  }
                  required
                />
                <TextField
                  label="비밀번호 확인"
                  name="confirmPassword"
                  type="password"
                  value={formState.confirmPassword}
                  onChange={(event) =>
                    setFormState((prev) => ({ ...prev, confirmPassword: event.target.value }))
                  }
                  required
                />
              </div>
              <div className="grid gap-5 lg:grid-cols-2">
                <TextField
                  label="생년월일"
                  name="birthday"
                  type="date"
                  value={formState.birthday}
                  onChange={(event) =>
                    setFormState((prev) => ({ ...prev, birthday: event.target.value }))
                  }
                  required
                />
                <div className="flex flex-col gap-1 lg:col-span-1">
                  <label className="text-sm font-medium text-slate-700" htmlFor="introduce">
                    자기소개
                  </label>
                  <textarea
                    id="introduce"
                    name="introduce"
                    className="min-h-[140px] rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm shadow-sm transition focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/30"
                    value={formState.introduce}
                    onChange={(event) =>
                      setFormState((prev) => ({ ...prev, introduce: event.target.value }))
                    }
                    required
                  />
                </div>
              </div>
              {error ? <p className="rounded-md bg-rose-50 px-3 py-2 text-sm text-rose-600">{error}</p> : null}
              <div className="flex flex-col gap-3 pt-2">
                <Button type="submit" loading={submitting}>
                  회원가입 완료하기
                </Button>
                <Button type="button" variant="outline" onClick={() => router.push("/login")}
                >
                  이미 계정이 있으신가요?
                </Button>
              </div>
            </Form>
          </CardBody>
        </Card>
      </div>
    </main>
  );
}
