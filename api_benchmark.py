import os
import uuid
import time
import requests
import csv
from dotenv import load_dotenv

def make_dummy_register_body() -> dict:
    return {
        'nickname': 'dummy_name',
        'email': f"dummy_{uuid.uuid4().hex}@example.com",
        'password': 'Dummy@_pass1234'
    }

def make_request(endpoint: str, body: dict, headers: dict = None) -> tuple[float, requests.Response]:
    start = time.perf_counter()
    resp = requests.post(endpoint, json=body, headers=headers)
    end = time.perf_counter()
    return end - start, resp

def average_time(resp_dict: dict, key: str) -> float:
    resp_arr = resp_dict.get(key)
    return sum(resp_arr) / len(resp_arr)

# main
load_dotenv()
api_base_url = os.environ.get('API_BASE_URL')
endpoints = {
    'register': api_base_url + '/register',
    'login': api_base_url + '/login',
    'refresh': api_base_url + '/refresh',
    'logout': api_base_url + '/logout'
}
response_times = {
    'register': [],
    'login': [],
    'refresh': [],
    'logout': []
}


csv_data = []  # lista de tuplas: (iteracao, endpoint, tempo_resposta)

for i in range(1000):
    # register
    body = make_dummy_register_body()
    elapsed_time, _ = make_request(endpoints['register'], body)
    response_times['register'].append(elapsed_time)
    csv_data.append((i + 1, 'register', elapsed_time))

    # login
    login_body = {'email': body['email'], 'password': body['password']}
    login_elapsed_time, login_resp = make_request(endpoints['login'], login_body)
    response_times['login'].append(login_elapsed_time)
    csv_data.append((i + 1, 'login', login_elapsed_time))

    login_data = login_resp.json()
    refresh_token = login_data.get('refreshToken')
    access_token = login_data.get('accessToken')
    headers = {'Authorization': f'Bearer {access_token}'}

    # refresh
    refresh_body = {'refreshToken': refresh_token}
    refresh_elapsed_time, _ = make_request(endpoints['refresh'], refresh_body, headers=headers)
    response_times['refresh'].append(refresh_elapsed_time)
    csv_data.append((i + 1, 'refresh', refresh_elapsed_time))

    # logout
    logout_body = {'refreshToken': refresh_token}
    logout_elapsed_time, _ = make_request(endpoints['logout'], logout_body, headers=headers)
    response_times['logout'].append(logout_elapsed_time)
    csv_data.append((i + 1, 'logout', logout_elapsed_time))


print(f"Tempo médio de resposta para /register: {average_time(response_times, 'register'):.4f} segundos")
print(f"Tempo médio de resposta para /login: {average_time(response_times, 'login'):.4f} segundos")
print(f"Tempo médio de resposta para /refresh: {average_time(response_times, 'refresh'):.4f} segundos")
print(f"Tempo médio de resposta para /logout: {average_time(response_times, 'logout'):.4f} segundos")


with open('response_times.csv', mode='w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['iteracao', 'endpoint', 'tempo_resposta'])
    writer.writerows(csv_data)

print("Arquivo 'response_times.csv' gerado com sucesso.")
