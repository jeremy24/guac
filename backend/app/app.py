from enum import Enum
from flask import Flask
from flask import request
from flask import Response
from flask import json

from Crypto.Signature import pkcs1_15
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA

import pymysql
import bcrypt
import time

# Classes

# class Status:
#     def __init__(self, message):

WORK_FACTOR = 13


class Code:
    bad_request = Response("Invalid Request", status=400)
    dupe_user = Response("User already exists", status=400)
    server_error = Response("Internal Error", status=500)
    success = Response("Success", status=200)
    user_not_found = Response("User not found", status=400)
    not_authorized = Response("Not Authorized", status=400)

    authorize_trans = Response("Transaction authorized", status=200)
    fail_trans = Response("Transaction not authorized", status=200)


class Server:
    def __init__(self, db_ip, db_port, db_user, db_pass, http_app):

        print("Connecting to {0}:{1} with {2}:{3}".format(db_ip, db_port, db_user, db_pass))

        self.db_conn = pymysql.connect(host=str(db_ip),
                                       port=db_port,
                                       user=db_user,
                                       password=db_pass,
                                       db="volcard",
                                       cursorclass=pymysql.cursors.DictCursor)
        self.app = http_app


    def start(self):
        self.app.run(host='0.0.0.0')


# Global Variables

APP = Flask(__name__)
# server_app = Server("192.168.11.153", 3306, 'root', 'password', APP)
server_app = Server("home.piroax.com", 9003, 'root', 'password', APP)


def query_db(query):
    try:
        with server_app.db_conn.cursor() as cursor:
            cursor.execute(query)
            a = cursor.fetchall()
            server_app.db_conn.commit()
            return a
    except Exception as ex:
        print("Query_db error: {0}".format(ex))
        if ex.args[0] == 1644:
            return 1644
        raise ex



@APP.route('/')
def hello_world():
    return 'This is not the right way to access the volcard app...!'



@APP.route("/user/add", methods=["POST"])
def add_user():
    try:
        data = request.get_json()

        print("/user/add:  {0}".format(data))

        if "username" not in data.keys():
            return Code.bad_request
        if 'password' not in data.keys():
            return Code.bad_request
        if 'public_key' not in data.keys():
            key = 'NULL'
        else:
            key = data['public_key']

        username = data["username"]
        password = data['password']

        p = password
        print("password:", p)

        start = time.time()

        

        salt = bcrypt.gensalt(rounds=WORK_FACTOR)
        password = bcrypt.hashpw(str(password).encode(), salt)
        end = time.time()
        
        # print("pass: ", password)

        print("Time to hash with work factor: {0}  {1}".format(WORK_FACTOR, end-start))


        salt=None

        print(bcrypt.checkpw(p.encode(), password))

        print("Trying to add the user")
        try:
            query_str = "CALL add_user('{0}', '{1}', '{2}')".format(username, password.decode('utf-8'), key)
            print("Query: {0}".format(query_str))
        except Exception as ex:
            print("Query Error: {0}".format(ex))
            return Code.server_error

        res = query_db(query_str)

        # print("RES:  ", res)
        if res == 1062:
            return Code.dupe_user
        return Code.success
    except Exception as ex:
        print("Add user error: {0}".format(ex))
        return Code.server_error


def _get_user(user=None):
    try:
        username = str(user)

        query_str = "CALL get_user('{0}')".format(username)
        res = query_db(query_str)

        # print(res)
        if len(res):
            resp = {'username': username, "public_key": res[0]["public_key"], "password": res[0]["password"]}
            return resp
            
        return None
    except Exception as ex:
        print("_get_user", ex)
        return None


def _verify_password(password, hash):
    return bcrypt.checkpw(password.encode(), hash.encode())


def validate(username, message, signature):
    user = _get_user(username)
    key_str = user['public_key']

    try:
        key = RSA.import_key(key_str)
        print("key", key)
        digest = SHA256.new(message)
        print("digest", digest)
        try:
            pkcs1_15.new(key).verify(digest, signature)
            print ("The signature is valid.")
            return True
        except (ValueError, TypeError):
            print ("The signature is not valid.")
            return False
    except Exception as ex:
        print("validate error: ", ex)
        return None


@APP.route("/message/validate", methods=["POST"])
def validate_message():
    try:
        data = request.get_json()

        if "username" not in data.keys():
            return Code.bad_request
        if "message" not in data.keys():
            return Code.bad_request
        if "signature" not in data.keys():
            return Code.bad_request
        if len(list(data.keys())) != 3:
            return Code.bad_request

        username = data["username"]
        message = data["message"]
        signature = data["signature"]

        # dont let them just throw random stuff at us
        if _get_user(username) is None:
            return code.not_authorized

        res = validate(username, message, signature)
        if res == True:
            return Code.authorize_trans
        elif res is None: # returning None from this is an error state
            return Code.server_error
        return Code.fail_trans


    except Exception as ex:
        print("/user/get", ex)
        return Code.server_error    


@APP.route("/user/addkey", methods=["POST"])
def add_user_key():
    try:
        print("")
        data = request.get_json()

        # print("data:", data)
        # print("keys", data.keys())
        if "username" not in data.keys():
            print("missing username")
            return Code.bad_request
        if "public_key" not in data.keys():
            print("missing key")
            return Code.bad_request
        if "password" not in data.keys():
            print("missing password")
            return Code.bad_request
        if len(list(data.keys())) != 3:
            print("wrong length")
            return Code.bad_request

        username = data['username']
        plain_pass = data["password"]
        key = data["public_key"]

        user = _get_user(user=username)
        print("user: ", user)

        if not _verify_password(plain_pass, user["password"]):
            return Code.not_authorized

        if user is None:
            return Code.user_not_found

        query = "CALL add_user_key('{0}', '{1}')".format(data['username'], data['public_key'])
        res = query_db(query)

        if len(res) == 1:
            return Code.success
        return Code.server_error
    except Exception as ex:
        print("/user/addkey", ex)
        return Code.server_error



@APP.route("/user/get", methods=["POST"])
def get_user():
    try:
        data = request.get_json()

        if "username" not in data.keys():
            return Code.bad_request
        if len(list(data.keys())) != 1:
            return Code.bad_request

        username = data["username"]

        query_str = "CALL get_user('{0}')".format(username)
        res = query_db(query_str)

        # print(res)
        if len(res):
            resp = {'username': username, "public_key": res[0]["public_key"]}
            return Response(json.dumps(resp), status=200)

        return Code.user_not_found
    except Exception as ex:
        print("/user/get", ex)
        return Code.server_error




if __name__ == '__main__':
    server_app.start()
