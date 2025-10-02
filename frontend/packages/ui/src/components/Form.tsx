import type { FormHTMLAttributes, PropsWithChildren, ReactNode } from "react";
import { clsx } from "clsx";

type FormProps = PropsWithChildren<
  FormHTMLAttributes<HTMLFormElement> & {
    title?: string;
    description?: string;
    footer?: ReactNode;
  }
>;

export function Form({
  children,
  className,
  title,
  description,
  footer,
  ...props
}: FormProps) {
  return (
    <form
      className={clsx("flex flex-col gap-6", className)}
      noValidate
      {...props}
    >
      {(title || description) && (
        <header className="flex flex-col gap-1">
          {title ? <h3 className="text-lg font-semibold text-brand">{title}</h3> : null}
          {description ? <p className="text-sm text-slate-500">{description}</p> : null}
        </header>
      )}
      <div className="flex flex-col gap-4">{children}</div>
      {footer ? <footer className="flex flex-col gap-3 pt-2">{footer}</footer> : null}
    </form>
  );
}
