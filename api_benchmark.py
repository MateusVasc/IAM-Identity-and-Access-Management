import os
import uuid
import time
import requests
import csv
from dotenv import load_dotenv

def make_dummy_register_body() -> dict:
    return {
        'nickname': f'dummy_{uuid.uuid4().hex[:8]}',
        'email': f"dummy_{uuid.uuid4().hex}@example.com",
        'password': 'Dummy@pass1234'
    }

def make_request(endpoint: str, body: dict, headers: dict = None) -> tuple[float, requests.Response]:
    start = time.perf_counter()
    try:
        resp = requests.post(endpoint, json=body, headers=headers, timeout=30)
    except requests.RequestException as e:
        print(f"❌ Erro de conexão: {e}")
        resp = requests.Response()
        resp.status_code = 500
        resp._content = b'{"error": "Connection error"}'
    end = time.perf_counter()
    return end - start, resp

def average_time(resp_dict: dict, key: str) -> float:
    resp_arr = resp_dict.get(key, [])
    if len(resp_arr) == 0:
        return 0.0
    return sum(resp_arr) / len(resp_arr)

def safe_print_average(response_times: dict, endpoint: str):
    avg_time = average_time(response_times, endpoint)
    count = len(response_times.get(endpoint, []))
    if count == 0:
        print(f"❌ {endpoint.upper()}: Nenhuma requisição bem-sucedida")
    else:
        print(f"✅ {endpoint.upper()}: {avg_time:.4f}s (baseado em {count} requisições)")

# main
load_dotenv()
api_base_url = os.environ.get('API_BASE_URL', 'http://localhost:8080')
endpoints = {
    'register': api_base_url + '/register',
    'login': api_base_url + '/login',
    'refresh': api_base_url + '/refresh',
    'logout': api_base_url + '/logout'
}

print(f"🚀 Iniciando benchmark contra: {api_base_url}")
print("Testando conectividade...")

try:
    test_resp = requests.get(api_base_url, timeout=5)
    print(f"✅ API respondendo - Status: {test_resp.status_code}")
except Exception as e:
    print(f"❌ Erro de conectividade: {e}")
    print("Verifique se a API está rodando e o URL está correto")
    exit(1)

response_times = {
    'register': [],
    'login': [],
    'refresh': [],
    'logout': []
}

csv_data = []
consecutive_errors = 0
max_consecutive_errors = 10

for i in range(1000):
    if i % 50 == 0:
        print(f"📊 Progresso: {i}/1000 iterações...")
    
    body = make_dummy_register_body()
    elapsed_time, register_resp = make_request(endpoints['register'], body)
    
    if register_resp.status_code == 200:
        response_times['register'].append(elapsed_time)
        csv_data.append((i + 1, 'register', elapsed_time))
        consecutive_errors = 0
    else:
        consecutive_errors += 1
        print(f"❌ Erro no register (iteração {i+1}): {register_resp.status_code}")
        if register_resp.status_code == 400:
            try:
                error_detail = register_resp.json()
                print(f"   Detalhes: {error_detail}")
            except:
                print(f"   Texto: {register_resp.text}")
        
        if consecutive_errors >= max_consecutive_errors:
            print(f"❌ Muitos erros consecutivos ({consecutive_errors}). Parando execução.")
            break
        continue

    login_body = {'email': body['email'], 'password': body['password']}
    login_elapsed_time, login_resp = make_request(endpoints['login'], login_body)
    
    if login_resp.status_code == 200:
        response_times['login'].append(login_elapsed_time)
        csv_data.append((i + 1, 'login', login_elapsed_time))
        consecutive_errors = 0
    else:
        consecutive_errors += 1
        print(f"❌ Erro no login (iteração {i+1}): {login_resp.status_code}")
        if login_resp.status_code in [400, 401, 403]:
            try:
                error_detail = login_resp.json()
                print(f"   Detalhes: {error_detail}")
            except:
                print(f"   Texto: {login_resp.text}")
        
        if consecutive_errors >= max_consecutive_errors:
            print(f"❌ Muitos erros consecutivos ({consecutive_errors}). Parando execução.")
            break
        continue

    try:
        login_data = login_resp.json()
        access_token = login_data.get('token')
        refresh_token = login_data.get('refreshToken')
        
        if not access_token or not refresh_token:
            print(f"❌ Tokens não encontrados na resposta do login (iteração {i+1})")
            print(f"   Resposta: {login_data}")
            consecutive_errors += 1
            if consecutive_errors >= max_consecutive_errors:
                print(f"❌ Muitos erros consecutivos. Parando execução.")
                break
            continue
    except Exception as e:
        print(f"❌ Erro ao processar resposta do login (iteração {i+1}): {e}")
        consecutive_errors += 1
        if consecutive_errors >= max_consecutive_errors:
            print(f"❌ Muitos erros consecutivos. Parando execução.")
            break
        continue

    refresh_body = {'refreshToken': refresh_token}
    refresh_elapsed_time, refresh_resp = make_request(endpoints['refresh'], refresh_body)
    
    if refresh_resp.status_code == 200:
        response_times['refresh'].append(refresh_elapsed_time)
        csv_data.append((i + 1, 'refresh', refresh_elapsed_time))
    else:
        print(f"❌ Erro no refresh (iteração {i+1}): {refresh_resp.status_code}")
        continue
    
    try:
        refresh_data = refresh_resp.json()
        new_access_token = refresh_data.get('token')
        new_refresh_token = refresh_data.get('refreshToken')
        
        if not new_access_token or not new_refresh_token:
            print(f"❌ Novos tokens não encontrados na resposta do refresh (iteração {i+1})")
            continue
    except Exception as e:
        print(f"❌ Erro ao processar resposta do refresh (iteração {i+1}): {e}")
        continue

    logout_headers = {'Authorization': f'Bearer {new_access_token}'}
    logout_body = {'refreshToken': new_refresh_token}
    logout_elapsed_time, logout_resp = make_request(endpoints['logout'], logout_body, headers=logout_headers)
    
    if logout_resp.status_code == 200:
        response_times['logout'].append(logout_elapsed_time)
        csv_data.append((i + 1, 'logout', logout_elapsed_time))
    else:
        print(f"❌ Erro no logout (iteração {i+1}): {logout_resp.status_code}")

print("\n" + "="*50)
print("RESULTADOS DO BENCHMARK")
print("="*50)
safe_print_average(response_times, 'register')
safe_print_average(response_times, 'login')
safe_print_average(response_times, 'refresh')
safe_print_average(response_times, 'logout')

print("\n" + "="*30)
print("ESTATÍSTICAS DETALHADAS")
print("="*30)
for endpoint, times in response_times.items():
    if times:
        min_time = min(times)
        max_time = max(times)
        avg_time = sum(times) / len(times)
        print(f"{endpoint.upper()}:")
        print(f"  📊 Count: {len(times)} | Mín: {min_time:.4f}s | Máx: {max_time:.4f}s | Média: {avg_time:.4f}s")
    else:
        print(f"{endpoint.upper()}: ❌ Nenhuma requisição bem-sucedida")

if csv_data:
    with open('response_times_post_update.csv', mode='w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['iteracao', 'endpoint', 'tempo_resposta'])
        writer.writerows(csv_data)
    print("\n✅ Arquivo 'response_times_post_update.csv' gerado com sucesso.")
else:
    print("\n❌ Nenhum dado foi coletado. CSV não gerado.")

print(f"\n📊 Resumo: {len(csv_data)} requisições bem-sucedidas de um total estimado de {(len(response_times['register']) + len(response_times['login']) + len(response_times['refresh']) + len(response_times['logout']))} tentativas.")
