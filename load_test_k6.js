import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '30s', target: 10 },
    { duration: '10s', target: 0 },
    { duration: '2m', target: 1000 }, // Ramp-up gradual
    { duration: '30s', target: 1000 }, // Sustenta carga
    { duration: '30s', target: 0 },    // Ramp-down
  ],
};

const BASE_URL = 'http://localhost:8080';
const JSON_HEADERS = { 'Content-Type': 'application/json' };
const params = (name, token = null) => ({
  tags: { name },
  headers: token
    ? { ...JSON_HEADERS, Authorization: `Bearer ${token}` }
    : JSON_HEADERS,
});

export default function () {
  // ---------- 1. REGISTER ----------
  const user = {
    nickname: `user_${__VU}_${__ITER}`,
    email:    `user_${__VU}_${__ITER}@gmail.com`,
    password: 'Dummy@pass1234',
  };

  const regRes = http.post(`${BASE_URL}/register`, JSON.stringify(user), params('register'));
  check(regRes, { 'register success': (r) => r.status === 200 || r.status === 201 });

  // ---------- 2. LOGIN ----------
  const loginPayload = JSON.stringify({ email: user.email, password: user.password });
  const loginRes = http.post(`${BASE_URL}/login`, loginPayload, params('login'));
  const { token: accessTokenA, refreshToken: refreshTokenA } = loginRes.json() || {};

  check(loginRes, {
    'login success':       (r) => r.status === 200,
    'accessToken exists':  () => !!accessTokenA,
    'refreshToken exists': () => !!refreshTokenA,
  });
  if (!accessTokenA || !refreshTokenA) return;

  // ---------- 3. REFRESH ----------
  const refreshPayload = JSON.stringify({
    accessToken: accessTokenA,
    refreshToken: refreshTokenA,
  });
  const refreshRes = http.post(`${BASE_URL}/refresh`, refreshPayload, params('refresh', accessTokenA));
  const { token: accessTokenB, refreshToken: refreshTokenB } = refreshRes.json() || {};

  check(refreshRes, {
    'refresh success':         (r) => r.status === 200,
    'new accessToken exists':  () => !!accessTokenB,
    'new refreshToken exists': () => !!refreshTokenB,
  });
  if (!accessTokenB || !refreshTokenB) return;

  // ---------- 4. LOGOUT ----------
  const logoutPayload = JSON.stringify({
    accessToken: accessTokenB,
    refreshToken: refreshTokenB,
  });

  const logoutRes = http.post(`${BASE_URL}/logout`, logoutPayload, {
    headers: {
      ...JSON_HEADERS,
      Authorization: `Bearer ${accessTokenB}`,
    },
    tags: { name: 'logout' },
    redirects: 0,
  });

  check(logoutRes, {
    'logout success': (r) =>
      r.status === 200 ||
      r.status === 204 ||
      (r.status === 401 && r.body.includes('Token has been revoked')),
  });

  sleep(1);
}
