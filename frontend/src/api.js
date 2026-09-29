let csrf;
export async function token() {
  const response = await fetch("/api/auth/csrf", {
    credentials: "same-origin",
  });
  if (!response.ok)
    throw new Error("Không kết nối được backend. Kiểm tra cửa sổ chạy Java.");
  csrf = await response.json();
  return csrf;
}
export async function api(path, method = "GET", body) {
  const headers = {};
  if (method !== "GET") {
    if (!csrf) await token();
    headers[csrf.headerName] = csrf.token;
  }
  if (body !== undefined) headers["Content-Type"] = "application/json";
  let r;
  try {
    r = await fetch("/api" + path, {
      method,
      headers,
      credentials: "same-origin",
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new Error(
      "Không kết nối được máy chủ. Nếu vừa lưu, hãy tải lại dữ liệu trước khi thử lại.",
    );
  }
  const text = await r.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    throw new Error(
      "Máy chủ trả về dữ liệu không hợp lệ. Kiểm tra backend và cổng kết nối.",
    );
  }
  if (!r.ok) {
    if (r.status === 401) window.dispatchEvent(new Event("session-expired"));
    throw new Error(data?.message || "Thao tác không thành công.");
  }
  return data;
}
export async function login(username, password) {
  await token();
  const r = await fetch("/api/auth/login", {
    method: "POST",
    credentials: "same-origin",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      [csrf.headerName]: csrf.token,
    },
    body: new URLSearchParams({ username, password }),
  });
  const data = await r.json();
  if (!r.ok) throw new Error(data.message);
  await token();
  return api("/auth/me");
}
export async function logout() {
  await api("/auth/logout", "POST");
  csrf = null;
}
export function exportCsv(name, columns, rows) {
  const escape = (v) =>
    '"' +
    String(v ?? "")
      .replace(/^[=+@-]/, "'$&")
      .replaceAll('"', '""') +
    '"';
  const csv =
    "\ufeff" +
    [
      columns.map((c) => c.label),
      ...rows.map((r) => columns.map((c) => (c.value ? c.value(r) : r[c.key]))),
    ]
      .map((row) => row.map(escape).join(","))
      .join("\r\n");
  const url = URL.createObjectURL(
    new Blob([csv], { type: "text/csv;charset=utf-8" }),
  );
  const a = document.createElement("a");
  a.href = url;
  a.download = name + ".csv";
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
export function parseCsv(text) {
  const rows = [];
  let row = [],
    cell = "",
    quoted = false;
  text = text.replace(/^\ufeff/, "");
  for (let i = 0; i < text.length; i++) {
    const c = text[i];
    if (c === '"') {
      if (quoted && text[i + 1] === '"') {
        cell += '"';
        i++;
      } else quoted = !quoted;
    } else if (c === "," && !quoted) {
      row.push(cell);
      cell = "";
    } else if ((c === "\n" || c === "\r") && !quoted) {
      if (c === "\r" && text[i + 1] === "\n") i++;
      row.push(cell);
      if (row.some((v) => v.trim())) rows.push(row);
      row = [];
      cell = "";
    } else cell += c;
  }
  if (quoted) throw new Error("CSV có dấu ngoặc kép chưa đóng.");
  row.push(cell);
  if (row.some((v) => v.trim())) rows.push(row);
  if (rows.length < 2)
    throw new Error("CSV cần một dòng tiêu đề và ít nhất một dòng dữ liệu.");
  const headers = rows.shift().map((s) => s.trim());
  if (new Set(headers).size !== headers.length)
    throw new Error("CSV bị trùng tên cột.");
  return rows.map((r, i) => {
    if (r.length !== headers.length)
      throw new Error("Số cột không đúng ở dòng " + (i + 2));
    return Object.fromEntries(headers.map((h, j) => [h, r[j].trim()]));
  });
}
