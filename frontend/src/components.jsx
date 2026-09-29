import React, { useState, useEffect, useRef } from "react";
import { labels } from "./config";
import { exportCsv } from "./api";
export function Icon({ name = "grid", size = 20 }) {
  const shapes = {
    grid: (
      <>
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <rect x="14" y="14" width="7" height="7" rx="1" />
      </>
    ),
    users: (
      <>
        <circle cx="9" cy="8" r="3" />
        <path d="M3 21v-3a6 6 0 0112 0v3M16 5a3 3 0 010 6m2 3a5 5 0 013 4v3" />
      </>
    ),
    book: (
      <>
        <path d="M12 5v16M3 4c4-1 6 0 9 1 3-1 5-2 9-1v15c-4-1-6 0-9 2-3-2-5-3-9-2z" />
      </>
    ),
    check: (
      <>
        <path d="M5 12l4 4L20 5M20 13v7H4V4h10" />
      </>
    ),
    lock: (
      <>
        <rect x="4" y="10" width="16" height="11" rx="2" />
        <path d="M8 10V7a4 4 0 018 0v3M12 14v3" />
      </>
    ),
    clock: (
      <>
        <circle cx="12" cy="12" r="9" />
        <path d="M12 7v5l3 2" />
      </>
    ),
    exit: (
      <>
        <path d="M10 4H4v16h6M9 12h12m-4-4 4 4-4 4" />
      </>
    ),
    search: (
      <>
        <circle cx="10" cy="10" r="6" />
        <path d="m15 15 6 6" />
      </>
    ),
    chart: (
      <>
        <path d="M4 3v18h18M8 17v-5m5 5V7m5 10v-8" />
      </>
    ),
  };
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {shapes[name] || shapes.grid}
    </svg>
  );
}
export function Badge({ value }) {
  return (
    <span className={"badge " + String(value).toLowerCase()}>
      {labels[value] || value}
    </span>
  );
}
export function PageTitle({ title, description, children }) {
  return (
    <div className="page-title">
      <div>
        <div className="eyebrow">CỔNG THÔNG TIN ĐÀO TẠO</div>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      <div className="title-actions">{children}</div>
    </div>
  );
}
export function Modal({ title, description, onClose, children, busy = false }) {
  const ref = useRef();
  const closeRef = useRef(onClose);
  closeRef.current = onClose;
  useEffect(() => {
    const prior = document.activeElement;
    const node = ref.current;
    node.querySelector("input,select,button")?.focus();
    const handler = (e) => {
      if (e.key === "Escape" && !busy) closeRef.current();
      if (e.key === "Tab") {
        const els = [
          ...node.querySelectorAll("button,input,select,textarea,a[href]"),
        ].filter((x) => !x.disabled);
        const first = els[0],
          last = els.at(-1);
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last?.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first?.focus();
        }
      }
    };
    node.addEventListener("keydown", handler);
    return () => {
      node.removeEventListener("keydown", handler);
      prior?.focus();
    };
  }, [busy]);
  return (
    <div className="modal-backdrop">
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="dialog-title"
        ref={ref}
      >
        <div className="modal-head">
          <h2 id="dialog-title">{title}</h2>
          <button
            type="button"
            className="icon-button"
            aria-label="Đóng"
            disabled={busy}
            onClick={onClose}
          >
            ×
          </button>
        </div>
        {description && <p className="muted">{description}</p>}
        {children}
      </section>
    </div>
  );
}
export function Editor({
  title,
  fields,
  initial = {},
  lookups = {},
  description,
  onClose,
  onSave,
  saveLabel = "Lưu thay đổi",
}) {
  const [values, setValues] = useState(() =>
    Object.fromEntries(
      fields.map((f) => [
        f.name,
        initial[f.name] ?? f.default ?? f.options?.[0]?.value ?? "",
      ]),
    ),
  );
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      await onSave(values);
      onClose();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Modal
      title={title}
      description={description}
      onClose={onClose}
      busy={busy}
    >
      <form onSubmit={submit}>
        {error && (
          <div role="alert" className="alert error">
            {error}
          </div>
        )}
        <div className="form-grid">
          {fields.map((f) => (
            <label key={f.name}>
              {f.label}
              {f.required && <span className="required"> *</span>}
              {f.source || f.options ? (
                <select
                  required={f.required}
                  value={values[f.name]}
                  disabled={busy}
                  onChange={(e) =>
                    setValues({ ...values, [f.name]: e.target.value })
                  }
                >
                  <option value="">Chọn {f.label.toLowerCase()}</option>
                  {(
                    f.options ||
                    (lookups[f.source] || []).map((r) => ({
                      value: r.id,
                      label: r.code + " · " + (r.name || r.full_name),
                    }))
                  ).map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  name={f.name}
                  value={values[f.name]}
                  type={f.type || "text"}
                  required={f.required}
                  min={f.min}
                  max={f.max}
                  step={f.step || (f.type === "number" ? "1" : undefined)}
                  minLength={f.minLength}
                  maxLength={f.maxLength}
                  pattern={f.pattern}
                  autoComplete={f.type === "password" ? "new-password" : "off"}
                  disabled={busy}
                  onChange={(e) =>
                    setValues({ ...values, [f.name]: e.target.value })
                  }
                />
              )}
            </label>
          ))}
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="secondary"
            disabled={busy}
            onClick={onClose}
          >
            Hủy
          </button>
          <button disabled={busy}>{busy ? "Đang lưu…" : saveLabel}</button>
        </div>
      </form>
    </Modal>
  );
}
export function Confirm({
  title,
  description,
  onClose,
  onConfirm,
  label = "Xác nhận",
  danger = false,
  reasonRequired = false,
}) {
  const [reason, setReason] = useState(""),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      await onConfirm(reason);
      onClose();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Modal
      title={title}
      description={description}
      onClose={onClose}
      busy={busy}
    >
      <form onSubmit={submit}>
        {reasonRequired && (
          <label>
            Lý do
            <textarea
              required
              maxLength={300}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
          </label>
        )}
        {error && (
          <div className="alert error" role="alert">
            {error}
          </div>
        )}
        <div className="modal-footer">
          <button
            type="button"
            className="secondary"
            disabled={busy}
            onClick={onClose}
          >
            Hủy
          </button>
          <button className={danger ? "danger" : ""} disabled={busy}>
            {busy ? "Đang xử lý…" : label}
          </button>
        </div>
      </form>
    </Modal>
  );
}
export function DataTable({
  columns,
  rows,
  actions,
  exportName = "danh-sach",
  filters,
  searchPlaceholder = "Tìm mã, họ tên hoặc thông tin…",
}) {
  const [q, setQ] = useState(""),
    [page, setPage] = useState(1),
    [size, setSize] = useState(10),
    [sort, setSort] = useState(null);
  const filtered = rows.filter((r) =>
    columns.some((c) =>
      String(c.value ? c.value(r) : (r[c.key] ?? ""))
        .toLocaleLowerCase("vi")
        .includes(q.toLocaleLowerCase("vi")),
    ),
  );
  if (sort) {
    const c = columns.find((c) => c.key === sort.key);
    filtered.sort(
      (a, b) =>
        String(c.value ? c.value(a) : (a[c.key] ?? "")).localeCompare(
          String(c.value ? c.value(b) : (b[c.key] ?? "")),
          "vi",
          { numeric: true },
        ) * sort.dir,
    );
  }
  const pages = Math.max(1, Math.ceil(filtered.length / size)),
    current = Math.min(page, pages),
    shown = filtered.slice((current - 1) * size, current * size);
  return (
    <div className="table-card">
      <div className="table-toolbar">
        <div className="search-box">
          <Icon name="search" size={18} />
          <input
            aria-label="Tìm kiếm"
            placeholder={searchPlaceholder}
            value={q}
            onChange={(e) => {
              setQ(e.target.value);
              setPage(1);
            }}
          />
        </div>
        {filters}
        <button
          className="secondary"
          onClick={() => exportCsv(exportName, columns, filtered)}
        >
          ↓ Xuất CSV
        </button>
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th className="index">STT</th>
              {columns.map((c) => (
                <th key={c.key}>
                  <button
                    className="sort"
                    onClick={() =>
                      setSort({
                        key: c.key,
                        dir: sort?.key === c.key ? -sort.dir : 1,
                      })
                    }
                  >
                    {c.label}
                    {sort?.key === c.key ? (sort.dir === 1 ? " ↑" : " ↓") : ""}
                  </button>
                </th>
              ))}
              {actions && <th>Thao tác</th>}
            </tr>
          </thead>
          <tbody>
            {shown.map((r, i) => (
              <tr key={r.id ?? i}>
                <td className="index">{(current - 1) * size + i + 1}</td>
                {columns.map((c) => (
                  <td key={c.key}>
                    {c.render
                      ? c.render(r)
                      : ((c.value ? c.value(r) : r[c.key]) ?? "—")}
                  </td>
                ))}
                {actions && (
                  <td>
                    <div className="row-actions">{actions(r)}</div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        {!shown.length && (
          <div className="empty">
            <Icon name="book" size={32} />
            <h3>Chưa có dữ liệu phù hợp</h3>
            <p>Thử thay đổi bộ lọc hoặc thêm dữ liệu mới.</p>
          </div>
        )}
      </div>
      <div className="table-footer">
        <span>
          Tổng cộng <strong>{filtered.length}</strong> bản ghi
        </span>
        <div>
          <select
            aria-label="Số dòng mỗi trang"
            value={size}
            onChange={(e) => {
              setSize(+e.target.value);
              setPage(1);
            }}
          >
            {[10, 20, 50].map((n) => (
              <option key={n} value={n}>
                {n} dòng
              </option>
            ))}
          </select>
          <button
            className="secondary"
            disabled={current <= 1}
            onClick={() => setPage(current - 1)}
          >
            ‹
          </button>
          <span>
            {current} / {pages}
          </span>
          <button
            className="secondary"
            disabled={current >= pages}
            onClick={() => setPage(current + 1)}
          >
            ›
          </button>
        </div>
      </div>
    </div>
  );
}
