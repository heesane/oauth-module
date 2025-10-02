import { forwardRef } from "react";
import type { InputHTMLAttributes } from "react";
import { clsx } from "clsx";

type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  hint?: string;
  error?: string;
};

export const TextField = forwardRef<HTMLInputElement, TextFieldProps>(
  ({ id, label, type = "text", hint, error, className, ...props }, ref) => {
    const inputId = id ?? props.name;

    return (
      <div className={clsx("flex flex-col gap-1", className)}>
        <label className="text-sm font-medium text-slate-700" htmlFor={inputId}>
          {label}
        </label>
        <input
          ref={ref}
          id={inputId}
          type={type}
          className={clsx(
            "w-full rounded-lg border bg-white px-3.5 py-2.5 text-sm shadow-sm transition focus:outline-none focus:ring-2",
            error
              ? "border-rose-400 focus:border-rose-500 focus:ring-rose-200"
              : "border-slate-200 focus:border-brand focus:ring-brand/30"
          )}
          {...props}
        />
        {hint && !error ? <p className="text-xs text-slate-500">{hint}</p> : null}
        {error ? <p className="text-xs text-rose-500">{error}</p> : null}
      </div>
    );
  }
);

TextField.displayName = "TextField";
