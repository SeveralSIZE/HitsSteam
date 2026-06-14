import sys
import requests
import secrets
import string
import random
import json
import os


class CheckerResult:
    OK = 101
    CORRUPT = 102
    MUMBLE = 103
    DOWN = 104
    CHECK_FAILED = 110


FLAGS = []

STORE_FILE = "flags_store.json"

def load_store():
    if not os.path.exists(STORE_FILE):
        return []
    with open(STORE_FILE, "r") as f:
        return json.load(f)

def save_store(store):
    with open(STORE_FILE, "w") as f:
        json.dump(store, f)

def generate_random_flag(length=31):
    alphabet = string.ascii_uppercase + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length)) + '='


def random_string(length=10):
    return ''.join(random.choices(string.ascii_lowercase + string.digits, k=length))

def random_password():
    with open("exploits/rockyou.txt", "r", encoding="latin-1") as f:
        lines = f.readlines()
    return random.choice(lines).strip()


def get_base(ip, port):
    return f"http://{ip}:{port}"


def register(base, username, email, password):
    r = requests.post(f"{base}/auth/register", json={
        "username": username, "email": email, "password": password
    }, timeout=5)
    if r.status_code != 200:
        raise Exception(f"Register failed: {r.status_code}")
    return r.json()


def login(base, username, password):
    r = requests.post(f"{base}/auth/login", json={
        "username": username, "password": password
    }, timeout=5)
    if r.status_code != 200:
        raise Exception(f"Login failed: {r.status_code}")
    token = r.json().get("token")
    if not token:
        raise Exception("No token in login response")
    return token


def add_card(base, token, cardholder, card_number, expiry, cvv):
    r = requests.post(f"{base}/cards/add", json={
        "cardholderName": cardholder,
        "cardNumber": card_number,
        "expiry": expiry,
        "cvv": cvv,
        "amount": 0
    }, headers={"Authorization": f"Bearer {token}"}, timeout=5)
    if r.status_code != 200:
        raise Exception(f"Add card failed: {r.status_code}")
    return {}


def get_cards(base, token):
    r = requests.get(f"{base}/cards",
                     headers={"Authorization": f"Bearer {token}"},
                     timeout=5)
    if r.status_code != 200:
        raise Exception(f"Get cards failed: {r.status_code}")
    return r.json()


def search_users(base, token, q):
    r = requests.get(f"{base}/users/search",
                     params={"q": q},
                     headers={"Authorization": f"Bearer {token}"},
                     timeout=5)
    if r.status_code != 200:
        raise Exception(f"Search users failed: {r.status_code}")
    return r.json()


def check(target_ip, target_port, *args):
    try:
        base = get_base(target_ip, target_port)
        username = "checker_" + random_string(8)
        email = random_string(8) + "@check.com"
        password = random_password()

        register(base, username, email, password)

        token = login(base, username, password)

        add_card(base, token, "TEST USER", "4111111111111111", "12/2030", "123")

        cards = get_cards(base, token)
        if not isinstance(cards, list):
            return CheckerResult.MUMBLE

        users = search_users(base, token, username)
        if not isinstance(users, list):
            return CheckerResult.MUMBLE

        return CheckerResult.OK

    except requests.exceptions.ConnectionError:
        return CheckerResult.DOWN
    except requests.exceptions.Timeout:
        return CheckerResult.DOWN
    except Exception:
        return CheckerResult.MUMBLE


def put(target_ip, target_port, flag):
    try:
        base = get_base(target_ip, target_port)
        username = "putter_" + random_string(8)
        email = random_string(8) + "@put.com"
        password = random_password()

        register(base, username, email, password)
        token = login(base, username, password)
        add_card(base, token, "FLAG HOLDER", flag, "12/2030", "000")

        store = load_store()
        store.append({"username": username, "password": password, "flag": flag})
        save_store(store)

        return CheckerResult.OK

    except requests.exceptions.ConnectionError:
        return CheckerResult.DOWN
    except requests.exceptions.Timeout:
        return CheckerResult.DOWN
    except Exception as e:
        print(f"MUMBLE reason: {e}", file=sys.stderr)
        return CheckerResult.MUMBLE


def get(target_ip, target_port, *args):
    try:
        store = load_store()
        if not store:
            return CheckerResult.CORRUPT

        base = get_base(target_ip, target_port)
        entry = store[-1]

        token = login(base, entry["username"], entry["password"])
        cards = get_cards(base, token)

        if not cards:
            return CheckerResult.CORRUPT

        found = any(card.get("cardNumber") == entry["flag"] for card in cards)
        if not found:
            return CheckerResult.CORRUPT

        return CheckerResult.OK

    except requests.exceptions.ConnectionError:
        return CheckerResult.DOWN
    except requests.exceptions.Timeout:
        return CheckerResult.DOWN
    except Exception as e:
        print(f"CORRUPT reason: {e}", file=sys.stderr)
        return CheckerResult.CORRUPT


def get_flags(target_ip, target_port):
    store = load_store()
    if not store:
        print("No flags found")
        return CheckerResult.OK

    for entry in store:
        print(entry["flag"])

    return CheckerResult.OK


def execute_command(command, target_ip, target_port, *args):
    if command == "check":
        return check(target_ip, target_port, *args)
    elif command == "put":
        flag = args[0] if args else generate_random_flag()
        return put(target_ip, target_port, flag)
    elif command == "get":
        return get(target_ip, target_port, *args)
    elif command == "get_flags":
        return get_flags(target_ip, target_port)
    else:
        return CheckerResult.CHECK_FAILED


def main():
    if len(sys.argv) < 4:
        sys.exit(CheckerResult.CHECK_FAILED)

    target_ip = sys.argv[1]
    target_port = int(sys.argv[2])
    commands_string = sys.argv[3]
    commands = commands_string.split()

    for command in commands:
        args = sys.argv[4:] if len(sys.argv) > 4 else []
        result = execute_command(command, target_ip, target_port, *args)

        if result != CheckerResult.OK:
            sys.exit(result)

    sys.exit(CheckerResult.OK)


if __name__ == "__main__":
    main()