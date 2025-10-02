import { forwardRef } from "react";
import type { ButtonHTMLAttributes } from "react";
import { clsx } from "clsx";

type ButtonVariant = "primary" | "secondary" | "outline";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  block?: boolean;
  loading?: boolean;
};

const variantClassNames: Record<ButtonVariant, string> = {
  primary: "bg-brand text-brand-contrast hover:bg-brand-light focus:ring-brand/40",
  secondary: "bg-slate-100 text-brand hover:bg-slate-200 focus:ring-slate-200",
  outline: "border border-slate-300 text-brand hover:bg-slate-100 focus:ring-brand/30"
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", block = false, loading = false, disabled, children, ...props }, ref) => {
    const isDisabled = disabled || loading;

    return (
      <button
        ref={ref}
        className={clsx(
          "inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2",
          variantClassNames[variant],
          block && "w-full",
          isDisabled && "cursor-not-allowed opacity-60",
          className
        )}
        disabled={isDisabled}
        {...props}
      >
        {loading ? (
          <span className="inline-flex items-center gap-2">
            <span className="h-3 w-3 animate-spin rounded-full border-2 border-brand-contrast border-t-transparent" aria-hidden />
            <span>{children}</span>
          </span>
        ) : (
          children
        )}
      </button>
    );
  }
);

Button.displayName = "Button";
