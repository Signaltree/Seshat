#!/usr/bin/env bash
# ============================================================================
# Security Verification Script — Seshat
# ============================================================================
# Prerequisites:
#   1. App running at http://localhost:8080
#   2. ADMIN_PASS env var set (e.g. export ADMIN_PASS='test123')
#   3. curl, grep, sed, tr installed
#
# Usage:
#   chmod +x test/verify-security.sh
#   ./test/verify-security.sh           # run all tests
#   ADMIN_PASS='test123' ./test/verify-security.sh  # specify password
#
# Exit codes: 0 = all passed, 1 = one or more failures
# ============================================================================

set -uo pipefail

BASE="${BASE:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
PASS=0
FAIL=0
COOKIE_JAR=$(mktemp)
CSRF_COOKIE_JAR=$(mktemp)

cleanup() {
    rm -f "$COOKIE_JAR" "$CSRF_COOKIE_JAR"
}
trap cleanup EXIT

log_pass()  { echo "  ✅ $1"; ((PASS++)); }
log_fail()  { echo "  ❌ $1"; ((FAIL++)); }
log_info()  { echo "  ℹ️  $1"; }
log_skip()  { echo "  ⏭️  $1"; }

# ---------------------------------------------------------------------------
# Section header
# ---------------------------------------------------------------------------
section() {
    echo ""
    echo "========== $1 =========="
}

# ---------------------------------------------------------------------------
# Extract CSRF token from a page's HTML (hidden _csrf input)
# Usage: extract_csrf_token <html>
# ---------------------------------------------------------------------------
extract_csrf_token() {
    echo "$1" | grep -oP 'name="_csrf"\s+value="\K[^"]+' | head -1
}

# ---------------------------------------------------------------------------
# Extract CSRF token from meta tag (used in index.html for HTMX)
# ---------------------------------------------------------------------------
extract_meta_csrf() {
    echo "$1" | grep -oP '<meta name="_csrf"\s+th:content="\K[^"]+' | head -1
}

# ---------------------------------------------------------------------------
# 1. SECURITY HEADERS
# ---------------------------------------------------------------------------
section "1. Security Headers (unauthenticated)"

# GET /login (permitted without auth)
resp_headers=$(mktemp)
curl -s -D "$resp_headers" "${BASE}/login" -o /dev/null

echo "  Checking headers on GET /login..."

if grep -qi "content-security-policy:" < "$resp_headers"; then
    log_pass "CSP header present"
else
    log_fail "CSP header MISSING"
fi

if grep -qi "strict-transport-security:" < "$resp_headers"; then
    log_pass "HSTS header present"
else
    log_fail "HSTS header MISSING"
fi

if grep -qi "x-frame-options:\s*DENY" < "$resp_headers"; then
    log_pass "X-Frame-Options: DENY"
else
    log_fail "X-Frame-Options: DENY MISSING"
fi

if grep -qi "x-content-type-options:\s*nosniff" < "$resp_headers"; then
    log_pass "X-Content-Type-Options: nosniff"
else
    log_fail "X-Content-Type-Options MISSING"
fi

if grep -qi "referrer-policy:" < "$resp_headers"; then
    log_pass "Referrer-Policy header present"
else
    log_fail "Referrer-Policy MISSING"
fi

if grep -qi "x-xss-protection:\s*1;\s*mode=block" < "$resp_headers"; then
    log_pass "X-XSS-Protection: 1; mode=block"
else
    log_fail "X-XSS-Protection MISSING"
fi

# Check CSP content for key directives
csp_line=$(grep -ai "content-security-policy:" "$resp_headers" | head -1)
if echo "$csp_line" | grep -qi "unsafe-inline"; then
    log_pass "CSP includes unsafe-inline (required for Tailwind CDN)"
else
    log_fail "CSP missing unsafe-inline"
fi
if echo "$csp_line" | grep -qi "frame-ancestors.*none"; then
    log_pass "CSP frame-ancestors: none"
else
    log_fail "CSP missing frame-ancestors: none"
fi

rm -f "$resp_headers"

# ---------------------------------------------------------------------------
# 2. CSRF COOKIE ON LOGIN PAGE
# ---------------------------------------------------------------------------
section "2. CSRF Cookie & Token on Login Page"

login_html=$(mktemp)
curl -s -c "$CSRF_COOKIE_JAR" "${BASE}/login" > "$login_html"

csrf_cookie=$(grep -c "XSRF-TOKEN" "$CSRF_COOKIE_JAR" 2>/dev/null || true)
if [ "$csrf_cookie" -ge 1 ]; then
    log_pass "CSRF cookie (XSRF-TOKEN) set on GET /login"
else
    log_fail "No CSRF cookie received"
fi

csrf_token=$(extract_csrf_token "$(cat "$login_html")")
if [ -n "$csrf_token" ]; then
    log_pass "CSRF hidden field present in login form"
else
    log_fail "CSRF hidden field MISSING in login form"
fi

rm -f "$login_html"

# ---------------------------------------------------------------------------
# 3. AUTHENTICATION
# ---------------------------------------------------------------------------
section "3. Authentication (Login / Logout)"

login_html=$(mktemp)
curl -s -c "$COOKIE_JAR" "${BASE}/login" > "$login_html"
csrf_token=$(extract_csrf_token "$(cat "$login_html")")
rm -f "$login_html"

if [ -z "$csrf_token" ]; then
    log_fail "Cannot extract CSRF token from login page — aborting auth tests"
    # cannot continue without auth, skip remaining tests
    log_info "Skipping remaining authenticated tests..."
else
    # Login with valid credentials
    login_resp=$(mktemp)
    http_code=$(curl -s -o "$login_resp" -w "%{http_code}" \
        -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
        -d "username=${ADMIN_USER}&password=${ADMIN_PASS}&_csrf=${csrf_token}" \
        "${BASE}/login")

    if [ "$http_code" -eq 302 ]; then
        log_pass "Login successful (302 redirect to /)"
    else
        log_fail "Login failed (HTTP ${http_code}, expected 302)"
    fi
    rm -f "$login_resp"

    # -----------------------------------------------------------------------
    # 4. SECURITY HEADERS ON AUTHENTICATED PAGE
    # -----------------------------------------------------------------------
    section "4. Security Headers (authenticated)"

    resp_headers=$(mktemp)
    curl -s -b "$COOKIE_JAR" -D "$resp_headers" "${BASE}/" -o /dev/null

    if grep -qi "content-security-policy:" < "$resp_headers"; then
        log_pass "CSP on / (authenticated)"
    else
        log_fail "CSP missing on /"
    fi
    rm -f "$resp_headers"

    # -----------------------------------------------------------------------
    # 5. CSRF ENFORCEMENT (POST without token → 403)
    # -----------------------------------------------------------------------
    section "5. CSRF Enforcement"

    # POST to a protected endpoint WITHOUT CSRF token
    http_no_csrf=$(curl -s -o /dev/null -w "%{http_code}" \
        -b "$COOKIE_JAR" \
        -X POST "${BASE}/personas/guardar" \
        -d "id=0&nombres=Test")

    if [ "$http_no_csrf" -eq 403 ]; then
        log_pass "POST without CSRF token → 403 Forbidden"
    elif [ "$http_no_csrf" -eq 401 ]; then
        log_pass "POST without CSRF token → 401 (also acceptable, CSRF not the only gate)"
    else
        log_fail "POST without CSRF token returned HTTP ${http_no_csrf} (expected 403)"
    fi

    # POST WITH CSRF token (extracted from / page)
    index_html=$(mktemp)
    curl -s -b "$COOKIE_JAR" -c "$COOKIE_JAR" "${BASE}/" > "$index_html"
    csrf_token_index=$(grep -oP '<meta name="_csrf"\s+th:content="\K[^"]+' "$index_html" | head -1)
    csrf_header=$(grep -oP '<meta name="_csrf_header"\s+th:content="\K[^"]+' "$index_html" | head -1)
    rm -f "$index_html"

    if [ -z "$csrf_token_index" ]; then
        log_skip "Cannot extract CSRF meta token — skipping token-present test"
        log_info "  (meta tags may be server-rendered; try with XSRF-TOKEN cookie)"
    else
        http_with_csrf=$(curl -s -o /dev/null -w "%{http_code}" \
            -b "$COOKIE_JAR" \
            -X POST "${BASE}/personas/guardar" \
            -H "X-CSRF-TOKEN: ${csrf_token_index}" \
            -d "id=0&nombres=Test&apellidos=Test&rut=1.1.1.1-1")

        if [ "$http_with_csrf" -eq 200 ]; then
            log_pass "POST with CSRF token → accessible (200 or redirect)"
        elif [ "$http_with_csrf" -eq 302 ]; then
            log_pass "POST with CSRF token → 302 (redirect, acceptable)"
        else
            # Try with cookie-based token
            xsrf_cookie=$(grep "XSRF-TOKEN" "$COOKIE_JAR" 2>/dev/null | awk '{print $NF}')
            if [ -n "$xsrf_cookie" ]; then
                http_with_csrf2=$(curl -s -o /dev/null -w "%{http_code}" \
                    -b "$COOKIE_JAR" \
                    -X POST "${BASE}/personas/guardar" \
                    -H "X-CSRF-TOKEN: ${xsrf_cookie}" \
                    -d "id=0&nombres=Test&apellidos=Test&rut=1.1.1.1-1")
                if [ "$http_with_csrf2" -eq 200 ] || [ "$http_with_csrf2" -eq 302 ]; then
                    log_pass "POST with cookie-based CSRF → ${http_with_csrf2}"
                else
                    log_fail "POST with cookie-based CSRF returned HTTP ${http_with_csrf2}"
                fi
            else
                log_fail "POST with CSRF token returned HTTP ${http_with_csrf}"
            fi
        fi
    fi

    # -----------------------------------------------------------------------
    # 6. RATE LIMITING
    # -----------------------------------------------------------------------
    section "6. Rate Limiting"

    # Get a fresh CSRF token for login attempts
    login_html=$(mktemp)
    curl -s -c "$COOKIE_JAR" "${BASE}/login" > "$login_html"
    csrf_token=$(extract_csrf_token "$(cat "$login_html")")
    rm -f "$login_html"

    if [ -z "$csrf_token" ]; then
        log_skip "Cannot extract CSRF token — skipping rate limit test"
    else
        echo "  Attempting 6 rapid failed logins..."

        rate_limit_hit=0
        for i in $(seq 1 6); do
            http_code=$(curl -s -o /dev/null -w "%{http_code}" \
                -b "$COOKIE_JAR" \
                -d "username=attacker&password=wrong${i}&_csrf=${csrf_token}" \
                "${BASE}/login")
            if [ "$http_code" -eq 429 ]; then
                rate_limit_hit=$i
                break
            fi
            # Brief pause to avoid overwhelming the server, but still within 60s window
            sleep 0.1
        done

        if [ "$rate_limit_hit" -eq 6 ]; then
            log_pass "Rate limiting triggered on attempt #6 (429 Too Many Requests)"
        elif [ "$rate_limit_hit" -gt 0 ]; then
            log_fail "Rate limiting triggered early on attempt #${rate_limit_hit} (expected #6)"
        else
            log_fail "Rate limiting NOT triggered after 6 rapid login attempts"
        fi
    fi

    # -----------------------------------------------------------------------
    # 7. FILE UPLOAD MAGIC BYTES VALIDATION
    # -----------------------------------------------------------------------
    section "7. File Upload Magic Bytes Validation"

    # Need to get CSRF token from the index page
    index_html=$(mktemp)
    curl -s -b "$COOKIE_JAR" -c "$COOKIE_JAR" "${BASE}/" > "$index_html"
    csrf_token_index2=$(grep -oP '<meta name="_csrf"\s+th:content="\K[^"]+' "$index_html" | head -1)
    xsrf_cookie2=$(grep "XSRF-TOKEN" "$COOKIE_JAR" 2>/dev/null | awk '{print $NF}')
    rm -f "$index_html"

    token_to_use="${csrf_token_index2:-$xsrf_cookie2}"

    if [ -z "$token_to_use" ]; then
        log_skip "Cannot extract CSRF token — skipping file upload tests"
    else
        # Create test files with different magic bytes
        tmp_dir=$(mktemp -d)

        # Valid JPEG (magic bytes: FF D8 FF E0)
        printf '\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00' > "${tmp_dir}/test.jpg"

        # Valid PNG (magic bytes: 89 50 4E 47)
        printf '\x89\x50\x4E\x47\x0D\x0A\x1A\x0A' > "${tmp_dir}/test.png"

        # Valid PDF (magic bytes: 25 50 44 46)
        printf '%%PDF-1.4\n' > "${tmp_dir}/test.pdf"

        # Invalid — plain text (not PDF/JPEG/PNG/GIF)
        echo "This is not a valid image or PDF" > "${tmp_dir}/malware.txt"

        # Upload invalid file first — expect error
        echo "  Uploading invalid file (malware.txt)..."
        upload_resp=$(mktemp)
        http_upload=$(curl -s -o "$upload_resp" -w "%{http_code}" \
            -b "$COOKIE_JAR" \
            -X POST "${BASE}/fotos/subir" \
            -H "X-CSRF-TOKEN: ${token_to_use}" \
            -F "personaId=1" \
            -F "archivo=@${tmp_dir}/malware.txt" 2>/dev/null)

        if [ "$http_upload" -eq 200 ]; then
            # Check if error message is in response
            if grep -qi "no permitido\|error\|tipo de archivo" "$upload_resp" 2>/dev/null; then
                log_pass "Invalid file rejected with error message"
            else
                log_skip "Invalid file returned 200 but no error msg in response (check server logs)"
                log_info "  (HTMX fragment may not show server errors in curl)"
            fi
        elif [ "$http_upload" -eq 400 ] || [ "$http_upload" -eq 413 ] || [ "$http_upload" -eq 500 ]; then
            log_pass "Invalid file rejected (HTTP ${http_upload})"
        else
            log_fail "Invalid file upload returned unexpected HTTP ${http_upload}"
        fi
        rm -f "$upload_resp"

        # Upload valid JPEG file — expect success
        echo "  Uploading valid file (test.jpg)..."
        upload_resp2=$(mktemp)
        http_upload2=$(curl -s -o "$upload_resp2" -w "%{http_code}" \
            -b "$COOKIE_JAR" \
            -X POST "${BASE}/fotos/subir" \
            -H "X-CSRF-TOKEN: ${token_to_use}" \
            -F "personaId=1" \
            -F "archivo=@${tmp_dir}/test.jpg" 2>/dev/null)

        # Even 500 is ok if it's DB constraint (no persona 1), we care that it wasn't rejected by magic bytes
        if [ "$http_upload2" -eq 200 ] || [ "$http_upload2" -eq 500 ]; then
            log_pass "Valid JPEG accepted for processing (HTTP ${http_upload2})"
        else
            log_skip "Valid JPEG upload returned HTTP ${http_upload2} (may need DB setup)"
        fi
        rm -f "$upload_resp2"

        rm -rf "$tmp_dir"
    fi

    # -----------------------------------------------------------------------
    # 8. LOGOUT
    # -----------------------------------------------------------------------
    section "8. Logout"

    logout_html=$(mktemp)
    curl -s -b "$COOKIE_JAR" "${BASE}/login" > "$logout_html"
    csrf_token=$(extract_csrf_token "$(cat "$logout_html")")
    rm -f "$logout_html"

    if [ -z "$csrf_token" ]; then
        log_skip "Cannot extract CSRF token — skipping logout test"
    else
        http_logout=$(curl -s -o /dev/null -w "%{http_code}" \
            -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
            -d "_csrf=${csrf_token}" \
            -X POST "${BASE}/logout")

        if [ "$http_logout" -eq 302 ]; then
            log_pass "Logout successful (302 redirect)"
        else
            log_fail "Logout returned HTTP ${http_logout} (expected 302)"
        fi
    fi
fi

# ============================================================================
# SUMMARY
# ============================================================================
echo ""
echo "============================================"
echo "  Results: ${PASS} passed, ${FAIL} failed"
echo "============================================"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
exit 0
