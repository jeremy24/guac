from enum import Enum
from flask import Flask
from flask import request
from flask import Response
from flask import json

import pymysql
import bcrypt


# Classes

# class Status:
#     def __init__(self, message):


class Code:
    bad_request = Response("Invalid Request", status=400)
    dupe_user = Response("User already exists", status=400)
    server_error = Response("Internal Error", status=500)
    success = Response("Success", status=200)
    user_not_found = Response("User not found", status=400)

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
server_app = Server("192.168.11.153", 3306, 'root', 'password', APP)



def query_db(query):
    try:
        with server_app.db_conn.cursor() as cursor:
            cursor.execute(query)
            a = cursor.fetchall()
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

        password = bcrypt.hashpw(str(password).encode(), bcrypt.gensalt())
        print("pass: ", password)


        print(bcrypt.checkpw(p.encode(), password))

        print("Trying to add the user")
        try:
            query_str = "CALL add_user('{0}', '{1}', '{2}')".format(username, password.decode('utf-8'), key)
            print("Query: {0}".format(query_str))
        except Exception as ex:
            print("Query Error: {0}".format(ex))
            return Code.server_error

        res = query_db(query_str)

        print("RES:  ", res)
        if res == 1062:
            return Code.dupe_user
        return Code.success
    except Exception as ex:
        print("Add user error: {0}".format(ex))
        return Code.server_error


@APP.route("/user/addkey", methods=["POST"])
def add_user_key():
    data = request.get_json()

    if "username" not in data.keys():
        return Code.bad_request
    if 'public_key' not in data.keys():
        return Code.bad_request
    if len(list(data.keys())) != 2:
        return Code.bad_request

    user = get_user(internal=True)

    if user is None:
        return Code.user_not_found

    query = "CALL add_user_key('{0}', '{1}')".format(data['username'], data['public_key'])
    res = query_db(query)

    if len(res) == 1:
        return Code.success
    return Code.server_error


@APP.route("/user/get", methods=["POST"])
def get_user(internal=False):
    data = request.get_json()

    if "username" not in data.keys():
        return Code.bad_request
    if len(list(data.keys())) != 1:
        return Code.bad_request

    username = data["username"]
    query_str = "CALL get_user('{0}')".format(username)

    res = query_db(query_str)
    print(res)
    if len(res):
        resp = {'username': username, "public_key": res[0]["public_key"]}
        if internal:
            return resp
        return Response(json.dumps(resp), status=200)

    if internal:
        return None
    return Code.user_not_found





if __name__ == '__main__':
    server_app.start()
