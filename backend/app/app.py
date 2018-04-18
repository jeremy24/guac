from enum import Enum
from flask import Flask
from flask import request
from flask import Response
from flask import json

from Crypto.Signature import pkcs1_15
from Crypto.Signature import DSS

from Crypto.Hash import SHA256
from Crypto.Hash import BLAKE2b


from Crypto.PublicKey import RSA
from Crypto.PublicKey import ECC

import pymysql
import bcrypt
import time
import functools
import sys
import base64

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






def doublewrap(fn):
    """
    A decorator decorator, allowing to use the decorator to be used without
    parentheses if not arguments are provided. All arguments must be optional.
    """

    @functools.wraps(fn)
    def decorator(*args, **kwargs):
        if len(args) == 1 and len(kwargs) == 0 and callable(args[0]):
            return fn(args[0])
        else:
            return lambda wrapee: fn(wrapee, *args, **kwargs)

    return decorator


@doublewrap
def error_wrap(fn, do_exit=False):
    """
    A decorator for functions that catches errors thrown by a function.
        It has an optional parameter for whether to exit the program if an error is thrown.
    """
    @functools.wraps(fn)
    def _wrapper(*args, **kwargs):
        try:
            return fn(*args, **kwargs)
        except Exception as ex:
            print("ERROR {0}:  ".format(fn.__name__), ex)
            if do_exit:
                sys.exit(1)
            return None
    return _wrapper


@error_wrap(do_exit=True)
def check_keys(data, keys=list()):
    for key in keys:
        if key not in data.keys():
            print("Missing key: {0}".format(str(key)))
            return False
        if len(data) != len(list(data.keys())):
            print("Data is of invalid length")
            return False
    return True


@error_wrap(do_exit=False)
def verify_password(plain_password, password_hash):
    return bcrypt.checkpw(plain_password.encode(), password_hash.encode())


@error_wrap(do_exit=True)
def verify_ecc_signature(message, public_key_string, signature):
    if type(message) != str:
        print("messaged passed to verify_ecc_signature must be a string")
        return False

    try:
        pub_key = ECC.import_key(public_key_string)
    except Exception as ex:
        print("error making ecc key: ", ex)


    hashed = SHA256.new(message.encode("utf-8"))

    verifier = DSS.new(pub_key, 'fips-186-3')
    try:
        verifier.verify(hashed, signature)
        print("The message is authentic.")
        return True
    except ValueError:
        print("The message is not authentic.")
        return False







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
        print("QUERY: ", query)
        with server_app.db_conn.cursor() as cursor:
            cursor.execute(query)
            a = cursor.fetchall()
            server_app.db_conn.commit()
            return a
    except Exception as ex:
        print("Query_db error: {0}".format(ex))
        server_app.db_conn.rollback()
        if ex.args[0] == 1644:
            return 1644
        raise ex



@APP.route('/')
def hello_world():
    return 'This is not the right way to access the VolCard app...!'




@APP.route("/message/validate", methods=["POST"])
def validate_message():
    try:
        data = request.get_json()
        keys = ["username", "message", "signature"]

        if not check_keys(data=data, keys=keys):
            return Code.bad_request

        username = data["username"]
        message = data["message"]
        signature = data["signature"]

        user = _get_user(username)
        pub_str = user["public_key"]

        decoded_sig = base64.b64decode(signature)

        print("Validating:\n\tmsg: {0}\n\tkey: {1}\n\tsig: {2}".format(message, pub_str, signature))


        verified = verify_ecc_signature(message, pub_str, decoded_sig)

        print("VERIFIED:  ", verified)



        if verified:
            return Code.authorize_trans
        elif verified is None: # returning None from this is an error state
            return Code.server_error
        return Code.fail_trans

    except Exception as ex:
        print("/user/get", ex)
        return Code.server_error



@APP.route("/user/add", methods=["POST"])
def add_user():
    try:
        data = request.get_json()
        keys = ["username", "password", "public_key"]

        if not check_keys(data=data, keys=keys):
            return Code.bad_request

        print("/user/add:  {0}".format(data))

        username = data["username"]
        password = data['password']
        key = data["public_key"]

        # print("password:", password)
        start = time.time()
        salt = bcrypt.gensalt(rounds=WORK_FACTOR)
        hashed = bcrypt.hashpw(str(password).encode(), salt)
        end = time.time()

        # print("pass: ", password)
        print("Time to hash with work factor: {0}  {1}".format(WORK_FACTOR, end-start))
        print("hashed correctly: ", bcrypt.checkpw(password.encode(), hashed))
        print("Trying to add the user")

        try:
            query_str = "CALL add_user('{0}', '{1}', '{2}')".format(username, hashed.decode('utf-8'), key)
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





@APP.route("/user/addkey", methods=["POST"])
def add_user_key():
    try:
        data = request.get_json()
        print("data", data)
        keys = ["username", "public_key", "password"]

        if not check_keys(data, keys):
            return Code.bad_request

        print("checked")

        username = data['username']
        plain_pass = data["password"]
        key = data["public_key"]

        user = _get_user(user=username)

        if user is None:
            return Code.user_not_found

        hashed_password = user["password"]

        if not verify_password(plain_pass, hashed_password):
            return Code.not_authorized

        query = "CALL add_user_key('{0}', '{1}')".format(username, key)
        print(query)
        res = query_db(query)

        if len(res) == 1:
            return Code.success
        return Code.server_error
    except Exception as ex:
        print("Error /user/addkey:  ", ex)
        return Code.server_error


@APP.route("/user/get", methods=["POST"])
def get_user():
    try:
        data = request.get_json()
        keys = ["username"]

        if not check_keys(data=data, keys=keys):
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
    # key = ECC.generate(curve="P-256")
    # print(key.public_key().export_key(format="OpenSSH"))
    # pub = key.public_key()
    #
    # raw_msg = "user=bob,transaction=145368,endpoint=00015,type=purchase,amount=15.35,time={}".format(str(time.time()))
    # msg = raw_msg.encode("utf-8")
    #
    # # h = SHA256.new(b"test").digest()
    # h = SHA256.new(msg)
    # # print(h)
    # # h.update(msg)
    # # print(h.digest())
    # # h = h.digest()
    #
    # # print("pub: ", pub)
    #
    # signer = DSS.new(key, 'fips-186-3')
    # signature = signer.sign(h)
    #
    # encoded_sig = base64.b64encode(signature)
    #
    # print("Sig: ", encoded_sig.decode())
    #
    # pub_key_str = pub.export_key(format="OpenSSH")
    #
    # print("pub: ", pub_key_str)
    # print("msg: ", raw_msg)
    #
    #
    # time.sleep(1)
    # verify_ecc_signature(raw_msg, pub_key_str, signature)
    #
    # # verifier = DSS.new(pub, 'fips-186-3')
    # # try:
    # #     verifier.verify(h, signature)
    # #     print ("The message is authentic.")
    # # except ValueError:
    # #     print ("The message is not authentic.")


