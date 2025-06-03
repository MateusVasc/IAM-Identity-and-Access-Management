import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    // Aumenta de 0 para 10 VUs em 10s (ramp-up)
    { duration: '10s', target: 10 },
    // Mantém 10 VUs por 30s (carga constante)
    { duration: '30s', target: 10 },
    // Diminui de 10 para 0 VUs em 10s (ramp-down)
    { duration: '10s', target: 0 },
  ],
};

export default function () {
  const url = 'http://localhost:8080/register';

  const payload = JSON.stringify({
    nickname: `dummy_user_${__VU}_${__ITER}`,
    email: `user_${__VU}_${__ITER}@example.com`,
    password: 'Dummy@pass1234',
  });

  const headers = { 'Content-Type': 'application/json' };

  const res = http.post(url, payload, { headers });

  const success = check(res, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
  });

  if (!success) {
    console.log(`Erro na requisição: status ${res.status}`);
    console.log(`Response body: ${res.body}`);
  }

  console.log(`Register request by VU ${__VU}, iteration ${__ITER} took ${res.timings.duration} ms`);

  sleep(1);
}
