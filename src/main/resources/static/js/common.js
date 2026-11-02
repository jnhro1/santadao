/* 산타다오 공통 스크립트: API 호출, 복사, 토스트 */
(function (global) {
  'use strict';

  var API_BASE = '/api/v1';

  /** fetch 래퍼. 성공 시 JSON, 실패 시 {status, message}를 담은 Error를 던진다. */
  function api(method, path, body) {
    var opts = { method: method, headers: { 'Accept': 'application/json' } };
    if (body !== undefined) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    return fetch(API_BASE + path, opts).then(function (res) {
      return res.text().then(function (text) {
        var data = null;
        if (text) { try { data = JSON.parse(text); } catch (e) { data = { message: text }; } }
        if (!res.ok) {
          var err = new Error(pickMessage(data) || '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
          err.status = res.status;
          err.data = data;
          throw err;
        }
        // 서버 응답 래퍼 ApiResponse { code, status, message, data }에서 data를 꺼낸다
        if (data && typeof data.code === 'number' && 'data' in data) return data.data || {};
        return data || {};
      });
    });
  }

  // ApiResponse(message) / Spring ProblemDetail(detail) / 필드 에러(errors[]) 모두 지원
  function pickMessage(data) {
    if (!data) return '';
    if (data.message) return data.message;
    if (data.detail) return data.detail;
    if (Array.isArray(data.errors) && data.errors.length) {
      var e = data.errors[0];
      return e.message || e.defaultMessage || e.reason || '';
    }
    return '';
  }

  /** 응답 필드 이름이 조금 달라도 동작하도록 첫 번째 값을 고른다. */
  function pick(obj) {
    for (var i = 1; i < arguments.length; i++) {
      var v = obj && obj[arguments[i]];
      if (v !== undefined && v !== null) return v;
    }
    return undefined;
  }

  /** 참가자 배열 정규화: ["민수"] 또는 [{name, pin, id}] → [{id, name, pin}] */
  function normalizeParticipants(list) {
    return (list || []).map(function (p) {
      if (typeof p === 'string') return { id: null, name: p, pin: null };
      return { id: pick(p, 'id', 'participantId'), name: pick(p, 'name', 'participantName'), pin: pick(p, 'pin') };
    });
  }

  /** URL 경로의 마지막 코드: /e/K7mQ2xPa → K7mQ2xPa (또는 ?code=) */
  function codeFromPath(prefix) {
    var m = location.pathname.match(new RegExp('/' + prefix + '/([A-Za-z0-9_-]+)'));
    if (m) return m[1];
    return new URLSearchParams(location.search).get('code');
  }

  function copyText(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text);
    }
    return new Promise(function (resolve, reject) {
      var ta = document.createElement('textarea');
      ta.value = text;
      ta.setAttribute('readonly', '');
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      try { document.execCommand('copy') ? resolve() : reject(); } catch (e) { reject(e); }
      document.body.removeChild(ta);
    });
  }

  var toastTimer;
  function toast(msg) {
    var el = document.getElementById('toast');
    if (!el) {
      el = document.createElement('div');
      el.id = 'toast';
      el.className = 'toast';
      el.setAttribute('role', 'status');
      el.setAttribute('aria-live', 'polite');
      document.body.appendChild(el);
    }
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { el.classList.remove('show'); }, 1800);
  }

  function copyWithToast(text, msg, btn, doneLabel) {
    return copyText(text).then(function () {
      toast(msg || '복사했어요');
      if (btn && doneLabel) {
        var original = btn.dataset.label || btn.textContent;
        btn.dataset.label = original;
        btn.textContent = doneLabel;
        setTimeout(function () { btn.textContent = original; }, 1500);
      }
    }, function () { toast('복사하지 못했어요. 직접 선택해서 복사해 주세요'); });
  }

  function restartAnim(el, cls) {
    el.classList.remove(cls);
    void el.offsetWidth;
    el.classList.add(cls);
  }

  global.Santa = {
    api: api,
    pick: pick,
    normalizeParticipants: normalizeParticipants,
    codeFromPath: codeFromPath,
    copyText: copyText,
    copyWithToast: copyWithToast,
    toast: toast,
    restartAnim: restartAnim
  };
})(window);
