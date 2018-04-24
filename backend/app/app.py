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
from Crypto.PublicKey import DSA


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
DUPE_USER_KEY = 1062



def resp_json(msg):
    return json.dumps({"message": str(msg)})


class Code:
    bad_request = Response(resp_json("Invalid Request"), status=400)
    dupe_user = Response(resp_json("User already exists"), status=418)
    server_error = Response(resp_json("Internal Error"), status=500)
    success = Response(resp_json("Success"), status=200)
    user_not_found = Response(resp_json("User not found"), status=400)
    not_authorized = Response(resp_json("Not Authorized"), status=400)

    authorize_trans = Response(resp_json("Transaction authorized"), status=200)
    fail_trans = Response(resp_json("Transaction not authorized"), status=418)






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
        if type(data) != dict:
            print("Data passed to check_keys is not a dict")
            return False
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
def verify_ecc_signature(message, public_key_encoded, sig):
    if type(message) != str:
        print("messaged passed to verify_ecc_signature must be a string")
        return False

    try:
        pub_key = base64.b64decode(public_key_encoded)
        ecc_key = ECC.import_key(pub_key)
    except Exception as ex:
        print("error making ECC key, must be wrong type", ex)
        return False

    hashed = SHA256.new(message.encode("utf-8"))
    verifier_ecc = DSS.new(ecc_key, "fips-186-3")

    try:
        verifier_ecc.verify(hashed, sig)
        print("The message is authentic.  ECC")
        # verifier_dsa.verify(hashed, sig)
        # print("The message is authentic.  DSA")
        return True
    except ValueError as ex:
        print("The message is not authentic.   ECC", ex)
        return False


def verify_rsa_signature(message, public_key_encoded, sig):
    if type(message) != str:
        print("messaged passed to verify_rsa_signature must be a string")
        return False

    try:
        pub_key = base64.b64decode(public_key_encoded)
        rsa_key = RSA.import_key(pub_key)
    except Exception as ex:
        print("error making RSA key, must be wrong type:  ", ex)
        return False

    hashed = SHA256.new(message.encode("utf-8"))
    verifier_rsa = pkcs1_15.new(rsa_key)

    try:
        verifier_rsa.verify(hashed, sig)
        print("The message is authentic.  RSA")
        # verifier_dsa.verify(hashed, sig)
        # print("The message is authentic.  DSA")
        return True
    except ValueError as ex:
        print("The message is not authentic.  RSA", ex)
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
        print("ex", ex)
        if ex.args[0] == DUPE_USER_KEY:
            return DUPE_USER_KEY
        raise ex



@APP.route('/')
def hello_world():
    return 'This is not the right way to access the VolCard app...!'



@APP.route("/message/validate/ec", methods=["POST"])
def validate_message_ecc():
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

        print("Validating ECC:\n\tmsg: '{0}'\n\tkey: '{1}'\n\tsig: '{2}'".format(message, pub_str, signature))
        print("Lengths: {0} {1} {2}".format(len(message), len(pub_str), len(signature)))

        verified = verify_ecc_signature(message, pub_str, decoded_sig)

        print("VERIFIED:  ", verified)

        if verified:
            return Code.authorize_trans
        elif verified is None: # returning None from this is an error state
            return Code.server_error
        return Code.fail_trans

    except Exception as ex:
        print("/message/validate", ex)
        return Code.server_error



@APP.route("/message/validate/rsa", methods=["POST"])
def validate_message_rsa():
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

        print("Validating RSA:\n\tmsg: '{0}'\n\tkey: '{1}'\n\tsig: '{2}'".format(message, pub_str, signature))
        print("Lengths: {0} {1} {2}".format(len(message), len(pub_str), len(signature)))

        verified = verify_rsa_signature(message, pub_str, decoded_sig)

        print("VERIFIED:  ", verified)

        if verified:
            return Code.authorize_trans
        elif verified is None: # returning None from this is an error state
            return Code.server_error
        return Code.fail_trans

    except Exception as ex:
        print("/message/validate", ex)
        return Code.server_error



@APP.route("/user/add", methods=["POST"])
def add_user():
    try:
        data = request.get_json()
        keys = ["username", "password", "public_key"]

        print("Add user req:  Type:{0}  Date: {1}".format(type(data), data))

        if not check_keys(data=data, keys=keys):
            return Code.bad_request

        username = data["username"]
        password = data['password']
        key = data["public_key"]

        # print("password:", password)
        start = time.time()
        salt = bcrypt.gensalt(rounds=WORK_FACTOR)
        hashed = bcrypt.hashpw(str(password).encode("utf-8"), salt)
        end = time.time()

        # print("pass: ", password)
        print("Time to hash with work factor: {0}  {1}".format(WORK_FACTOR, end-start))
        print("hashed correctly: ", bcrypt.checkpw(password.encode("utf-8"), hashed))
        print("Trying to add the user")

        try:
            query_str = "CALL add_user('{0}', '{1}', '{2}')".format(username, hashed.decode('utf-8'), key)
            print("Query: {0}".format(query_str))
        except Exception as ex:
            print("Query Error: {0}".format(ex))
            return Code.server_error

        res = query_db(query_str)

        print("RES:  ", res)
        if res == 1062:
            print("dupe user")
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
        if res and len(res):
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

        print ("Raw data:", data)
        keys = ["username"]

        if not check_keys(data=data, keys=keys):
            return Code.bad_request

        username = data["username"]
        query_str = "CALL get_user('{0}')".format(username)
        res = query_db(query_str)

        # print(res)
        if res and len(res):
            # print("get user raw resp: ", res)
            try:
                resp = {'username': username, "public_key": res[0]["public_key"]}
            except Exception as ex:
                print("Error building response from: Res: {0}  {1}".format(res, ex))
                return Code.server_error
            return Response(json.dumps(resp), status=200)

        return Code.user_not_found
    except Exception as ex:
        print("/user/get", ex)
        return Code.server_error




if __name__ == '__main__':
    server_app.start()



    # key = ECC.generate(curve="P-256")
    # e = base64.b64encode(key.public_key().export_key(format="DER"))
    #
    #
    #
    # print("Key:", str(e))
    # pub = key.public_key()
    # #

    #
    # key = ECC.generate(curve="P-256")
    # e = base64.b64encode(key.public_key().export_key(format="DER"))
    # # raw_msg = "user=bob,transaction=145368,endpoint=00015,type=purchase,amount=15.35,time={0}".format(str(time.time()))
    #
    # raw_msg = "00A4040007A000000247100100"
    #
    # msg = raw_msg.encode()
    # h = SHA256.new(msg)
    #
    # signer = DSS.new(key, 'fips-186-3')
    # signature = signer.sign(h)
    # encoded_sig = base64.b64encode(signature)


    #
    # print("Sig: ", encoded_sig.decode())
    #
    # # pub_key = pub.export_key(format="DER")
    # # pub_b64 = base64.b64encode(pub_key)
    # #
    # # print("pub: ", pub_b64)
    # print("msg: ", raw_msg)
    # #
    #
    # time.sleep(1)
    # verify_ecc_signature(raw_msg, e, signature)
    #
    # # verifier = DSS.new(pub, 'fips-186-3')
    # # try:
    # #     verifier.verify(h, signature)
    # #     print ("The message is authentic.")
    # # except ValueError:
    # #     print ("The message is not authentic.")


