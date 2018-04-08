from flask import Flask
from flask import request


import pymysql


class Server:
    def __init__(self, db_ip, db_port, db_user, db_pass):

        print("Connecting to {0}:{1} with {2}:{3}".format(db_ip, db_port, db_user, db_pass))

        self.db_conn = pymysql.connect(host=str(db_ip),
            port=db_port,
            user=db_user,
            password=db_pass,
            db="volcard",
            cursorclass=pymysql.cursors.DictCursor)

        self.app = Flask(__name__)

    def start(self):
        self.app.run()



app = Flask(__name__)
invalid_request = ("Invalid Request", 400)

@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route("/user/get", methods=["POST"])
def get_user():
    data = request.get_json()

    if "username" not in data.keys():
        return invalid_request
    username = data["username"]

    print(data)
    return "success"


if __name__ == '__main__':
    app = Server("192.168.10.81", 9000, 'root', 'password')
    with app.db_conn.cursor() as cursor:
        cursor.execute('SELECT * FROM users;')
        a = cursor.fetchall()
        print("Data:", a)