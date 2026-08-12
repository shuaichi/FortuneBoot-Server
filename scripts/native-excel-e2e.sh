#!/usr/bin/env bash
set -euo pipefail

: "${NATIVE_BINARY:?请设置 NATIVE_BINARY 为待测原生二进制的绝对路径}"
: "${E2E_PASSWORD:?请通过 E2E_PASSWORD 传入演示管理员密码}"

E2E_USERNAME="${E2E_USERNAME:-admin}"
E2E_PORT="${E2E_PORT:-18080}"
KEEP_E2E_ARTIFACTS="${KEEP_E2E_ARTIFACTS:-false}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${E2E_ARTIFACT_DIR:-$(mktemp -d "${TMPDIR:-/tmp}/fortuneboot-native-excel.XXXXXX")}"
mkdir -p "$RUN_DIR"
DB_PATH="$RUN_DIR/fortuneboot-e2e.db"
BASE_URL="http://127.0.0.1:${E2E_PORT}"
APP_PID=""

cleanup() {
  local exit_code=$?
  if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
  if [[ $exit_code -ne 0 || "$KEEP_E2E_ARTIFACTS" == "true" ]]; then
    printf '原生 Excel 验收产物保留在：%s\n' "$RUN_DIR" >&2
  else
    rm -rf "$RUN_DIR"
  fi
}
trap cleanup EXIT

fail() {
  printf '原生 Excel 验收失败：%s\n' "$1" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null || fail "缺少命令：$1"
}

for command in curl jq openssl sqlite3 java mvn; do
  require_command "$command"
done
[[ -x "$NATIVE_BINARY" ]] || fail "原生二进制不可执行：$NATIVE_BINARY"

start_application() {
  DB_TYPE=sqlite DB_PATH="$DB_PATH" \
    "$NATIVE_BINARY" \
    --server.port="$E2E_PORT" \
    --logging.file.path="$RUN_DIR/logs" \
    >"$RUN_DIR/application.out" 2>"$RUN_DIR/application.err" &
  APP_PID=$!

  for _ in $(seq 1 60); do
    if curl --silent --fail "$BASE_URL/getConfig" >"$RUN_DIR/config.json"; then
      return
    fi
    if ! kill -0 "$APP_PID" 2>/dev/null; then
      fail "应用启动进程提前退出"
    fi
    sleep 1
  done
  fail "应用在 60 秒内未就绪"
}

stop_application() {
  if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID"
    wait "$APP_PID" 2>/dev/null || true
  fi
  APP_PID=""
}

bootstrap_database() {
  start_application
  stop_application

  [[ -f "$DB_PATH" ]] || fail "应用未创建隔离 SQLite 数据库"
  sqlite3 "$DB_PATH" "
    UPDATE sys_config
    SET config_value = 'false'
    WHERE config_key = 'sys.account.captchaOnOff';
  "
  [[ "$(sqlite3 "$DB_PATH" "SELECT config_value FROM sys_config WHERE config_key = 'sys.account.captchaOnOff';")" == "false" ]] \
    || fail "无法关闭隔离 E2E 数据库的验证码"
}

encrypt_password() {
  local public_key
  public_key="$(curl --silent --fail "$BASE_URL/getRsaPublicKey" | jq -er '.data')"
  if base64 --decode </dev/null >/dev/null 2>&1; then
    printf '%s' "$public_key" | base64 --decode >"$RUN_DIR/public.der"
  else
    printf '%s' "$public_key" | base64 -D >"$RUN_DIR/public.der"
  fi
  openssl pkey -pubin -inform DER -in "$RUN_DIR/public.der" -out "$RUN_DIR/public.pem" >/dev/null 2>&1
  printf '%s' "$E2E_PASSWORD" | openssl pkeyutl -encrypt -pubin -inkey "$RUN_DIR/public.pem" \
    -pkeyopt rsa_padding_mode:pkcs1 | base64 | tr -d '\n'
}

login() {
  local encrypted_password
  encrypted_password="$(encrypt_password)"
  jq -n --arg username "$E2E_USERNAME" --arg password "$encrypted_password" \
    '{username: $username, password: $password}' >"$RUN_DIR/login-request.json"
  curl --silent --show-error --fail-with-body -X POST "$BASE_URL/login" \
    -H 'Content-Type: application/json' \
    --data-binary "@$RUN_DIR/login-request.json" \
    -o "$RUN_DIR/login-response.json"
  TOKEN="$(jq -er '.data.token' "$RUN_DIR/login-response.json")" \
    || fail "登录未返回 token；请确认 E2E_PASSWORD 与 admin/admin123 演示账号匹配"
  rm -f "$RUN_DIR/login-request.json" "$RUN_DIR/login-response.json"
}

download_template() {
  curl --silent --show-error --fail-with-body \
    -H "Authorization: Bearer $TOKEN" \
    -D "$RUN_DIR/template.headers" \
    "$BASE_URL/fortune/bill/1/excelTemplate" \
    -o "$RUN_DIR/template.xlsx"
  grep -qi 'content-disposition:.*\.xlsx' "$RUN_DIR/template.headers" \
    || fail "模板下载未返回 xlsx Content-Disposition"
  [[ -s "$RUN_DIR/template.xlsx" ]] || fail "模板下载为空"
}

build_fixture_writer() {
  mvn -q -pl fortuneboot-starter -am test-compile dependency:build-classpath \
    -DskipTests -Dmdep.outputFile="$RUN_DIR/classpath.txt" -DincludeScope=test
  [[ -s "$RUN_DIR/classpath.txt" ]] || fail "无法生成 Excel fixture 的测试类路径"
}

create_fixtures() {
  build_fixture_writer
  java -cp "$ROOT_DIR/fortuneboot-starter/target/test-classes:$ROOT_DIR/fortuneboot-starter/target/classes:$(cat "$RUN_DIR/classpath.txt")" \
    com.fortuneboot.service.fortune.importer.NativeExcelFixtureWriter \
    create "$RUN_DIR/template.xlsx" "$RUN_DIR/valid.xlsx" "$RUN_DIR/valid.xls" "$RUN_DIR/invalid.xlsx"
}

import_excel() {
  local source_file=$1
  local response_file=$2
  curl --silent --show-error --fail-with-body -X POST "$BASE_URL/fortune/bill/1/import" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@${source_file}" \
    -o "$response_file"
  jq -e '(.code == 0 or .code == 200) and .data.successCount == 1 and .data.failCount == 0 and .data.totalCount == 1' "$response_file" >/dev/null \
    || fail "导入响应不是单条成功结果：$(basename "$response_file")"
}

assert_bill_persisted() {
  local title=$1
  curl --silent --show-error --fail-with-body \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/fortune/bill/getPage?bookId=1&pageNum=1&pageSize=10&title=$title" \
    -o "$RUN_DIR/query-${title}.json"
  jq -e --arg title "$title" '.data.rows | any(.title == $title)' "$RUN_DIR/query-${title}.json" >/dev/null \
    || fail "未查询到已持久化账单：$title"
}

verify_invalid_file() {
  curl --silent --show-error --fail-with-body -X POST "$BASE_URL/fortune/bill/1/import" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@$RUN_DIR/invalid.xlsx" \
    -D "$RUN_DIR/invalid.headers" \
    -o "$RUN_DIR/invalid-response.xlsx"
  grep -qi 'content-disposition:.*\.xlsx' "$RUN_DIR/invalid.headers" \
    || fail "无效数据未返回错误 Excel"
  java -cp "$ROOT_DIR/fortuneboot-starter/target/test-classes:$ROOT_DIR/fortuneboot-starter/target/classes:$(cat "$RUN_DIR/classpath.txt")" \
    com.fortuneboot.service.fortune.importer.NativeExcelFixtureWriter \
    verify-error "$RUN_DIR/invalid-response.xlsx"
}

bootstrap_database
start_application
login
download_template
create_fixtures
import_excel "$RUN_DIR/valid.xlsx" "$RUN_DIR/import-xlsx.json"
assert_bill_persisted native-e2e-xlsx
import_excel "$RUN_DIR/valid.xls" "$RUN_DIR/import-xls.json"
assert_bill_persisted native-e2e-xls
verify_invalid_file
printf '原生 Excel 验收通过：模板下载、xlsx 导入、xls 导入、错误 Excel 返回均已验证。\n'
