# -*- coding: utf-8 -*-
"""swagger-demo 端到端验证：业务接口 + 异常分支 + OpenAPI 文档 + 分组 + Swagger UI。

用法：python tools/verify_endpoints.py
前置：应用已启动并监听 8080。脚本自带就绪探测，不依赖外部 sleep 循环。
"""
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

BASE = "http://localhost:8080"
RESULTS = []


def request(method, path, body=None, headers=None, timeout=10):
    url = BASE + path
    data = None
    hdrs = dict(headers or {})
    if body is not None:
        if isinstance(body, (dict, list)):
            data = json.dumps(body, ensure_ascii=False).encode("utf-8")
            hdrs["Content-Type"] = "application/json; charset=utf-8"
        elif isinstance(body, bytes):
            data = body
        else:
            data = str(body).encode("utf-8")
    req = urllib.request.Request(url, data=data, method=method, headers=hdrs)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, dict(resp.headers), resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, dict(e.headers), e.read().decode("utf-8", "replace")
    except Exception as e:  # noqa: BLE001
        return -1, {}, "REQUEST_ERROR: %s" % e


def check(name, cond, detail=""):
    RESULTS.append((name, bool(cond), detail))


def json_body(text):
    try:
        return json.loads(text)
    except Exception:  # noqa: BLE001
        return {}


def multipart_file(filename, content):
    boundary = "----wb" + uuid.uuid4().hex
    head = (
        '--%s\r\n'
        'Content-Disposition: form-data; name="file"; filename="%s"\r\n'
        'Content-Type: text/plain\r\n\r\n'
    ) % (boundary, filename)
    body = head.encode("utf-8") + content.encode("utf-8") + ("\r\n--%s--\r\n" % boundary).encode("utf-8")
    return body, {"Content-Type": "multipart/form-data; boundary=%s" % boundary}


def wait_ready():
    for _ in range(60):
        status, _, _ = request("GET", "/v3/api-docs", timeout=2)
        if status == 200:
            return True
        time.sleep(1)
    return False


def main():
    if not wait_ready():
        print("应用未就绪（60s 内 /v3/api-docs 未返回 200）")
        sys.exit(2)

    # ---------- 1. 用户模块：查询与异常 ----------
    s, _, b = request("GET", "/api/users")
    users = json_body(b)
    check("GET /api/users -> 200 且含初始两条用户",
          s == 200 and isinstance(users, list) and len(users) == 2
          and {u.get("id") for u in users} == {1001, 1002}, "status=%s body=%s" % (s, b[:200]))

    s, _, b = request("GET", "/api/users/1001")
    d = json_body(b)
    check("GET /api/users/1001 -> 200 张三", s == 200 and d.get("username") == "张三", "status=%s" % s)

    s, _, b = request("GET", "/api/users/999999")
    d = json_body(b)
    check("GET 不存在的用户 -> 404 + 统一错误体", s == 404 and d.get("code") == 404, "status=%s body=%s" % (s, b[:200]))

    s, _, b = request("GET", "/api/users/abc")
    d = json_body(b)
    check("GET 非法 ID 类型 -> 400（不落 500）", s == 400 and d.get("code") == 400, "status=%s body=%s" % (s, b[:200]))

    # ---------- 2. 创建与校验 ----------
    s, _, b = request("POST", "/api/users", {"username": "王五", "email": "wangwu@example.com"})
    d = json_body(b)
    check("POST /api/users 合法 -> 201 且返回 id=1003",
          s == 201 and d.get("id") == 1003 and d.get("username") == "王五", "status=%s body=%s" % (s, b[:200]))

    s, _, b = request("POST", "/api/users", {"username": "", "email": "bad-email"})
    d = json_body(b)
    check("POST 空姓名 + 非法邮箱 -> 400 且含字段级错误",
          s == 400 and d.get("code") == 400
          and "username" in d.get("fieldErrors", {}) and "email" in d.get("fieldErrors", {}),
          "status=%s body=%s" % (s, b[:250]))

    s, _, b = request("POST", "/api/users", {"username": "X" * 31, "email": "x@example.com"})
    d = json_body(b)
    check("POST 姓名超 30 字符 -> 400", s == 400 and "username" in d.get("fieldErrors", {}), "status=%s" % s)

    s, _, b = request("POST", "/api/users", b"{bad json", {"Content-Type": "application/json"})
    check("POST 非法 JSON -> 400（不落 500）", s == 400, "status=%s body=%s" % (s, b[:200]))

    # ---------- 3. 搜索 / 分页 / profile ----------
    s, _, b = request("GET", "/api/users/search?username=" + urllib.parse.quote("张"))
    d = json_body(b)
    check("GET /api/users/search?username=张 -> 200 命中张三",
          s == 200 and any(u.get("username") == "张三" for u in d), "status=%s body=%s" % (s, b[:200]))

    s, _, b = request("GET", "/api/users/search?pageNum=0&pageSize=10")
    check("GET 非法分页参数 -> 400", s == 400, "status=%s" % s)

    s, _, b = request("GET", "/api/users/profile")
    d = json_body(b)
    check("GET /api/users/profile -> 200（文档层 SecurityRequirement）",
          s == 200 and d.get("id") == 1001, "status=%s" % s)

    # ---------- 4. 文件上传 ----------
    body, hdr = multipart_file("demo.txt", "hello swagger")
    s, _, b = request("POST", "/api/users/upload", body, hdr)
    check("POST multipart 上传 -> 200 返回原始文件名", s == 200 and "demo.txt" in b, "status=%s body=%s" % (s, b[:200]))

    s, _, b = request("POST", "/api/users/upload", b"", {"Content-Type": "multipart/form-data; boundary=none"})
    check("POST 上传缺少 file 部件 -> 400", s == 400, "status=%s body=%s" % (s, b[:200]))

    # ---------- 5. 删除与状态自洽 ----------
    s, _, b = request("DELETE", "/api/users/1002")
    check("DELETE /api/users/1002 -> 204", s == 204, "status=%s" % s)

    s, _, b = request("GET", "/api/users/1002")
    check("删除后再查 1002 -> 404", s == 404, "status=%s" % s)

    s, _, b = request("GET", "/api/users")
    d = json_body(b)
    check("删除后列表不含 1002 且含新建用户",
          s == 200 and all(u.get("id") != 1002 for u in d)
          and any(u.get("id") == 1003 for u in d), "status=%s body=%s" % (s, b[:250]))

    s, _, b = request("DELETE", "/api/users/999999")
    check("DELETE 不存在的用户 -> 404", s == 404, "status=%s" % s)

    # ---------- 6. 订单模块 ----------
    s, _, b = request("GET", "/api/orders")
    d = json_body(b)
    check("GET /api/orders -> 200 且含 3 条", s == 200 and len(d) == 3, "status=%s body=%s" % (s, b[:250]))

    s, _, b = request("GET", "/api/orders?status=PAID")
    d = json_body(b)
    check("GET /api/orders?status=PAID -> 200 且仅 1 条", s == 200 and len(d) == 1, "status=%s body=%s" % (s, b[:200]))

    s, _, b = request("GET", "/api/orders/OD20261120001")
    d = json_body(b)
    check("GET 单个订单 -> 200 且 status=PAID", s == 200 and d.get("status") == "PAID", "status=%s" % s)

    s, _, b = request("GET", "/api/orders/NOT_EXIST")
    check("GET 不存在的订单 -> 404", s == 404, "status=%s" % s)

    # ---------- 7. OpenAPI JSON ----------
    s, _, b = request("GET", "/v3/api-docs")
    doc = json_body(b)
    paths = doc.get("paths", {})
    schemas = doc.get("components", {}).get("schemas", {})
    sec = doc.get("components", {}).get("securitySchemes", {})
    check("GET /v3/api-docs -> 200 且为 OpenAPI 3",
          s == 200 and str(doc.get("openapi", "")).startswith("3."), "status=%s openapi=%s" % (s, doc.get("openapi")))

    check("文档包含用户与订单全部路径",
          all(p in paths for p in ["/api/users", "/api/users/{id}", "/api/users/search",
                                   "/api/users/upload", "/api/users/profile",
                                   "/api/orders", "/api/orders/{orderNo}"]),
          "paths=%s" % sorted(paths.keys()))

    check("文档包含 @Tag 分组标签",
          any(t.get("name") == "用户管理" for t in doc.get("tags", [])), "tags=%s" % doc.get("tags"))

    check("文档包含 Bearer Authentication 安全方案", "Bearer Authentication" in sec, "sec=%s" % list(sec.keys()))

    check("文档包含 DTO Schema 且 username 为必填",
          "UserCreateRequest" in schemas and "username" in schemas.get("UserCreateRequest", {}).get("required", []),
          "required=%s" % schemas.get("UserCreateRequest", {}).get("required"))

    enum_values = schemas.get("OrderResponse", {}).get("properties", {}).get("status", {}).get("enum", [])
    check("文档包含枚举值（OrderResponse.status 的 enum 列表）",
          len(enum_values) == 5 and "PAID" in enum_values, "enum=%s" % enum_values)

    profile_op = paths.get("/api/users/profile", {}).get("get", {})
    check("profile 操作声明了 SecurityRequirement",
          any("Bearer Authentication" in r for r in profile_op.get("security", [])),
          "security=%s" % profile_op.get("security"))

    upload_op = paths.get("/api/users/upload", {}).get("post", {})
    check("upload 操作声明 multipart/form-data 请求体",
          "multipart/form-data" in upload_op.get("requestBody", {}).get("content", {}),
          "content=%s" % list(upload_op.get("requestBody", {}).get("content", {}).keys()))

    # ---------- 8. YAML / 分组 / UI ----------
    s, _, b = request("GET", "/v3/api-docs.yaml")
    check("GET /v3/api-docs.yaml -> 200 且含 openapi 字段", s == 200 and "openapi:" in b, "status=%s" % s)

    for group, must_have, must_not in [
        ("用户模块", "/api/users", "/api/orders"),
        ("订单模块", "/api/orders", "/api/users"),
    ]:
        s, _, b = request("GET", "/v3/api-docs/" + urllib.parse.quote(group))
        d = json_body(b)
        p = d.get("paths", {})
        check("分组 /v3/api-docs/%s -> 200 且路径正确隔离" % group,
              s == 200 and any(k.startswith(must_have) for k in p)
              and not any(k.startswith(must_not) for k in p),
              "status=%s paths=%s" % (s, sorted(p.keys())))

    s, _, b = request("GET", "/v3/api-docs/swagger-config")
    d = json_body(b)
    cfg = json.dumps(d, ensure_ascii=False)
    check("swagger-config 列出两个分组", s == 200 and "用户模块" in cfg and "订单模块" in cfg,
          "status=%s body=%s" % (s, cfg[:300]))

    s, _, b = request("GET", "/swagger-ui.html")
    check("GET /swagger-ui.html -> 2xx/3xx", 200 <= s < 400, "status=%s" % s)

    s, _, b = request("GET", "/swagger-ui/index.html")
    check("GET /swagger-ui/index.html -> 200 且为 Swagger UI 页面",
          s == 200 and "swagger-ui" in b.lower(), "status=%s len=%s" % (s, len(b)))

    s, _, b = request("GET", "/favicon.ico")
    check("GET /favicon.ico -> 404（不产生 ERROR 堆栈）", s == 404, "status=%s" % s)

    # ---------- 汇总 ----------
    passed = sum(1 for _, ok, _ in RESULTS if ok)
    failed = len(RESULTS) - passed
    print("=" * 72)
    for name, ok, detail in RESULTS:
        print("%s %s%s" % ("PASS" if ok else "FAIL", name, ("  | " + detail) if not ok else ""))
    print("=" * 72)
    print("TOTAL=%d  PASS=%d  FAIL=%d" % (len(RESULTS), passed, failed))
    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
