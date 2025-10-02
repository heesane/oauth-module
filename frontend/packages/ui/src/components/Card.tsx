import type { PropsWithChildren, ReactNode } from "react";
import { clsx } from "clsx";

type CardProps = PropsWithChildren<{ className?: string }>;

type CardHeaderProps = {
  title: string;
  subtitle?: string;
  action?: ReactNode;
  className?: string;
};

type CardBodyProps = PropsWithChildren<{ className?: string }>;

export function Card({ className, children }: CardProps) {
  return <div className={clsx("surface shadow-soft", className)}>{children}</div>;
}

export function CardHeader({ title, subtitle, action, className }: CardHeaderProps) {
  return (
    <header
      className={clsx(
        "flex flex-col gap-3 border-b border-slate-100 bg-slate-50/60 p-6 sm:flex-row sm:items-center sm:justify-between",
        className
      )}
    >
      <div className="space-y-1">
        <h2 className="text-xl font-semibold text-brand">{title}</h2>
        {subtitle ? <p className="text-sm text-slate-500">{subtitle}</p> : null}
      </div>
      {action}
    </header>
  );
}

export function CardBody({ children, className }: CardBodyProps) {
  return <div className={clsx("flex flex-col gap-6 p-6", className)}>{children}</div>;
}
