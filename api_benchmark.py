import os
from dotenv import load_dotenv

load_dotenv()

api_base_url = os.environ.get('API_BASE_URL')
print(api_base_url)

# api_base_url = ""